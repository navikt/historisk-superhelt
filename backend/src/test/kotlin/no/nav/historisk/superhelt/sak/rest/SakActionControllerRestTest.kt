package no.nav.historisk.superhelt.sak.rest

import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.sak.db.SakJpaEntity
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.WithAttestant
import no.nav.historisk.superhelt.test.withMockedUser
import no.nav.historisk.superhelt.utbetaling.UtbetalingService
import no.nav.historisk.superhelt.vedtak.VedtakRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
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


    @WithAttestant(navIdent = "a12345")
    @Nested
    inner class `ferdigstill sak` {

        @Test
        fun `skal ferdigstille sak`() {
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
        }
        //TODO sjekke validering
    }
}
