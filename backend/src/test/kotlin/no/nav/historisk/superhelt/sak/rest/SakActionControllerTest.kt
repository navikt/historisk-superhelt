package no.nav.historisk.superhelt.sak.rest

import no.nav.historisk.superhelt.endringslogg.EndringsloggService
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.infrastruktur.validation.ValideringException
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.WithAttestant
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.historisk.superhelt.utbetaling.UtbetalingService
import no.nav.historisk.superhelt.vedtak.VedtakRepository
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.Test

@MockedSpringBootTest
@AutoConfigureMockMvc
class SakActionControllerTest() {

    @Autowired
    private lateinit var sakActionController: SakActionController

    @Autowired
    private lateinit var sakRepository: SakRepository

    @Autowired
    private lateinit var vedtakRepository: VedtakRepository

    @Autowired
    private lateinit var endringsloggService: EndringsloggService

    @MockitoBean
    private lateinit var utbetalingService: UtbetalingService

    @WithSaksbehandler(navIdent = "s12345")
    @Nested
    inner class `Send til attestering` {

        @Test
        fun `skal sende sak til attestering`() {
            val sak = SakTestData.lagreNySak(sakRepository, SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.UNDER_BEHANDLING))

            sakActionController.tilAttestering(sak.saksnummer)

            val sendtSak = sakRepository.getSak(sak.saksnummer)
            assertThat(sendtSak.status).isEqualTo(SakStatus.TIL_ATTESTERING)
            assertThat(sendtSak.attestant).isNull()

            val endringslogg = endringsloggService.findBySak(sak.saksnummer)
            assertThat(endringslogg)
                .anySatisfy {
                    assertThat(it.type).isEqualTo(EndringsloggType.TIL_ATTESTERING)
                    assertThat(it.endretAv.value).isEqualTo("s12345")
                }

        }

        @WithAttestant
        @Test
        fun `attestant skal ikke få sende til attestering`() {
            val sak = SakTestData.lagreNySak(sakRepository, SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.UNDER_BEHANDLING))

            assertThatThrownBy {
                sakActionController.tilAttestering(sak.saksnummer)
            }.isInstanceOf(ValideringException::class.java)
                .hasMessageContaining("Manglende rettighet")

