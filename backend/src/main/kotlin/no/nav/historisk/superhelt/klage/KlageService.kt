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
import java.time.Instant

@Service
class KlageService(
    private val kabalClient: KabalClient,
    private val navEnhetService: NavEnhetService,
    private val klageRepository: KlageRepository,
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
        val sendtTidspunkt = Instant.now()

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
        kabalClient.sendSakV4(kabalRequest)  // kastar KabalException ved feil – DB-skriv skjer ALDRI då
        logger.info("Klage sendt til Kabal for sak ${sak.saksnummer}")

        // Logg klagen i DB berre etter vellykka Kabal-kall.
        // Dersom DB-skrivet feiler etter at Kabal har mottatt klagen, loggar vi feilen
        // men returnerer likevel suksess – klagen er allereie registrert hos Kabal.
        try {
            klageRepository.lagreKlage(
                saksnummer = sak.saksnummer,
                hjemmelId = hjemmel.id,
                datoKlageMottatt = request.datoKlageMottatt,
                kommentar = request.kommentar,
                forrigeBehandlendeEnhet = enhet.value,
                sendtTidspunkt = sendtTidspunkt,
            )
        } catch (e: Exception) {
            logger.error(
                "Klage for sak {} vart sendt til Kabal men kunne ikkje lagrast i DB – " +
                        "manuell oppfølging kan vere nødvendig. Feil: {}",
                sak.saksnummer, e.message, e,
            )
        }
    }
}
