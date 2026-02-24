package no.nav.historisk.superhelt.sak.rest

import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.infrastruktur.validation.ValideringException
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.WithAttestant
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
class SakActionControllerSendTilAttesteringTest : AbstractSakActionTest() {

    @Autowired
    private lateinit var sakActionController: SakActionController

    @Test
    fun `skal sende sak til attestering`() {
        val sak = SakTestData.lagreNySak(
            sakRepository,
            SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.UNDER_BEHANDLING)
        )

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
        verify(oppgaveService).ferdigstillOppgaver(
            eq(sak.saksnummer),
            eq(OppgaveType.BEH_SAK),
            eq(OppgaveType.BEH_UND_VED)
        )
        verify(oppgaveService).opprettOppgave(
            eq(OppgaveType.GOD_VED),
            any<Sak>(),
            any(),
            isNull(),
            any()
        )
    }

    @WithAttestant
    @Test
    fun `attestant skal ikke få sende til attestering`() {
        val sak = SakTestData.lagreNySak(
            sakRepository,
            SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.UNDER_BEHANDLING)
        )

        assertThatThrownBy {
            sakActionController.tilAttestering(sak.saksnummer)
        }.isInstanceOf(ValideringException::class.java)
            .hasMessageContaining("Manglende rettighet")

        val sendtSak = sakRepository.getSak(sak.saksnummer)
        assertThat(sendtSak.status).isEqualTo(SakStatus.UNDER_BEHANDLING)
    }

    @Test
    fun `skal feile validering når saken ikke er under behandling`() {
        val sak =
            SakTestData.lagreNySak(sakRepository, SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.FERDIG))

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

