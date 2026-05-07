package no.nav.historisk.superhelt.sak.rest

import no.nav.common.types.Behandlingsnummer
import no.nav.historisk.superhelt.brev.BrevStatus
import no.nav.historisk.superhelt.brev.BrevTestdata
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.infrastruktur.validation.ValideringException
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.historisk.superhelt.test.withMockedUser
import no.nav.historisk.superhelt.vedtak.Vedtak
import no.nav.historisk.superhelt.vedtak.VedtakTestData
import no.nav.oppgave.OppgaveType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import kotlin.test.Test

/**
 * Tester at en sak som har blitt gjenåpnet ved en feil kan tilbakestilles til
 * tilstanden den hadde ved siste ferdigstilling (siste vedtak).
 *
 * Dette er en annen operasjon enn vanlig feilregistrering:
 * - Vanlig feilregistrering: brukes for nye saker (behandlingsnummer=1) uten vedtak
 *   setter status til FEILREGISTRERT.
 * - Tilbakestilling etter gjenåpning: brukes for saker med eksisterende vedtak der
 *   gjenåpningen var feil, gjenoppretter sak til tilstanden fra siste vedtak.
 */
@WithSaksbehandler(navIdent = "s12345")
class SakActionControllerTilbakestillEtterGjenapningTest : AbstractSakActionTest() {

    @Autowired
    private lateinit var sakActionController: SakActionController

    private fun lagreFerdigSakMedVedtak(): Pair<Sak, Vedtak> {
        val sak = SakTestData.lagreSak(
            sakRepository,
            SakTestData.sakMedUtbetaling().copy(status = SakStatus.FERDIG)
        )
        val vedtak = VedtakTestData.vedtakForSak(sak)
        withMockedUser { vedtakRepository.save(vedtak) }
        return Pair(sak, vedtak)
    }

    @Test
    fun `saksbehandler kan tilbakestille gjenåpnet sak til tilstanden fra siste vedtak`() {
        val (originalSak, vedtak) = lagreFerdigSakMedVedtak()

        sakActionController.gjenapneSak(originalSak.saksnummer, GjenapneSakRequestDto("Gjenåpnet ved en feil"))

        val gjenapnetSak = sakRepository.getSak(originalSak.saksnummer)
        assertThat(gjenapnetSak.gjenapnet).isTrue()
        assertThat(gjenapnetSak.behandlingsnummer).isEqualTo(Behandlingsnummer(2))
        sakActionController.tilbakestillGjenapning(
            originalSak.saksnummer,
            TilbakestillGjenapningRequestDto("Gjenåpningen var feil")
        )

        val tilbakestiltSak = sakRepository.getSak(originalSak.saksnummer)

        assertThat(tilbakestiltSak.status).isEqualTo(SakStatus.FERDIG)
        assertThat(tilbakestiltSak.gjenapnet).isFalse()
        assertThat(tilbakestiltSak.behandlingsnummer).isEqualTo(vedtak.behandlingsnummer)
        assertThat(tilbakestiltSak.saksbehandler).isEqualTo(vedtak.saksbehandler)
        assertThat(tilbakestiltSak.attestant).isEqualTo(vedtak.attestant)
        assertThat(tilbakestiltSak.beskrivelse).isEqualTo(vedtak.beskrivelse)
        assertThat(tilbakestiltSak.begrunnelse).isEqualTo(vedtak.begrunnelse)
        assertThat(tilbakestiltSak.soknadsDato).isEqualTo(vedtak.soknadsDato)
        assertThat(tilbakestiltSak.tildelingsAar).isEqualTo(vedtak.tildelingsAar)
        assertThat(tilbakestiltSak.vedtaksResultat).isEqualTo(vedtak.resultat)
        assertThat(tilbakestiltSak.utbetalingsType).isEqualTo(vedtak.utbetalingsType)
        assertThat(tilbakestiltSak.belop).isEqualTo(vedtak.belop)
    }

