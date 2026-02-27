package no.nav.historisk.superhelt.sak.rest

import no.nav.common.types.Belop
import no.nav.common.types.FolkeregisterIdent
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.person.TilgangsmaskinTestData
import no.nav.historisk.superhelt.person.tilgangsmaskin.TilgangsmaskinService
import no.nav.historisk.superhelt.person.toMaskertPersonIdent
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.WithLeseBruker
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.historisk.superhelt.test.bodyAsProblemDetail
import no.nav.historisk.superhelt.utbetaling.UtbetalingRepository
import no.nav.historisk.superhelt.utbetaling.UtbetalingStatus
import no.nav.historisk.superhelt.utbetaling.UtbetalingsType
import no.nav.tilgangsmaskin.TilgangsmaskinClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester
import tools.jackson.databind.ObjectMapper

@MockedSpringBootTest
@AutoConfigureMockMvc
class SakControllerRestTest() {

    @Autowired
    private lateinit var mockMvc: MockMvcTester

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var repository: SakRepository

    @Autowired
    private lateinit var utbetalingRepository: UtbetalingRepository

    @MockitoBean
    private lateinit var tilgangsmaskinService: TilgangsmaskinService

    @BeforeEach
    fun setup() {
        whenever(tilgangsmaskinService.sjekkKomplettTilgang(any())) doReturn TilgangsmaskinClient.TilgangResult(
            harTilgang = true
        )
    }

    @WithSaksbehandler
    @Nested
    inner class `oppdater sak` {

        @Test
        fun `oppdater sak ok`() {
            val opprettetSak = SakTestData.lagreNySak(repository, SakTestData.nySakMinimum())
            val saksnummer = opprettetSak.saksnummer
            val oppdatertTittel = "Ny tittel"
            val oppdatertBegrunnelse = "Ny begrunnelse"

            assertThat(
                oppdaterSak(
                    saksnummer, SakUpdateRequestDto(
                        beskrivelse = oppdatertTittel,
                        begrunnelse = oppdatertBegrunnelse
                    )
                )
            )
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(Sak::class.java)
                .satisfies({
                    assertThat(it.saksnummer).isEqualTo(saksnummer)
                    assertThat(it.beskrivelse).isEqualTo(oppdatertTittel)
                    assertThat(it.begrunnelse).isEqualTo(oppdatertBegrunnelse)
                    assertThat(it.fnr).isNotNull
                })
            verify(tilgangsmaskinService, atLeast(1)).sjekkKomplettTilgang(opprettetSak.fnr)
        }

        @WithLeseBruker
        @Test
        fun `oppdater sak uten skrivetilgang skal gi feil`() {
            val opprettetSak = SakTestData.lagreNySak(repository, SakTestData.nySakMinimum()).saksnummer
            assertThat(
                oppdaterSak(
                    opprettetSak, SakUpdateRequestDto(
                        beskrivelse = "Ny tittel",
                        begrunnelse = "Ny begrunnelse"
                    )
                )
            )
                .hasStatus4xxClientError()
                .bodyAsProblemDetail()
                .satisfies({
                    assertThat(it?.detail).isNotBlank
                })
        }

        @Test
        fun `oppdater sak som ikke finnes skal gi feil`() {
            val ikkeFinnsSaksnummer = Saksnummer(999999)

            assertThat(
                oppdaterSak(
                    ikkeFinnsSaksnummer, SakUpdateRequestDto(
                        beskrivelse = "Ny tittel",
                        begrunnelse = "Ny begrunnelse"
                    )
                )
            )
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyAsProblemDetail()
        }

        private fun oppdaterSak(
            saksnummer: Saksnummer?,
            dto: SakUpdateRequestDto
        ): MockMvcTester.MockMvcRequestBuilder = mockMvc.put().uri("/api/sak/{saksnummer}", saksnummer)
            .with(csrf())
            .contentType("application/json")
            .content(
                objectMapper.writeValueAsString(dto)
            )
    }


