package no.nav.historisk.superhelt.utbetaling.rest

import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.WithDriftbruker
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.historisk.superhelt.test.withMockedUser
import no.nav.historisk.superhelt.utbetaling.UtbetalingService
import no.nav.historisk.superhelt.utbetaling.UtbetalingStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.assertj.MockMvcTester
import tools.jackson.databind.json.JsonMapper

@MockedSpringBootTest
@AutoConfigureMockMvc
class AdminUtbetalingControllerTest {


    @Autowired
    private lateinit var mockMvc: MockMvcTester

    @Autowired
    private lateinit var jsonMapper: JsonMapper

    @Autowired
    private lateinit var sakRepository: SakRepository

    @Autowired
    private lateinit var utbetalingService: UtbetalingService


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
            utbetalingService.updateUtbetalingsStatus(sak.utbetaling!!, newStatus = UtbetalingStatus.FEILET)

        }
        assertThat(mockMvc.get().uri("/admin/utbetaling/feilet"))
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .convertTo(List::class.java)
            .satisfies({
                assertThat(it).hasSizeGreaterThanOrEqualTo(1)
            })
    }


}