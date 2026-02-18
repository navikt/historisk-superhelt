package no.nav.historisk.superhelt.test

import no.nav.common.types.EksternJournalpostId
import no.nav.dokarkiv.DokarkivClient
import no.nav.dokarkiv.JournalpostResponse
import no.nav.dokdist.DistribuerJournalpostResponse
import no.nav.dokdist.DokdistClient
import no.nav.historisk.superhelt.brev.pdfgen.PdfgenService
import no.nav.historisk.superhelt.infrastruktur.token.NaisTokenService
import no.nav.historisk.superhelt.person.tilgangsmaskin.TilgangsmaskinService
import no.nav.pdl.*
import no.nav.tilgangsmaskin.TilgangsmaskinClient
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

/**
 * Mock av eksterne tjenester. Bruk MockitoBean i testene for å overstyre disse mockene i spesifikke tester.
 */
@TestConfiguration
class ExternalMockTestConfig {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @Primary
    @Bean
    fun tilgangsmaskinServiceMock(): TilgangsmaskinService {
        val mock = mock<TilgangsmaskinService>()
        logger.warn("Bruker mock av tilgangsmaskin")
        mock.stub {
            on { sjekkKomplettTilgang(any()) } doReturn TilgangsmaskinClient.TilgangResult(
                harTilgang = true,
                response = null
            )
        }
        return mock
    }

    @Primary
    @Bean
    fun pdlClientMock(): PdlClient {
        logger.warn("Bruker mock av pdl")
        return mock<PdlClient>().stub {
            on { getPersonOgIdenter(any()) } doReturn HentPdlResponse(
                data = PdlData(
                    hentPerson = Person(
                        navn = listOf(Navn("Ola", null, "Nordmann")),
                        doedsfall = emptyList(),
                        foedselsdato = emptyList(),
                        adressebeskyttelse = emptyList(),
                        vergemaalEllerFremtidsfullmakt = emptyList()
                    ),
                    hentIdenter = Identliste(
                        identer = listOf(
                            IdentInformasjon("10987654321", IdentGruppe.FOLKEREGISTERIDENT, true),
                            IdentInformasjon("1234567890123", IdentGruppe.AKTORID, false)
                        )
                    )
                ),
                errors = null
            )
        }
    }

    @Primary
    @Bean
    fun pdfgenServiceMock(): PdfgenService {
        logger.warn("Bruker mock av Pdfgen")
        return mock<PdfgenService>().stub {
            on { genererPdf(any(), any()) } doReturn ByteArray(5)
            on { genererHtml(any(), any()) } doReturn "<html><body>Test</body></html>".toByteArray()
        }
    }

    @Primary
    @Bean
    fun naisTokenserviceMock(): NaisTokenService {
        logger.warn("Bruker mock av NaisTokenService")
        return mock<NaisTokenService>().stub {
            on { oboToken(any()) } doReturn "mocked-token"
            on { m2mToken(any()) } doReturn "mocked-token"
        }
    }

    @Primary
    @Bean
    fun dokarkivClientMock(): DokarkivClient {
        logger.warn("Bruker mock av DokarkivClient")
        return mock<DokarkivClient>().stub {
            on { opprett(any(), any()) } doReturn JournalpostResponse(
                dokumenter = emptyList(),
                journalpostId = EksternJournalpostId("mock-journalpost-id"),
                journalpostferdigstilt = true
            )

        }
    }

    @Primary
    @Bean
    fun dokDistClientMock(): DokdistClient {
        logger.warn("Bruker mock av DokdistClient")
        return mock<DokdistClient>().stub {
            on { distribuerJournalpost(any()) } doReturn DistribuerJournalpostResponse(
                bestillingsId = "bestillingsId"
            )

        }
    }
}