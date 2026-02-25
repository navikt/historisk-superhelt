package no.nav.historisk.superhelt.sak.rest

import no.nav.historisk.superhelt.brev.BrevRepository
import no.nav.historisk.superhelt.brev.BrevSendingService
import no.nav.historisk.superhelt.endringslogg.EndringsloggService
import no.nav.historisk.superhelt.oppgave.OppgaveService
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.utbetaling.UtbetalingService
import no.nav.historisk.superhelt.vedtak.VedtakRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester

@MockedSpringBootTest
@AutoConfigureMockMvc
abstract class AbstractSakActionTest {
    @Autowired
    protected lateinit var mockMvc: MockMvcTester

    @Autowired
    protected lateinit var sakRepository: SakRepository

    @Autowired
    protected lateinit var vedtakRepository: VedtakRepository

    @Autowired
    protected lateinit var brevRepository: BrevRepository

    @Autowired
    protected lateinit var endringsloggService: EndringsloggService

    @MockitoBean
    protected lateinit var utbetalingService: UtbetalingService

    @MockitoBean
    protected lateinit var oppgaveService: OppgaveService

    @MockitoBean
    protected lateinit var brevSendingService: BrevSendingService
}