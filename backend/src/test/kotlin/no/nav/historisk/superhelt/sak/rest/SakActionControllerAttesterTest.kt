package no.nav.historisk.superhelt.sak.rest

import no.nav.historisk.superhelt.brev.Brev
import no.nav.historisk.superhelt.brev.BrevTestdata
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.infrastruktur.validation.ValideringException
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.WithAttestant
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.oppgave.OppgaveType
import org.assertj.core.api.Assertions.*
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.Test


@WithAttestant(navIdent = "a12345")
class SakActionControllerAttesterTest : AbstractSakActionTest() {

    @Autowired
    private lateinit var sakActionController: SakActionController

    @Test
    fun `skal attestere sak med godkjent`() {
        val sak = SakTestData.lagreNySak(
            sakRepository,
            SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.TIL_ATTESTERING)
        )
        BrevTestdata.lagreBrev(
            brevRepository,
            sak.saksnummer,
            BrevTestdata.vedtaksbrevBruker()
        )

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
                assertThat(it.attestant.navIdent.value).isEqualTo("a12345")
                assertThat(it.vedtaksTidspunkt).isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS))
            })

        verify(utbetalingService).sendTilUtbetaling(any())
        verify(brevSendingService).sendBrev(any<Sak>(), any<Brev>())

        verify(oppgaveService).ferdigstillOppgaver(
            eq(sak.saksnummer),
            eq(OppgaveType.GOD_VED)
        )

        val endringslogg = endringsloggService.findBySak(sak.saksnummer)
        assertThat(endringslogg)
            .anySatisfy {
                assertThat(it.type).isEqualTo(EndringsloggType.ATTESTERT_SAK)
                assertThat(it.endretAv.value).isEqualTo("a12345")
            }

    }

    @Test
    fun `skal attestere sak med avslag`() {
        val sak = SakTestData.lagreNySak(
            sakRepository,
            SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.TIL_ATTESTERING)
        )

        sakActionController.attesterSak(
            sak.saksnummer,
            AttesterSakRequestDto(godkjent = false, kommentar = "Avslått av attestant")
        )

        val attestertSak = sakRepository.getSak(sak.saksnummer)
        assertThat(attestertSak.status).isEqualTo(SakStatus.UNDER_BEHANDLING)

        verify(oppgaveService).ferdigstillOppgaver(
            eq(sak.saksnummer),
            eq(OppgaveType.GOD_VED)
        )
        verify(oppgaveService).opprettOppgave(
            eq(OppgaveType.BEH_UND_VED),
            any<Sak>(),
            any(),
            eq(sak.saksbehandler.navIdent),
            any()
        )


        val endringslogg = endringsloggService.findBySak(sak.saksnummer)
        assertThat(endringslogg)
            .anySatisfy {
                assertThat(it.type).isEqualTo(EndringsloggType.ATTESTERING_UNDERKJENT)
                assertThat(it.endretAv.value).isEqualTo("a12345")
            }

    }

    @WithSaksbehandler
    @Test
    fun `saksbehandler skal ikke få attestere`() {
        val sak = SakTestData.lagreNySak(
            sakRepository,
            SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.TIL_ATTESTERING)
        )

        assertThatThrownBy {
            sakActionController.attesterSak(sak.saksnummer, AttesterSakRequestDto(godkjent = true))
        }.isInstanceOf(ValideringException::class.java)
            .hasMessageContaining("Manglende rettighet")

        val attestertSak = sakRepository.getSak(sak.saksnummer)
        assertThat(attestertSak.status).isEqualTo(SakStatus.TIL_ATTESTERING)
    }

    @Test
    fun `attestant skal ikke få attestere sin egen sak`() {
        val sak = SakTestData.lagreNySak(
            sakRepository,
            SakTestData.nySakCompleteUtbetaling(
                sakStatus = SakStatus.TIL_ATTESTERING,
                saksbehandlerIdent = "a12345"
            )
        )

        assertThatThrownBy {
            sakActionController.attesterSak(sak.saksnummer, AttesterSakRequestDto(godkjent = true))
        }.isInstanceOf(ValideringException::class.java)
            .hasMessageContaining("Manglende rettighet")

        val attestertSak = sakRepository.getSak(sak.saksnummer)
        assertThat(attestertSak.status).isEqualTo(SakStatus.TIL_ATTESTERING)
    }

    @Test
    fun `skal feile validering når kommentar mangler ved avslag`() {
        val sak = SakTestData.lagreNySak(
            sakRepository,
            SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.TIL_ATTESTERING)
        )

        assertThatThrownBy {
            sakActionController.attesterSak(
                sak.saksnummer,
                AttesterSakRequestDto(godkjent = false, kommentar = null)
            )
        }.isInstanceOf(ValideringException::class.java)
            .hasMessageContaining("Kommentar må")

        assertThatThrownBy {
            sakActionController.attesterSak(sak.saksnummer, AttesterSakRequestDto(godkjent = false, kommentar = ""))
        }.isInstanceOf(ValideringException::class.java)
            .hasMessageContaining("Kommentar må")
    }

    @Test
    fun `skal feile validering når saken ikke er til attestering`() {
        val sak = SakTestData.lagreNySak(
            sakRepository,
            SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.UNDER_BEHANDLING)
        )

        assertThatThrownBy {
            sakActionController.attesterSak(sak.saksnummer, AttesterSakRequestDto(godkjent = true))
        }.isInstanceOf(ValideringException::class.java)
            .hasMessageContaining("Manglende rettighet i sak: ATTESTERE")

    }
}

