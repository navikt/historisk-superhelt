package no.nav.historisk.superhelt.klage

import no.nav.historisk.superhelt.enhet.NavEnhetService
import no.nav.historisk.superhelt.infrastruktur.validation.ValidationFieldError
import no.nav.historisk.superhelt.infrastruktur.validation.ValideringException
import no.nav.historisk.superhelt.klage.rest.SendKlageRequestDto
import no.nav.historisk.superhelt.sak.Sak
import no.nav.kabal.KabalClient

import no.nav.kabal.model.Fagsak
import no.nav.kabal.model.Hjemmel
import no.nav.kabal.model.Ident
import no.nav.kabal.model.IdentType
import no.nav.kabal.model.Klager
import no.nav.kabal.model.SakType
import no.nav.kabal.model.SakenGjelder
import no.nav.kabal.model.SendSakV4Request
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class KlageService(
    private val kabalClient: KabalClient,
    private val navEnhetService: NavEnhetService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PreAuthorize("hasAuthority('WRITE')")
    fun sendKlage(sak: Sak, request: SendKlageRequestDto) {
        val hjemmel = try {
            Hjemmel.fromId(request.hjemmelId)
        } catch (e: IllegalArgumentException) {
            throw ValideringException(
                reason = "Ugyldig hjemmelId '${request.hjemmelId}'. Bruk GET /api/sak/kodeverk/hjemler for gyldige verdier.",
                cause = e,
                validationErrors = listOf(ValidationFieldError("hjemmelId", "Ukjent hjemmelId: ${request.hjemmelId}")),
            )
        }
        val enhet = navEnhetService.hentNavEnhet()

        val kabalRequest = SendSakV4Request(
            type = SakType.KLAGE,
            sakenGjelder = SakenGjelder(id = Ident(type = IdentType.PERSON, verdi = sak.fnr.value)),
            klager = Klager(id = Ident(type = IdentType.PERSON, verdi = sak.fnr.value)),
            fagsak = Fagsak(fagsakId = sak.saksnummer.value, fagsystem = "HJELPEMIDLER"),
            kildeReferanse = sak.saksnummer.value,
            dvhReferanse = sak.saksnummer.value,
            hjemler = listOf(hjemmel.id),
            forrigeBehandlendeEnhet = enhet.value,
            tilknyttedeJournalposter = emptyList(),
            brukersKlageMottattVedtaksinstans = request.datoKlageMottatt,
            ytelse = "HEL_HEL",
            kommentar = request.kommentar,
        )

        logger.info("Sender klage til Kabal for sak ${sak.saksnummer}, hjemmel: ${hjemmel.id}, enhet: ${enhet.value}")
        kabalClient.sendSakV4(kabalRequest)
        logger.info("Klage sendt til Kabal for sak ${sak.saksnummer}")
    }
}
