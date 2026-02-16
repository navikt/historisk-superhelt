package no.nav.historisk.superhelt.sak.rest

import no.nav.common.types.NavIdent
import no.nav.historisk.superhelt.brev.Brev
import no.nav.historisk.superhelt.brev.BrevRepository
import no.nav.historisk.superhelt.brev.BrevSendingService
import no.nav.historisk.superhelt.brev.BrevTestdata
import no.nav.historisk.superhelt.endringslogg.EndringsloggService
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.infrastruktur.validation.ValideringException
import no.nav.historisk.superhelt.oppgave.OppgaveService
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.WithAttestant
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.historisk.superhelt.utbetaling.UtbetalingService
import no.nav.historisk.superhelt.vedtak.VedtakRepository
import no.nav.historisk.superhelt.vedtak.VedtaksResultat
import no.nav.oppgave.OppgaveType
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.Test

@MockedSpringBootTest
@AutoConfigureMockMvc
class SakActionControllerTest() {

    @Autowired
    private lateinit var sakActionController: SakActionController

    @Autowired
    private lateinit var sakRepository: SakRepository

    @Autowired
    private lateinit var vedtakRepository: VedtakRepository

    @Autowired
    private lateinit var brevRepository: BrevRepository

    @Autowired
    private lateinit var endringsloggService: EndringsloggService

    @MockitoBean
    private lateinit var utbetalingService: UtbetalingService

    @MockitoBean
    private lateinit var oppgaveService: OppgaveService

    @MockitoBean
    private lateinit var brevSendingService: BrevSendingService


    @WithSaksbehandler(navIdent = "s12345")
    @Nested
    inner class `Send til attestering` {

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

    @WithAttestant(navIdent = "a12345")
    @Nested
    inner class `Attester sak` {

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

    @WithSaksbehandler(navIdent = "s12345")
    @Nested
    inner class `feilregistrer sak` {

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


    @WithSaksbehandler
    @Nested
    inner class `henlegg sak` {

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
                    hendleggelseBrevId =brev.uuid,
                    aarsak = "Årsak til feilregistrering"
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
    }
}
