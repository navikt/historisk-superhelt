package no.nav.historisk.superhelt.sak.rest

import no.nav.historisk.superhelt.infrastruktur.exception.ValideringException
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.sak.db.SakJpaEntity
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.WithAttestant
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.historisk.superhelt.test.withMockedUser
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

    @MockitoBean
    private lateinit var utbetalingService: UtbetalingService


    fun lagreNySak(sak: SakJpaEntity): Sak {
        return withMockedUser {
            sakRepository.save(sak)
        }
    }


    @WithSaksbehandler(navIdent = "s12345")
    @Nested
    inner class `Send til attestering` {


    }

    @WithAttestant(navIdent = "a12345")
    @Nested
    inner class `Attester sak` {

        @Test
        fun `skal attestere sak med godkjent`() {
            val sak = lagreNySak(SakTestData.sakEntityCompleteUtbetaling(sakStatus = SakStatus.TIL_ATTESTERING))

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
                    assertThat(it.attestant.value).isEqualTo("a12345")
                    assertThat(it.vedtaksTidspunkt).isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS))
                })

            verify(utbetalingService).sendTilUtbetaling(any())

            // TODO sjekke feks changelog og brev når det er på plass

        }

        @Test
        fun `skal attestere sak med avslag`() {
            val sak = lagreNySak(SakTestData.sakEntityCompleteUtbetaling(sakStatus = SakStatus.TIL_ATTESTERING))

            sakActionController.attesterSak(sak.saksnummer, AttesterSakRequestDto(godkjent = false, kommentar = "Avslått av attestant"))

            val attestertSak = sakRepository.getSak(sak.saksnummer)
            assertThat(attestertSak.status).isEqualTo(SakStatus.UNDER_BEHANDLING)

            // TODO: Sjekk at changelog er logget korrekt hvis mulig
        }

        @WithSaksbehandler
        @Test
        fun `saksbehandler skal ikke få attestere`() {
            val sak = lagreNySak(SakTestData.sakEntityCompleteUtbetaling(sakStatus = SakStatus.TIL_ATTESTERING))

            assertThatThrownBy {
                sakActionController.attesterSak(sak.saksnummer, AttesterSakRequestDto(godkjent = true))
            }.isInstanceOf(ValideringException::class.java)
                .hasMessageContaining("Manglende rettighet")

            val attestertSak = sakRepository.getSak(sak.saksnummer)
            assertThat(attestertSak.status).isEqualTo(SakStatus.TIL_ATTESTERING)
        }

        @Test
        fun `attestant skal ikke få attestere sin egen sak`() {
            val sak = lagreNySak(SakTestData.sakEntityCompleteUtbetaling(sakStatus = SakStatus.TIL_ATTESTERING, saksbehehandlerIdent = "a12345"))

            assertThatThrownBy {
                sakActionController.attesterSak(sak.saksnummer, AttesterSakRequestDto(godkjent = true))
            }.isInstanceOf(ValideringException::class.java)
                .hasMessageContaining("Manglende rettighet")

            val attestertSak = sakRepository.getSak(sak.saksnummer)
            assertThat(attestertSak.status).isEqualTo(SakStatus.TIL_ATTESTERING)
        }

        @Test
        fun `skal feile validering når kommentar mangler ved avslag`() {
            val sak = lagreNySak(SakTestData.sakEntityCompleteUtbetaling(sakStatus = SakStatus.TIL_ATTESTERING))

            assertThatThrownBy {
                sakActionController.attesterSak(sak.saksnummer, AttesterSakRequestDto(godkjent = false, kommentar = null))
            }.isInstanceOf(ValideringException::class.java)
                .hasMessageContaining("Kommentar å oppgis når sak ikke godkjennes")

            assertThatThrownBy {
                sakActionController.attesterSak(sak.saksnummer, AttesterSakRequestDto(godkjent = false, kommentar = ""))
            }.isInstanceOf(ValideringException::class.java)
                .hasMessageContaining("Kommentar å oppgis når sak ikke godkjennes")
        }

        @Test
        fun `skal feile validering når saken ikke er til attestering`() {
            val sak = lagreNySak(SakTestData.sakEntityCompleteUtbetaling(sakStatus = SakStatus.UNDER_BEHANDLING))

            assertThatThrownBy {
                sakActionController.attesterSak(sak.saksnummer, AttesterSakRequestDto(godkjent = true))
            }.isInstanceOf(ValideringException::class.java)
                .hasMessageContaining("Ugyldig statusovergang")

        }
    }
}