    @Test
    fun `tilbakestilling logger til endringslogg`() {
        val (originalSak, _) = lagreFerdigSakMedVedtak()

        sakActionController.gjenapneSak(originalSak.saksnummer, GjenapneSakRequestDto("Feil gjenåpning"))

        sakActionController.tilbakestillGjenapning(
            originalSak.saksnummer,
            TilbakestillGjenapningRequestDto("Dette var feil gjenåpnet")
        )

        val endringslogg = endringsloggService.findBySak(originalSak.saksnummer)
        assertThat(endringslogg)
            .anySatisfy {
                assertThat(it.type).isEqualTo(EndringsloggType.TILBAKESTILT_SAK)
                assertThat(it.endretAv.value).isEqualTo("s12345")
            }
    }

    @Test
    fun `tilbakestilling ferdigstiller oppgaven opprettet ved gjenåpning`() {
        val (originalSak, _) = lagreFerdigSakMedVedtak()

        sakActionController.gjenapneSak(originalSak.saksnummer, GjenapneSakRequestDto("Feil gjenåpning"))

        sakActionController.tilbakestillGjenapning(
            originalSak.saksnummer,
            TilbakestillGjenapningRequestDto("Dette var feil gjenåpnet")
        )

        verify(oppgaveService).ferdigstillOppgaver(
            eq(originalSak.saksnummer),
            eq(OppgaveType.BEH_SAK)
        )
    }

    @Test
    fun `sak uten gjenåpning kan ikke tilbakestilles`() {
        val sak = SakTestData.lagreSak(
            sakRepository,
            SakTestData.sakMedUtbetaling().copy(status = SakStatus.UNDER_BEHANDLING)
        )

        assertThatThrownBy {
            sakActionController.tilbakestillGjenapning(
                sak.saksnummer,
                TilbakestillGjenapningRequestDto("Feil årsak men sak er ikke gjenåpnet")
            )
        }.isInstanceOf(ValideringException::class.java)

        val lagretSak = sakRepository.getSak(sak.saksnummer)
        assertThat(lagretSak.status).isEqualTo(SakStatus.UNDER_BEHANDLING)
        assertThat(lagretSak.gjenapnet).isFalse()
    }

    @Test
    fun `vedtaksbrevBruker som ikke er fullført slettes ved tilbakestilling`() {
        val (originalSak, _) = lagreFerdigSakMedVedtak()

        val originalVedtaksbrev = BrevTestdata.lagreBrev(
            brevRepository,
            originalSak.saksnummer,
            BrevTestdata.vedtaksbrevBruker().copy(
                status = BrevStatus.SENDT,
                opprettetTidspunkt = Instant.now().minusSeconds(60)
            )
        )

        sakActionController.gjenapneSak(originalSak.saksnummer, GjenapneSakRequestDto("Gjenåpnet ved en feil"))

        val brevOpprettetEtterGjenapning = BrevTestdata.lagreBrev(
            brevRepository,
            originalSak.saksnummer,
            BrevTestdata.vedtaksbrevBruker().copy(opprettetTidspunkt = Instant.now())
        )

        sakActionController.tilbakestillGjenapning(
            originalSak.saksnummer,
            TilbakestillGjenapningRequestDto("Gjenåpningen var feil")
        )

        val tilbakestiltSak = sakRepository.getSak(originalSak.saksnummer)
        assertThat(tilbakestiltSak.vedtaksbrevBruker?.uuid).isEqualTo(originalVedtaksbrev.uuid)

        val alleBrevsForSak = brevRepository.findBySak(originalSak.saksnummer)
        assertThat(alleBrevsForSak).noneMatch { it.uuid == brevOpprettetEtterGjenapning.uuid }
    }

    @Test
    fun `vedtaksbrevBruker som er fullført slettes ikke ved tilbakestilling`() {
        val (originalSak, _) = lagreFerdigSakMedVedtak()

        val fullfortVedtaksbrev = BrevTestdata.lagreBrev(
            brevRepository,
            originalSak.saksnummer,
            BrevTestdata.vedtaksbrevBruker().copy(status = BrevStatus.SENDT)
        )

        sakActionController.gjenapneSak(originalSak.saksnummer, GjenapneSakRequestDto("Gjenåpnet ved en feil"))

        sakActionController.tilbakestillGjenapning(
            originalSak.saksnummer,
            TilbakestillGjenapningRequestDto("Gjenåpningen var feil")
        )

        val alleBrevsForSak = brevRepository.findBySak(originalSak.saksnummer)
        assertThat(alleBrevsForSak).anyMatch { it.uuid == fullfortVedtaksbrev.uuid }
    }
}
