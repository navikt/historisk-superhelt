package no.nav.historisk.superhelt.oppgave

import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.infrastruktur.authentication.Permission
import no.nav.historisk.superhelt.infrastruktur.authentication.SecurityContextUtils
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.oppgave.OppgaveClient
import no.nav.oppgave.OppgaveType
import no.nav.oppgave.model.OppgaveDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val SYSTEM_PERMISSIONS = listOf(Permission.READ, Permission.WRITE)

@Service
class OppgaveGjenopprettingService(
    private val sakRepository: SakRepository,
    private val oppgaveRepository: OppgaveRepository,
    private val oppgaveClient: OppgaveClient,
    private val oppgaveService: OppgaveService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun gjenopprettManglendeOppgaver(): List<Saksnummer> =
        SecurityContextUtils.runAsSystemuser(name = "oppgave-gjenoppretting", permissions = SYSTEM_PERMISSIONS) {
            val aapeneSaker = sakRepository.finnAapneSaker()
            val gjenopprettet = mutableListOf<Saksnummer>()

            for (sak in aapeneSaker) {
                val oppgaveType = OppgaveTypeMapper.fromSakstatus(sak.status) ?: continue
                runCatching {
                    if (!harAapenOppgave(sak, oppgaveType)) {
                        oppgaveService.opprettOppgave(
                            type = oppgaveType,
                            sak = sak,
                            beskrivelse = "Gjenopprettet oppgave i Superhelt for sak ${sak.saksnummer}"
                        )
                        logger.info("Gjenopprettet oppgave $oppgaveType for sak ${sak.saksnummer}")
                        gjenopprettet.add(sak.saksnummer)
                    }
                }.onFailure { e ->
                    logger.error("Klarte ikke behandle oppgavegjenoppretting for sak {}", sak.saksnummer, e)
                }
            }

            logger.info(
                "Gjenopprettingsjobb ferdig: {} av {} åpne saker fikk ny oppgave",
                gjenopprettet.size,
                aapeneSaker.size
            )
            gjenopprettet
        }

    private fun harAapenOppgave(sak: Sak, oppgaveType: OppgaveType): Boolean {
        val oppgaveIds = oppgaveRepository.finnOppgaverForSak(sak.saksnummer, oppgaveType)
        if (oppgaveIds.isEmpty()) return false

        return oppgaveIds.any { oppgaveId ->
            runCatching {
                val oppgave = oppgaveClient.hentOppgave(oppgaveId)
                oppgave != null && oppgave.status in arrayOf(
                    OppgaveDto.Status.OPPRETTET,
                    OppgaveDto.Status.AAPNET,
                    OppgaveDto.Status.UNDER_BEHANDLING
                )
            }.getOrElse { e ->
                logger.warn(
                    "Klarte ikke hente oppgave {} for sak {} — antar åpen",
                    oppgaveId,
                    sak.saksnummer,
                    e
                )
                true //  anta åpen hvis Gosys ikke svarer
            }
        }
    }
}