    @WithSaksbehandler
    @Nested
    inner class `hent sak` {

        @Test
        fun `hent sak ok sjekk json`() {
            val opprettetSak = SakTestData.lagreNySak(repository, SakTestData.nySakMinimum())

            assertThat(hentSak(opprettetSak.saksnummer))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPathSatisfying("$.saksnummer") { assertThat(it).isEqualTo(opprettetSak.saksnummer.value) }
                .hasPathSatisfying("$.fnr") { assertThat(it).isEqualTo(opprettetSak.fnr.value) }
                .hasPathSatisfying("$.type") { assertThat(it).isEqualTo(opprettetSak.type.name) }
                .hasPathSatisfying("$.beskrivelse") { assertThat(it).isEqualTo(opprettetSak.beskrivelse) }
                // Genererte verdier
                .hasPathSatisfying("$.maskertPersonIdent") { assertThat(it).isEqualTo(opprettetSak.fnr.toMaskertPersonIdent().value) }
                .hasPathSatisfying("$.rettigheter") { assertThat(it).isNotEmpty }
                .hasPathSatisfying("$.tilstand") { assertThat(it).isNotEmpty }
                .hasPathSatisfying("$.valideringsfeil") { assertThat(it).isNotNull }


            verify(tilgangsmaskinService, atLeast(1)).sjekkKomplettTilgang(opprettetSak.fnr)
        }

        @Test
        fun `hent sak som ikke finnes skal gi feil`() {
            val ikkeFinnsSaksnummer = Saksnummer(999999)

            assertThat(hentSak(ikkeFinnsSaksnummer))
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyAsProblemDetail()
        }

        @Test
        fun `hent sak uten tilgang til person skal gi feil`() {
            val opprettetSak = SakTestData.lagreNySak(repository, SakTestData.nySakMinimum())
            whenever(tilgangsmaskinService.sjekkKomplettTilgang(opprettetSak.fnr)) doReturn TilgangsmaskinClient.TilgangResult(
                harTilgang = false,
                TilgangsmaskinTestData.problemDetailResponse,
            )

            assertThat(hentSak(opprettetSak.saksnummer))
                .hasStatus(HttpStatus.FORBIDDEN)
                .bodyAsProblemDetail()
        }

        private fun hentSak(saksnummer: Saksnummer?): MockMvcTester.MockMvcRequestBuilder =
            mockMvc.get().uri("/api/sak/{saksnummer}", saksnummer)
    }

    @WithSaksbehandler
    @Nested
    inner class `finn saker for person` {


        @Test
        fun `finn saker for person ok`() {
            val fnr = FolkeregisterIdent("12345678901")
            SakTestData.lagreNySak(repository, SakTestData.nySakMinimum(fnr))
            SakTestData.lagreNySak(repository, SakTestData.nySakMinimum(fnr))
            SakTestData.lagreNySak(repository, SakTestData.nySakMinimum(FolkeregisterIdent("98765432101")))

            assertThat(finnSakerForPerson(fnr))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(List::class.java)
                .satisfies({
                    assertThat(it).hasSize(2)
                })

            verify(tilgangsmaskinService, atLeast(1)).sjekkKomplettTilgang(fnr)
        }

        @WithMockUser()
        @Test
        fun `finn saker for saksbehandler uten lesetilgang skal gi feil`() {
            val fnr = FolkeregisterIdent("22345678901")
            SakTestData.lagreNySak(repository, SakTestData.nySakMinimum(fnr))
            assertThat(finnSakerForPerson(fnr))
                .hasStatus(HttpStatus.FORBIDDEN)

        }

        @Test
        fun `finn saker for saksbehandler uten rettighet for person skal gi feil`() {
            val fnr = FolkeregisterIdent("32345678901")
            SakTestData.lagreNySak(repository, SakTestData.nySakMinimum(fnr))
            whenever(tilgangsmaskinService.sjekkKomplettTilgang(fnr)) doReturn TilgangsmaskinClient.TilgangResult(
                harTilgang = false,
                TilgangsmaskinTestData.problemDetailResponse,
            )
            assertThat(finnSakerForPerson(fnr))
                .hasStatus(HttpStatus.FORBIDDEN)
                .bodyAsProblemDetail()
        }

        private fun finnSakerForPerson(fnr: FolkeregisterIdent): MockMvcTester.MockMvcRequestBuilder =
            mockMvc.get().uri("/api/sak")
                .queryParam("maskertPersonId", fnr.toMaskertPersonIdent().value)
    }

