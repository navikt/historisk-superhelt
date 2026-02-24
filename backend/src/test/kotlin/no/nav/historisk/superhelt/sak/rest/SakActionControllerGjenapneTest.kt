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
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.Test


@WithSaksbehandler(navIdent = "s12345")
class SakActionControllerGjenapneTest : AbstractSakActionTest() {
    @Autowired
    private lateinit var sakActionController: SakActionController


    @Test
    fun `Saksbehandler kan gjenåpne ferdig sak `() {
        val sak = SakTestData.lagreNySak(
            sakRepository,
            SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.FERDIG)
        )

        sakActionController.gjenapneSak(sak.saksnummer, GjenapneSakRequestDto("Fordi jeg vil"))

        val lagretSak = sakRepository.getSak(sak.saksnummer)
        assertThat(lagretSak.status).isEqualTo(SakStatus.UNDER_BEHANDLING)
        assertThat(lagretSak.attestant).isNull()

        val endringslogg = endringsloggService.findBySak(sak.saksnummer)
        assertThat(endringslogg)
            .anySatisfy {
                assertThat(it.type).isEqualTo(EndringsloggType.GJENAPNET_SAK)
                assertThat(it.endretAv.value).isEqualTo("s12345")
            }

        verify(oppgaveService).opprettOppgave(
            eq(OppgaveType.BEH_SAK),
            any<Sak>(),
            any(),
            eq(NavIdent("s12345")),
            any()
        )
    }

    @Test
    fun `Sak som ikke er ferdig kan ikke gjenåpnes `() {
        val sak = SakTestData.lagreNySak(
            sakRepository,
            SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.UNDER_BEHANDLING)
        )

        assertThatThrownBy {
            sakActionController.gjenapneSak(sak.saksnummer, GjenapneSakRequestDto("Fordi jeg vil"))
        }.isInstanceOf(ValideringException::class.java)

        val lagretSak = sakRepository.getSak(sak.saksnummer)
        assertThat(lagretSak.status).isEqualTo(SakStatus.UNDER_BEHANDLING)
    }

}