package no.nav.historisk.superhelt.sak.rest

import no.nav.common.types.NavIdent
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.infrastruktur.validation.ValideringException
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.oppgave.OppgaveType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.Test


@WithSaksbehandler(navIdent = "s12345")
class SakActionControllerFeilregistrerTest : AbstractSakActionTest() {

    @Autowired
    private lateinit var sakActionController: SakActionController

    @Test
    fun `feilregister sak under behandling`() {
        val sak = SakTestData.lagreNySak(
            sakRepository,
            SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.UNDER_BEHANDLING)
        )

        sakActionController.feilregister(
            saksnummer = sak.saksnummer,
            request = FeilregisterRequestDto("Årsak til feilregistrering")
        )

        val sendtSak = sakRepository.getSak(sak.saksnummer)
        assertThat(sendtSak.status).isEqualTo(SakStatus.FEILREGISTRERT)

        val endringslogg = endringsloggService.findBySak(sak.saksnummer)
        assertThat(endringslogg)
            .anySatisfy {
                assertThat(it.type).isEqualTo(EndringsloggType.FEILREGISTERT)
                assertThat(it.endretAv.value).isEqualTo("s12345")
            }
        verify(oppgaveService).ferdigstillOppgaver(
            eq(sak.saksnummer),
            eq(OppgaveType.BEH_SAK)
        )
        verify(oppgaveService).opprettOppgave(
            eq(OppgaveType.BEH_SAK_MK),
            any<Sak>(),
            any(),
            eq(NavIdent("s12345")),
            isNull()
        )
    }


    @Test
    fun `feilregister ferdig sak`() {
        val sak = SakTestData.lagreNySak(
            sakRepository,
            SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.FERDIG)
        )

        assertThatThrownBy {
            sakActionController.feilregister(
                saksnummer = sak.saksnummer,
                request = FeilregisterRequestDto("Årsak til feilregistrering")
            )
        }.isInstanceOf(ValideringException::class.java)

        val sendtSak = sakRepository.getSak(sak.saksnummer)
        assertThat(sendtSak.status).isEqualTo(SakStatus.FERDIG)

    }
}

