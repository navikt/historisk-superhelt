package no.nav.historisk.superhelt.klage.kafka

import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.klage.KabalEventRepository
import no.nav.historisk.superhelt.klage.utfall
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
    private val kabalEventRepository: KabalEventRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun behandleEvent(event: BehandlingEvent) {
        if (kabalEventRepository.erAlleredeProsessert(event)) {
            logger.warn(
                "Kabal-event {} (type={}) er allerede prosessert – ignorerer duplikat.",
                event.eventId, event.type
            )
            return
        }

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

        kabalEventRepository.lagre(event, saksnummer.value)

        logger.info(
            "Prosesserer Kabal-event {} (type={}, utfall={}) for sak {}",
            event.eventId, event.type, event.utfall(), saksnummer
        )

        when (event.type) {
            BehandlingEventType.KLAGEBEHANDLING_AVSLUTTET -> {
                if (sak.status.isFinal()) {
                    logger.error(
                        "Mottatt KLAGEBEHANDLING_AVSLUTTET-event på sak {} med status {} – " +
                            "event kan være lest fra tidligere. Oppretter ikke oppgave.",
                        saksnummer, sak.status
                    )
                } else {
                    val detaljer = event.detaljer.klagebehandlingAvsluttet
                        ?: error("KLAGEBEHANDLING_AVSLUTTET mangler detaljer for event ${event.eventId}")
                    opprettOppgave(
                        event = event,
                        saksnummer = saksnummer,
                        beskrivelse = "Klagebehandling avsluttet i Kabal. " +
                            "Utfall: ${detaljer.utfall}. " +
                            "Avsluttet: ${detaljer.avsluttet.toLocalDate()}.",
                    )
                }
            }

            BehandlingEventType.ANKEBEHANDLING_AVSLUTTET -> {
                val detaljer = event.detaljer.ankebehandlingAvsluttet
                    ?: error("ANKEBEHANDLING_AVSLUTTET mangler detaljer for event ${event.eventId}")
                opprettOppgave(
                    event = event,
                    saksnummer = saksnummer,
                    beskrivelse = "Ankebehandling avsluttet i Kabal. " +
                        "Utfall: ${detaljer.utfall}. " +
                        "Avsluttet: ${detaljer.avsluttet.toLocalDate()}.",
                )
            }

            BehandlingEventType.BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET -> {
                val detaljer = event.detaljer.behandlingEtterTrygderettenOpphevetAvsluttet
                    ?: error("BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET mangler detaljer for event ${event.eventId}")
                opprettOppgave(
                    event = event,
                    saksnummer = saksnummer,
                    beskrivelse = "Behandling etter Trygderetten opphevet er avsluttet i Kabal. " +
                        "Utfall: ${detaljer.utfall}. " +
                        "Avsluttet: ${detaljer.avsluttet.toLocalDate()}.",
                )
            }

            BehandlingEventType.OMGJOERINGSKRAVBEHANDLING_AVSLUTTET -> {
                val detaljer = event.detaljer.omgjoeringskravbehandlingAvsluttet
                    ?: error("OMGJOERINGSKRAVBEHANDLING_AVSLUTTET mangler detaljer for event ${event.eventId}")
                opprettOppgave(
                    event = event,
                    saksnummer = saksnummer,
                    beskrivelse = "Omgjøringskravbehandling avsluttet i Kabal. " +
                        "Utfall: ${detaljer.utfall}. " +
                        "Avsluttet: ${detaljer.avsluttet.toLocalDate()}.",
                )
            }

            BehandlingEventType.GJENOPPTAKSBEHANDLING_AVSLUTTET -> {
                val detaljer = event.detaljer.gjenopptaksbehandlingAvsluttet
                    ?: error("GJENOPPTAKSBEHANDLING_AVSLUTTET mangler detaljer for event ${event.eventId}")
                opprettOppgave(
                    event = event,
                    saksnummer = saksnummer,
                    beskrivelse = "Gjenopptaksbehandling avsluttet i Kabal. " +
                        "Utfall: ${detaljer.utfall}. " +
                        "Avsluttet: ${detaljer.avsluttet.toLocalDate()}.",
                )
            }

            BehandlingEventType.BEHANDLING_FEILREGISTRERT -> {
                val detaljer = event.detaljer.behandlingFeilregistrert
                    ?: error("BEHANDLING_FEILREGISTRERT mangler detaljer for event ${event.eventId}")
                logger.info(
                    "Markerer sak {} som feilregistrert etter Kabal-event {}. " +
                        "Registrert av: {}, årsak: {}",
                    saksnummer, event.eventId, detaljer.navIdent, detaljer.reason
                )
                sakRepository.feilregistrerSak(saksnummer)
            }

            BehandlingEventType.ANKEBEHANDLING_OPPRETTET,
            BehandlingEventType.ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET,
            -> {
                // Ingen aksjon – vi logger bare at vi har mottatt eventet
                logger.info(
                    "Mottatt Kabal-event {} av type {} for sak {} – ingen aksjon kreves.",
                    event.eventId, event.type, saksnummer
                )
            }
        }
    }

    private fun opprettOppgave(event: BehandlingEvent, saksnummer: Saksnummer, beskrivelse: String) {
        val sak = sakRepository.getSak(saksnummer)
        logger.info("Oppretter oppgave for sak {} etter Kabal-event {} ({})", saksnummer, event.eventId, event.type)
        oppgaveService.opprettOppgave(
            type = OppgaveType.VUR_KONS_YTE,
            sak = sak,
            beskrivelse = beskrivelse,
            behandlesAvApplikasjon = "SUPERHELT",
        )
    }
}

