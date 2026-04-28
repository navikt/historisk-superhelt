package no.nav.historisk.superhelt.klage.kafka

import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.oppgave.OppgaveService
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.kabal.model.BehandlingEvent
import no.nav.kabal.model.BehandlingEventType
import no.nav.oppgave.OppgaveType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class KlageEventService(
    private val sakRepository: SakRepository,
    private val oppgaveService: OppgaveService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun behandleEvent(event: BehandlingEvent) {
        val saksnummer = runCatching { Saksnummer(event.kildeReferanse) }.getOrElse {
            logger.error("Ugyldig kildeReferanse '{}' i BehandlingEvent {}", event.kildeReferanse, event.eventId)
            return
        }

        val sak = runCatching { sakRepository.getSak(saksnummer) }.getOrElse {
            logger.error(
                "Fant ikke sak {} fra Kabal-event {} (type={}). Ignorerer.",
                saksnummer, event.eventId, event.type
            )
            return
        }

        val beskrivelse = buildBeskrivelse(event)
        if (beskrivelse == null) {
            logger.info("Ignorerer Kabal-event {} av type {} for sak {}", event.eventId, event.type, saksnummer)
            return
        }

        logger.info("Oppretter oppgave for sak {} etter Kabal-event {} ({})", saksnummer, event.eventId, event.type)
        // send to change log also

        // only create oppgave on certain event types, to avoid creating too many oppgaver for every little change in Kabal
        oppgaveService.opprettOppgave(
            type = OppgaveType.VUR_KONS_YTE,
            sak = sak,
            beskrivelse = beskrivelse,
            behandlesAvApplikasjon = "SUPERHELT",
        )
    }

    private fun buildBeskrivelse(event: BehandlingEvent): String? =
        when (event.type) {
            BehandlingEventType.KLAGEBEHANDLING_AVSLUTTET -> {
                val detaljer = event.detaljer.klagebehandlingAvsluttet
                    ?: return "Klagebehandling avsluttet i Kabal (detaljer mangler)."
                "Klagebehandling avsluttet i Kabal. Utfall: ${detaljer.utfall}. " +
                    "Avsluttet: ${detaljer.avsluttet.toLocalDate()}."
            }

            BehandlingEventType.BEHANDLING_FEILREGISTRERT -> {
                val detaljer = event.detaljer.behandlingFeilregistrert
                    ?: return "Behandling feilregistrert i Kabal (detaljer mangler)."
                "Behandling feilregistrert i Kabal av ${detaljer.navIdent}. Årsak: ${detaljer.reason}."
            }

            BehandlingEventType.ANKEBEHANDLING_OPPRETTET -> {
                val detaljer = event.detaljer.ankebehandlingOpprettet
                    ?: return "Ankebehandling opprettet i Kabal (detaljer mangler)."
                "Ankebehandling opprettet i Kabal. Mottatt klageinstans: ${detaljer.mottattKlageinstans.toLocalDate()}."
            }

            BehandlingEventType.ANKEBEHANDLING_AVSLUTTET -> {
                val detaljer = event.detaljer.ankebehandlingAvsluttet
                    ?: return "Ankebehandling avsluttet i Kabal (detaljer mangler)."
                "Ankebehandling avsluttet i Kabal. Utfall: ${detaljer.utfall}. " +
                    "Avsluttet: ${detaljer.avsluttet.toLocalDate()}."
            }

            else -> {
                logger.debug("Ubehandlet Kabal-event-type {} for eventId {}", event.type, event.eventId)
                null
            }
        }
}

