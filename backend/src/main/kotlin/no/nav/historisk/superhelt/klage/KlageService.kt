package no.nav.historisk.superhelt.klage

import no.nav.common.consts.APP_NAVN
import no.nav.common.types.Enhetsnummer
import no.nav.historisk.superhelt.ansatt.NavAnsattService
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
class KlageService(private val kabalClient: KabalClient, private val navAnsattService: NavAnsattService) {
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
        val enhet = finnEnhet(request.enhet)
        val kabalRequest = SendSakV4Request(
            type = SakType.KLAGE,
            sakenGjelder = SakenGjelder(id = Ident(type = IdentType.PERSON, verdi = sak.fnr.value)),
            klager = Klager(id = Ident(type = IdentType.PERSON, verdi = sak.fnr.value)),
            fagsak = Fagsak(fagsakId = sak.saksnummer.value, fagsystem = APP_NAVN),
            kildeReferanse = sak.saksnummer.value,
            dvhReferanse = sak.saksnummer.value,
            hjemler = listOf(hjemmel.id),
            forrigeBehandlendeEnhet = enhet.value,
            tilknyttedeJournalposter = emptyList(),
            brukersKlageMottattVedtaksinstans = request.datoKlageMottatt,
            ytelse = sak.type.kabalYtelse,
            kommentar = request.kommentar,
        )

        logger.info("Sender klage til Kabal for sak ${sak.saksnummer}, hjemmel: ${hjemmel.id}, enhet: ${enhet}")
        kabalClient.sendSakV4(kabalRequest)
        logger.info("Klage sendt til Kabal for sak ${sak.saksnummer}")
    }

    private fun finnEnhet(enhetsnummer: Enhetsnummer): Enhetsnummer {
        val brukersEnheter = navAnsattService.hentNavAnsatt().enheter.map { it.enhetnummer }
        if (!brukersEnheter.contains(enhetsnummer)) {
            throw ValideringException(
                reason = "Angitt enhet ${enhetsnummer.value} er ikke blant enhetene til innlogget saksbehandler.",
                validationErrors = listOf(ValidationFieldError("enhet", "Gyldige enheter: ${brukersEnheter.joinToString(", ")}")),
            )
        }
        return enhetsnummer
    }
}
