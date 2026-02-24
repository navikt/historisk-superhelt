package no.nav.historisk.superhelt.sak.rest

import no.nav.historisk.superhelt.brev.BrevTestdata
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.infrastruktur.validation.ValideringException
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.historisk.superhelt.vedtak.VedtaksResultat
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.Test


@WithSaksbehandler
class SakActionControllerHenleggTest : AbstractSakActionTest() {

    @Autowired
    private lateinit var sakActionController: SakActionController

    @Test
    fun `henlegg sak under behandling`() {
        val sak = SakTestData.lagreNySak(
            sakRepository,
            SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.UNDER_BEHANDLING)
        )
        val brev = BrevTestdata.lagreBrev(
            brevRepository,
            sak.saksnummer,
            BrevTestdata.henleggBrev()
        )

        sakActionController.henleggSak(
            saksnummer = sak.saksnummer,
            request = HenlagtSakRequestDto(
                henleggelseBrevId = brev.uuid,
                aarsak = "Årsak til henleggelse"
            )
        )

        val lagretSak = sakRepository.getSak(sak.saksnummer)
        assertThat(lagretSak.status).isEqualTo(SakStatus.FERDIG)
        assertThat(lagretSak.vedtaksResultat).isEqualTo(VedtaksResultat.HENLAGT)

        verify(brevSendingService).sendBrev(any<Sak>(), eq(brev.uuid))

        val endringslogg = endringsloggService.findBySak(sak.saksnummer)
        assertThat(endringslogg)
            .anySatisfy {
                assertThat(it.type).isEqualTo(EndringsloggType.HENLAGT_SAK)
            }
        verify(oppgaveService).ferdigstillOppgaver(
            eq(sak.saksnummer)
        )
    }

    @Test
    fun `henlegg ferdig sak skal feile`() {
        val sak = SakTestData.lagreNySak(
            sakRepository,
            SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.FERDIG)
        )
        val brev = BrevTestdata.lagreBrev(
            brevRepository,
            sak.saksnummer,
            BrevTestdata.henleggBrev()
        )



        assertThatThrownBy {
            sakActionController.henleggSak(
                saksnummer = sak.saksnummer,
                request = HenlagtSakRequestDto(
                    henleggelseBrevId = brev.uuid,
                    aarsak = "Årsak til henleggelse"
                )
            )
        }.isInstanceOf(ValideringException::class.java)

        val lagretSak = sakRepository.getSak(sak.saksnummer)
        assertThat(lagretSak.status).isEqualTo(SakStatus.FERDIG)

    }
}

