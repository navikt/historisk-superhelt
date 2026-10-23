package no.nav.historisk.superhelt.oppgave

import no.nav.common.consts.APP_NAVN
import no.nav.common.types.EksternJournalpostId
import no.nav.common.types.EksternOppgaveId
import no.nav.common.types.Enhetsnummer
import no.nav.common.types.FolkeregisterIdent
import no.nav.common.types.NavIdent
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.ansatt.NavAnsattService
import no.nav.historisk.superhelt.infrastruktur.authentication.getAuthenticatedUser
import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.historisk.superhelt.person.PersonService
import no.nav.historisk.superhelt.sak.Sak
import no.nav.oppgave.OppgaveClient
import no.nav.oppgave.OppgaveType
import no.nav.oppgave.model.FinnOppgaverParams
import no.nav.oppgave.model.OppgaveDto
import no.nav.oppgave.model.OpprettOppgaveRequest
import no.nav.oppgave.model.PatchOppgaveRequest
import no.nav.oppgave.type
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class OppgaveService(
    private val oppgaveClient: OppgaveClient,
    private val oppgaveRepository: OppgaveRepository,
    private val personService: PersonService,
    private val navAnsattService: NavAnsattService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /** Finnner oppgaver som er relevant */
    private val relevanteOppgaverPredicate: (OppgaveDto) -> Boolean =
        { OppgaveType.JFR.name == it.oppgavetype || APP_NAVN == it.behandlesAvApplikasjon }

    /** Hent åpne oppgaver tilordnet en saksbehandler som kan saksbehandles i denne appen */
    @PreAuthorize("hasAuthority('READ')")
    fun hentOppgaverForSaksbehandler(): List<OppgaveMedSak> {

        val navAnsatt = navAnsattService.hentNavAnsatt()

        val oppgaver = oppgaveClient.finnOppgaver(
            FinnOppgaverParams(
                tilordnetRessurs = navAnsatt.ident,
                statuskategori = "AAPEN",
                tema = navAnsatt.tema,
                limit = 50L
            )
        ).oppgaver ?: emptyList()


        return oppgaver
            .filter(relevanteOppgaverPredicate)
            .map {
                it.toOppgaveMedSak(
                    sak = oppgaveRepository.finnSakForOppgave(it.id)
                )
            }
    }

    @PreAuthorize("hasAuthority('READ') and @tilgangsmaskin.harTilgang(#fnr)")
    fun hentOppgaverForPerson(fnr: FolkeregisterIdent): List<OppgaveMedSak> {
        val person = personService.hentPerson(fnr)
            ?: throw IllegalStateException("Fant ikke persondata for person")

        val temaForSaksbehandler = navAnsattService.hentNavAnsatt().tema

        val oppgaver = oppgaveClient.finnOppgaver(
            FinnOppgaverParams(
                aktoerId = listOf(person.aktorId),
                statuskategori = "AAPEN",
                tema = temaForSaksbehandler,
                limit = 50L
            )
        ).oppgaver ?: emptyList()

        return oppgaver
            .filter(relevanteOppgaverPredicate)
            .map {
                it.toOppgaveMedSak(
                    sak = oppgaveRepository.finnSakForOppgave(it.id)
                )
            }
    }

    /** Finn attesteringsoppgaver på tema og enhet, Egne saker og oppgaver som er plukket blir ikke med  */
    @PreAuthorize("hasAuthority('READ')")
    fun finnAttesteringsOppgaverForSaksbehandler(): List<OppgaveMedSak> {

        val navAnsatt = navAnsattService.hentNavAnsatt()
        val saksBehandlersEnheter = navAnsatt.enheter.map { it.enhetnummer }

        val oppgaver = saksBehandlersEnheter.flatMap { enhetsnummer ->
            oppgaveClient.finnOppgaver(
                FinnOppgaverParams(
                    tildeltEnhetsnr = enhetsnummer,
                    oppgavetype = listOf(OppgaveType.GOD_VED.oppgavetype),
                    statuskategori = "AAPEN",
                    tema = navAnsatt.tema,
                    limit = 50L
                )
            ).oppgaver ?: emptyList()
        }


        return oppgaver
            .filter(relevanteOppgaverPredicate)
            .filter { it.opprettetAv != navAnsatt.ident.value } // Ikke attester egne saker
            .filter { it.tilordnetRessurs == null } // Ikke plukk oppaver som er tatt
            .map {
                it.toOppgaveMedSak(
                    sak = oppgaveRepository.finnSakForOppgave(it.id)
                )
            }
    }

    @PreAuthorize("hasAuthority('READ')")
    @Transactional(readOnly = true)
    fun getOppgave(oppgaveId: EksternOppgaveId): OppgaveMedSak {
        val dto = oppgaveClient.hentOppgave(oppgaveId)
            ?: throw IkkeFunnetException("Fant ikke oppgave med id $oppgaveId")

        val sak = oppgaveRepository.finnSakForOppgave(dto.id)
        return dto.toOppgaveMedSak(sak)
    }

    @PreAuthorize("hasAuthority('WRITE')")
    @Transactional
    fun knyttOppgaveTilSak(saksnummer: Saksnummer, oppgaveId: EksternOppgaveId, oppgaveType: OppgaveType) {
        oppgaveRepository.save(saksnummer, oppgaveId, oppgaveType)
    }

    @PreAuthorize("hasAuthority('WRITE')")
    fun ferdigstillOppgave(oppgaveId: EksternOppgaveId) {
        runCatching {
            val oppgave = oppgaveClient.hentOppgave(oppgaveId)

            if (oppgave == null) {
                logger.warn("Fant ikke oppgave med id {}, kan ikke ferdigstille", oppgaveId)
                return
            }

            if (oppgave.status == OppgaveDto.Status.FERDIGSTILT) {
                logger.debug("Oppgave {} er allerede ferdigstilt, ingen mulighet å oppdatere", oppgave.id)
                return
            }

            oppgaveClient.patchOppgave(
                oppgaveId = oppgaveId,
                request = PatchOppgaveRequest(
                    versjon = oppgave.versjon,
                    status = OppgaveDto.Status.FERDIGSTILT
                )
            )
            logger.info("Ferdigstiller oppgave {}: {}", oppgave.type, oppgaveId)
        }.onFailure { e ->
            logger.error("Feil ved ferdigstilling av oppgave med id {}. Ignoreres", oppgaveId, e)
        }
    }

    /** Ferdigstiller oppgaver av gitt type for en sak. Hvis ingen type er oppgitt, ferdigstilles alle oppgaver for saken. */
    @PreAuthorize("hasAuthority('WRITE')")
    fun ferdigstillOppgaver(saksnummer: Saksnummer, vararg types: OppgaveType?) {
        if (types.isEmpty()) {
            logger.debug("Ferdigstiller alle oppgaver for sak {}", saksnummer)
            val oppgaveIds = oppgaveRepository.finnOppgaverForSak(saksnummer, null)
            oppgaveIds.forEach { oppgaveId ->
                ferdigstillOppgave(oppgaveId)
            }
        } else {
            logger.debug("Ferdigstiller oppgaver for sak {} av type {}", saksnummer, types.joinToString(","))
            types.forEach { type ->
                val oppgaveIds = oppgaveRepository.finnOppgaverForSak(saksnummer, type)
                oppgaveIds.forEach { oppgaveId ->
                    ferdigstillOppgave(oppgaveId)
                }
            }
        }
    }

    @PreAuthorize("hasAuthority('WRITE') and @tilgangsmaskin.harTilgang(#sak.fnr)")
    @Transactional
    fun opprettOppgave(
        type: OppgaveType,
        sak: Sak,
        beskrivelse: String? = null,
        tilordneTil: NavIdent? = null,
        behandlesAvApplikasjon: String? = APP_NAVN,
        journalpostId: EksternJournalpostId? = null
    ): OppgaveMedSak {
        val gjelder = sak.type.tilOppgaveGjelder()
        val oppgave = oppgaveClient.opprettOppgave(
            OpprettOppgaveRequest(
                tema = sak.tema.kode,
                oppgavetype = type.oppgavetype,
                opprettetAvEnhetsnr = finnOpprettetAvEnhet(sak)?.value,
                journalpostId = journalpostId,
                beskrivelse = beskrivelse,
                personident = sak.fnr.value,
                saksreferanse = sak.saksnummer.value,
                behandlesAvApplikasjon = behandlesAvApplikasjon,
                behandlingstype = gjelder.behandlingstype,
                behandlingstema = gjelder.behandlingstema,
                fristFerdigstillelse = LocalDate.now().plusDays(5),
            )
        )

        knyttOppgaveTilSak(sak.saksnummer, oppgave.id, type)

        if (tilordneTil != null) {
            tilordne(oppgave, tilordneTil)
        }
        logger.info("Oppretter oppgave {}:{} for sak {}", type, oppgave.id, sak.saksnummer)
        return oppgave.toOppgaveMedSak(sak)

    }

    @PreAuthorize("hasAuthority('WRITE')")
    fun tilordneOppgave(oppgaveId: EksternOppgaveId, tilordneTil: NavIdent) {
        val dto = oppgaveClient.hentOppgave(oppgaveId)
            ?: throw IkkeFunnetException("Fant ikke oppgave med id $oppgaveId")
        if (dto.tilordnetRessurs != null) {
            logger.warn("Oppgave {} er allerede tilordnet {}, kan ikke tilordne til {}", oppgaveId, dto.tilordnetRessurs, tilordneTil)
            return
        }
        tilordne(dto, tilordneTil)
    }

    private fun tilordne(oppgave: OppgaveDto, tilordneTil: NavIdent) {
        oppgaveClient.patchOppgave(
            oppgaveId = oppgave.id,
            request = PatchOppgaveRequest(
                versjon = oppgave.versjon,
                tilordnetRessurs = tilordneTil
            )
        )
    }

    private fun finnOpprettetAvEnhet(sak: Sak): Enhetsnummer? {
        val authenticatedUser = getAuthenticatedUser()
        if (authenticatedUser.systemUser) {
            return null
        }
        val enhet = sak.enhet
        val brukersEnheter = navAnsattService.hentNavAnsatt().enheter.map { it.enhetnummer }

        if (brukersEnheter.contains(enhet)) {
            return enhet
        } else {
            val fallbackEnhet = brukersEnheter.firstOrNull()
            logger.warn("Bruker har ikke tilgang til sakens enhet {}, tildeler oppgave til enhet {} i stedet", enhet, fallbackEnhet)
            return fallbackEnhet
        }
    }
}
