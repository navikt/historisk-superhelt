package no.nav.historisk.superhelt.klage

import no.nav.historisk.superhelt.klage.db.KabalEventEntity
import no.nav.historisk.superhelt.klage.db.KabalEventJpaRepository
import no.nav.kabal.model.BehandlingEvent
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * Lagrer og slår opp Kabal-events for idempotens og revisjon.
 */
@Repository
class KabalEventRepository(
    private val jpaRepository: KabalEventJpaRepository,
) {

    /**
     * Lagrer event i en egen transaksjon (REQUIRES_NEW).
     * Returnerer true hvis eventet ble lagret (nytt), false hvis det var et duplikat.
     * Sjekker eksistens først for å unngå constraint-violation som setter Hibernate-sesjonen
     * i rollback-only-tilstand og dermed forhindrer at unntaket kan håndteres av kalleren.
     * REQUIRES_NEW sikrer at duplikat-sjekk ikke forstyrrer den ytre transaksjonen.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun lagre(event: BehandlingEvent, saksnummer: String): Boolean {
        if (jpaRepository.existsByEventId(event.eventId)) return false
        jpaRepository.save(
            KabalEventEntity(
                eventId = event.eventId,
                saksnummer = saksnummer,
                eventType = event.type.name,
                utfall = event.utfall(),
                tidspunkt = event.tidspunkt(),
                aarsakFeilregistrert = event.detaljer.behandlingFeilregistrert?.reason,
                journalpostReferanser = event.journalpostReferanser().joinToString(","),
            )
        )
        return true
    }
}

