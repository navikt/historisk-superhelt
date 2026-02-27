package no.nav.historisk.superhelt.utbetaling.rest

import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.WithDriftbruker
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.historisk.superhelt.test.withMockedUser
import no.nav.historisk.superhelt.utbetaling.UtbetalingRepository
import no.nav.historisk.superhelt.utbetaling.UtbetalingService
import no.nav.historisk.superhelt.utbetaling.UtbetalingStatus
import no.nav.historisk.superhelt.utbetaling.kafka.UtbetalingKafkaProducer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester

@MockedSpringBootTest
@AutoConfigureMockMvc
class AdminUtbetalingControllerTest {


    @Autowired
    private lateinit var mockMvc: MockMvcTester
    
    @Autowired
    private lateinit var sakRepository: SakRepository

    @Autowired
    private lateinit var utbetalingRepository: UtbetalingRepository

    @Autowired
    private lateinit var utbetalingService: UtbetalingService

    @MockitoBean
    private lateinit var utbetalingKafkaMock: UtbetalingKafkaProducer


    @Test
    @WithSaksbehandler
    fun `Hent feilet utbetalinger er forbudt for saksbehandler`() {
        assertThat(mockMvc.get().uri("/admin/utbetaling/feilet"))
            .hasStatus(HttpStatus.FORBIDDEN)
    }

    @Test
    @WithDriftbruker
    fun `Hent feilet utbetalinger er ok for drift`() {
        withMockedUser {
            val sak = sakRepository.opprettNySak(SakTestData.nySakCompleteUtbetaling())
            val utbetaling = utbetalingRepository.opprettUtbetaling(sak)
            utbetalingService.updateUtbetalingsStatus(utbetaling, newStatus = UtbetalingStatus.FEILET)
        }
        assertThat(mockMvc.get().uri("/admin/utbetaling/feilet"))
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .convertTo(List::class.java)
            .satisfies({
                assertThat(it).hasSizeGreaterThanOrEqualTo(1)
            })
    }

    @Test
    @WithDriftbruker
    fun `Rekjør alle feilete utbetalinger `() {
        withMockedUser {
            val sak = sakRepository.opprettNySak(SakTestData.nySakCompleteUtbetaling())
            val utbetaling = utbetalingRepository.opprettUtbetaling(sak)
            utbetalingService.updateUtbetalingsStatus(utbetaling, newStatus = UtbetalingStatus.FEILET)
            val sak2 = sakRepository.opprettNySak(SakTestData.nySakCompleteUtbetaling())
            val utbetaling2 = utbetalingRepository.opprettUtbetaling(sak2)
            utbetalingService.updateUtbetalingsStatus(utbetaling2, newStatus = UtbetalingStatus.FEILET)
        }

        assertThat(
            mockMvc.post()
                .uri("/admin/utbetaling/feilet")
                .with(csrf())
                .contentType("application/json")
        )
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .convertTo(List::class.java)
            .satisfies({
                assertThat(it).hasSizeGreaterThanOrEqualTo(2)
            })

        verify(utbetalingKafkaMock, atLeast(2)).sendTilUtbetaling(any(), any())
    }

    @Test
    @WithDriftbruker
    fun `Rekjør noen feilete utbetalinger `() {
        var utbetalingUuid: java.util.UUID? = null
        withMockedUser {
            val sak = sakRepository.opprettNySak(SakTestData.nySakCompleteUtbetaling())
            val utbetaling = utbetalingRepository.opprettUtbetaling(sak)
            utbetalingService.updateUtbetalingsStatus(utbetaling, newStatus = UtbetalingStatus.FEILET)
            utbetalingUuid = utbetaling.uuid
            val sak2 = sakRepository.opprettNySak(SakTestData.nySakCompleteUtbetaling())
            val utbetaling2 = utbetalingRepository.opprettUtbetaling(sak2)
            utbetalingService.updateUtbetalingsStatus(utbetaling2, newStatus = UtbetalingStatus.FEILET)
        }

        assertThat(
            mockMvc.post()
                .uri("/admin/utbetaling/feilet")
                .with(csrf())
                .contentType("application/json")
                .content("{\"utbetalingIds\": [\"$utbetalingUuid\"]}")
        )
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .convertTo(List::class.java)
            .satisfies({
                assertThat(it).hasSize(1)
            })

        verify(utbetalingKafkaMock, times(1)).sendTilUtbetaling(any(), any())
    }


}