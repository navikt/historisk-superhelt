package no.nav.historisk.superhelt.utbetaling.rest

import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.historisk.superhelt.test.withMockedUser
import no.nav.historisk.superhelt.utbetaling.UtbetalingRepository
import no.nav.historisk.superhelt.utbetaling.UtbetalingService
import no.nav.historisk.superhelt.utbetaling.UtbetalingStatus
import no.nav.historisk.superhelt.utbetaling.kafka.UtbetalingKafkaProducer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester

@MockedSpringBootTest
@AutoConfigureMockMvc
class UtbetalingControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvcTester

    @Autowired
    private lateinit var sakRepository: SakRepository

    @Autowired
    private lateinit var utbetalingRepository: UtbetalingRepository

    @Autowired
    private lateinit var utbetalingService: UtbetalingService

    @WithSaksbehandler
    @Test
    fun `Retry utbetaling for saksbehandler`() {
        val sak = SakTestData.lagreNySak(sakRepository, SakTestData.nySakCompleteUtbetaling())
        withMockedUser {
            val utbetaling = utbetalingRepository.opprettUtbetaling(sak)
            utbetalingService.updateUtbetalingsStatus(utbetaling, newStatus = UtbetalingStatus.FEILET)
        }
        assertThat(
            mockMvc.post()
                .uri("/api/utbetaling/retry/{saksnummer}", sak.saksnummer)
                .with(csrf())
        )
            .hasStatus(HttpStatus.OK)
        verify(utbetalingKafkaMock,).sendTilUtbetaling(any(), any())
    }

    @MockitoBean
    private lateinit var utbetalingKafkaMock: UtbetalingKafkaProducer
}