    @WithSaksbehandler
    @Nested
    inner class `oppdater utbetaling på sak` {

        @Test
        fun `sett utbetalingstype BRUKER og belop`() {
            val sak = SakTestData.lagreNySak(repository, SakTestData.nySakMinimum())
            val belop = Belop(5000)

            assertThat(
                oppdaterSak(sak.saksnummer, SakUpdateRequestDto(utbetalingsType = UtbetalingsType.BRUKER, belop = belop))
            )
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(Sak::class.java)
                .satisfies({
                    assertThat(it.utbetalingsType).isEqualTo(UtbetalingsType.BRUKER)
                    assertThat(it.belop).isEqualTo(belop)
                })
        }

        @Test
        fun `sett utbetalingstype INGEN fjerner belop`() {
            val sak = SakTestData.lagreNySak(repository, SakTestData.nySakCompleteUtbetaling())

            assertThat(
                oppdaterSak(sak.saksnummer, SakUpdateRequestDto(utbetalingsType = UtbetalingsType.INGEN))
            )
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(Sak::class.java)
                .satisfies({
                    assertThat(it.utbetalingsType).isEqualTo(UtbetalingsType.INGEN)
                    assertThat(it.belop).isNull()
                })
        }

        @Test
        fun `oppdater belop uten å endre andre felt`() {
            val sak = SakTestData.lagreNySak(repository, SakTestData.nySakCompleteUtbetaling())
            val nyttBelop = Belop(12345)

            assertThat(
                oppdaterSak(sak.saksnummer, SakUpdateRequestDto(utbetalingsType = UtbetalingsType.BRUKER, belop = nyttBelop))
            )
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(Sak::class.java)
                .satisfies({
                    assertThat(it.saksnummer).isEqualTo(sak.saksnummer)
                    assertThat(it.beskrivelse).isEqualTo(sak.beskrivelse)
                    assertThat(it.belop).isEqualTo(nyttBelop)
                    assertThat(it.utbetalingsType).isEqualTo(UtbetalingsType.BRUKER)
                })
        }

        private fun oppdaterSak(saksnummer: Saksnummer, dto: SakUpdateRequestDto) =
            mockMvc.put().uri("/api/sak/{saksnummer}", saksnummer)
                .with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(dto))
    }

    @WithSaksbehandler
    @Nested
    inner class `hent sak status` {

        @Test
        fun `sak uten utbetaling gir OK aggregert status`() {
            val sak = SakTestData.lagreNySak(repository, SakTestData.nySakMinimum())

            assertThat(hentSakStatus(sak.saksnummer))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPathSatisfying("$.sakStatus") { assertThat(it).isEqualTo("UNDER_BEHANDLING") }
                .hasPathSatisfying("$.utbetalingStatus") { assertThat(it).isNull() }
                .hasPathSatisfying("$.brevStatus") { assertThat(it).isNull() }
                .hasPathSatisfying("$.aggregertStatus") { assertThat(it).isEqualTo("OK") }
        }

        @Test
        fun `sak med utbetaling under behandling gir OK aggregert status`() {
            val sak = SakTestData.lagreNySak(repository, SakTestData.nySakCompleteUtbetaling())
            utbetalingRepository.opprettUtbetaling(sak)

            assertThat(hentSakStatus(sak.saksnummer))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPathSatisfying("$.utbetalingStatus") { assertThat(it).isEqualTo("UTKAST") }
                .hasPathSatisfying("$.aggregertStatus") { assertThat(it).isEqualTo("OK") }
        }

        @Test
        fun `sak med feilet utbetaling gir FEILET aggregert status`() {
            val sak = SakTestData.lagreNySak(repository, SakTestData.nySakCompleteUtbetaling())
            val utbetaling = utbetalingRepository.opprettUtbetaling(sak)
            utbetalingRepository.setUtbetalingStatus(utbetaling.transaksjonsId, UtbetalingStatus.FEILET)

            assertThat(hentSakStatus(sak.saksnummer))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPathSatisfying("$.utbetalingStatus") { assertThat(it).isEqualTo("FEILET") }
                .hasPathSatisfying("$.aggregertStatus") { assertThat(it).isEqualTo("FEILET") }
        }

        @Test
        fun `sak som ikke finnes gir 404`() {
            assertThat(hentSakStatus(Saksnummer(999999)))
                .hasStatus(HttpStatus.NOT_FOUND)
        }

        @WithLeseBruker
        @Test
        fun `lesebruker kan hente sak status`() {
            val sak = SakTestData.lagreNySak(repository, SakTestData.nySakMinimum())

            assertThat(hentSakStatus(sak.saksnummer))
                .hasStatus(HttpStatus.OK)
        }

        private fun hentSakStatus(saksnummer: Saksnummer) =
            mockMvc.get().uri("/api/sak/{saksnummer}/status", saksnummer)
    }
}