            val sendtSak = sakRepository.getSak(sak.saksnummer)
            assertThat(sendtSak.status).isEqualTo(SakStatus.UNDER_BEHANDLING)
        }

        @Test
        fun `skal feile validering når saken ikke er under behandling`() {
            val sak = SakTestData.lagreNySak(sakRepository, SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.FERDIG))

            assertThatThrownBy {
                sakActionController.tilAttestering(sak.saksnummer)
            }.isInstanceOf(ValideringException::class.java)
                .hasMessageContaining("Ugyldig statusovergang")

            val sendtSak = sakRepository.getSak(sak.saksnummer)
            assertThat(sendtSak.status).isEqualTo(SakStatus.FERDIG)
        }

        @Test
        fun `skal feile validering når saken ikke er komplett`() {
            val sak = SakTestData.lagreNySak(sakRepository)

            assertThatThrownBy {
                sakActionController.tilAttestering(sak.saksnummer)
            }.isInstanceOf(ValideringException::class.java)
                .hasMessageContaining("Validering av sak feilet")

            val sendtSak = sakRepository.getSak(sak.saksnummer)
            assertThat(sendtSak.status).isEqualTo(SakStatus.UNDER_BEHANDLING)
        }
    }

    @WithAttestant(navIdent = "a12345")
    @Nested
    inner class `Attester sak` {

        @Test
        fun `skal attestere sak med godkjent`() {
            val sak = SakTestData.lagreNySak(sakRepository, SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.TIL_ATTESTERING))

            sakActionController.attesterSak(sak.saksnummer, AttesterSakRequestDto(godkjent = true, kommentar = null))

            val ferdigstiltSak = sakRepository.getSak(sak.saksnummer)
            assertThat(ferdigstiltSak.status).isEqualTo(SakStatus.FERDIG)

            val vedtakForSak = vedtakRepository.findBySak(sak.saksnummer)
            assertThat(vedtakForSak)
                .singleElement()
                .satisfies({
                    assertThat(it.saksnummer).isEqualTo(sak.saksnummer)
                    assertThat(it.behandlingsnummer).isEqualTo(sak.behandlingsnummer)
                    assertThat(it.resultat).isEqualTo(sak.vedtaksResultat)
                    assertThat(it.attestant.navIdent.value).isEqualTo("a12345")
                    assertThat(it.vedtaksTidspunkt).isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS))
                })

            verify(utbetalingService).sendTilUtbetaling(any())

            val endringslogg = endringsloggService.findBySak(sak.saksnummer)
            assertThat(endringslogg)
                .anySatisfy {
                    assertThat(it.type).isEqualTo(EndringsloggType.ATTESTERT_SAK)
                    assertThat(it.endretAv.value).isEqualTo("a12345")
                }

        }

        @Test
        fun `skal attestere sak med avslag`() {
            val sak = SakTestData.lagreNySak(sakRepository, SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.TIL_ATTESTERING))

            sakActionController.attesterSak(sak.saksnummer, AttesterSakRequestDto(godkjent = false, kommentar = "Avslått av attestant"))

            val attestertSak = sakRepository.getSak(sak.saksnummer)
            assertThat(attestertSak.status).isEqualTo(SakStatus.UNDER_BEHANDLING)

            val endringslogg = endringsloggService.findBySak(sak.saksnummer)
            assertThat(endringslogg)
                .anySatisfy {
                    assertThat(it.type).isEqualTo(EndringsloggType.ATTESTERING_UNDERKJENT)
                    assertThat(it.endretAv.value).isEqualTo("a12345")
                }

        }

        @WithSaksbehandler
        @Test
        fun `saksbehandler skal ikke få attestere`() {
            val sak = SakTestData.lagreNySak(sakRepository, SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.TIL_ATTESTERING))

            assertThatThrownBy {
                sakActionController.attesterSak(sak.saksnummer, AttesterSakRequestDto(godkjent = true))
            }.isInstanceOf(ValideringException::class.java)
                .hasMessageContaining("Manglende rettighet")

            val attestertSak = sakRepository.getSak(sak.saksnummer)
            assertThat(attestertSak.status).isEqualTo(SakStatus.TIL_ATTESTERING)
        }

        @Test
        fun `attestant skal ikke få attestere sin egen sak`() {
            val sak = SakTestData.lagreNySak(sakRepository, SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.TIL_ATTESTERING, saksbehandlerIdent = "a12345"))

            assertThatThrownBy {
                sakActionController.attesterSak(sak.saksnummer, AttesterSakRequestDto(godkjent = true))
            }.isInstanceOf(ValideringException::class.java)
                .hasMessageContaining("Manglende rettighet")

            val attestertSak = sakRepository.getSak(sak.saksnummer)
            assertThat(attestertSak.status).isEqualTo(SakStatus.TIL_ATTESTERING)
        }

        @Test
        fun `skal feile validering når kommentar mangler ved avslag`() {
            val sak = SakTestData.lagreNySak(sakRepository, SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.TIL_ATTESTERING))

            assertThatThrownBy {
                sakActionController.attesterSak(sak.saksnummer, AttesterSakRequestDto(godkjent = false, kommentar = null))
            }.isInstanceOf(ValideringException::class.java)
                .hasMessageContaining("Kommentar må")

            assertThatThrownBy {
                sakActionController.attesterSak(sak.saksnummer, AttesterSakRequestDto(godkjent = false, kommentar = ""))
            }.isInstanceOf(ValideringException::class.java)
                .hasMessageContaining("Kommentar må")
        }

        @Test
        fun `skal feile validering når saken ikke er til attestering`() {
            val sak = SakTestData.lagreNySak(sakRepository, SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.UNDER_BEHANDLING))

            assertThatThrownBy {
                sakActionController.attesterSak(sak.saksnummer, AttesterSakRequestDto(godkjent = true))
            }.isInstanceOf(ValideringException::class.java)
                .hasMessageContaining("Ugyldig statusovergang")

        }
    }
}
