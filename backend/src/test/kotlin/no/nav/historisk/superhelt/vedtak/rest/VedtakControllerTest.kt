package no.nav.historisk.superhelt.vedtak.rest

import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.person.TilgangsmaskinTestData
import no.nav.historisk.superhelt.person.tilgangsmaskin.TilgangsmaskinService
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.WithLeseBruker
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.historisk.superhelt.test.withMockedUser
import no.nav.historisk.superhelt.vedtak.VedtakRepository
import no.nav.historisk.superhelt.vedtak.VedtakTestData
import no.nav.tilgangsmaskin.TilgangsmaskinClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester

@MockedSpringBootTest
@AutoConfigureMockMvc
@WithSaksbehandler
class VedtakControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvcTester

    @Autowired
    private lateinit var sakRepository: SakRepository

    @Autowired
    private lateinit var vedtakRepository: VedtakRepository

    @MockitoBean
    private lateinit var tilgangsmaskinService: TilgangsmaskinService

    @BeforeEach
    fun setup() {
        whenever(tilgangsmaskinService.sjekkKomplettTilgang(any())) doReturn TilgangsmaskinClient.TilgangResult(
            harTilgang = true
        )
    }

    @Test
    fun `hentVedtakForSak returnerer tom liste når ingen vedtak finnes`() {
        val sak = withMockedUser { SakTestData.lagreNySak(sakRepository) }

        assertThat(hentVedtak(sak.saksnummer))
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .convertTo(List::class.java)
            .satisfies({ assertThat(it).isEmpty() })
    }

    @Test
    fun `hentVedtakForSak returnerer vedtak for saken`() {
        val sak = withMockedUser { SakTestData.lagreNySak(sakRepository, SakTestData.nySakCompleteUtbetaling()) }
        val vedtak = VedtakTestData.vedtakForSak(sak)
        withMockedUser { vedtakRepository.save(vedtak) }

        assertThat(hentVedtak(sak.saksnummer))
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .hasPathSatisfying("$[0].saksnummer") { assertThat(it).isEqualTo(sak.saksnummer.value) }
            .hasPathSatisfying("$[0].resultat") { assertThat(it).isEqualTo(vedtak.resultat.name) }
    }

    @Test
    fun `hentVedtakForSak gir 404 for ukjent saksnummer`() {
        assertThat(hentVedtak(Saksnummer(999999)))
            .hasStatus(HttpStatus.NOT_FOUND)
    }

    @Test
    @WithLeseBruker
    fun `hentVedtakForSak er tilgjengelig for lesebruker`() {
        val sak = withMockedUser { SakTestData.lagreNySak(sakRepository) }

        assertThat(hentVedtak(sak.saksnummer))
            .hasStatus(HttpStatus.OK)
    }

    private fun hentVedtak(saksnummer: Saksnummer) =
        mockMvc.get().uri("/api/sak/{saksnummer}/vedtak", saksnummer)
}
