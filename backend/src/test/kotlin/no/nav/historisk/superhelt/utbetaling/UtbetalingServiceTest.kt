package no.nav.historisk.superhelt.utbetaling

import no.nav.common.types.Belop
import no.nav.helved.UtbetalingMelding
import no.nav.historisk.superhelt.endringslogg.EndringsloggService
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.sak.*
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.historisk.superhelt.test.withMockedUser
import no.nav.historisk.superhelt.vedtak.VedtaksResultat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.KafkaException
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.test.context.NestedTestConfiguration
import org.springframework.test.context.NestedTestConfiguration.EnclosingConfiguration.INHERIT
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.concurrent.CompletableFuture

@MockedSpringBootTest
@WithSaksbehandler
@NestedTestConfiguration(INHERIT)
class UtbetalingServiceTest {

    @Autowired
    private lateinit var utbetalingService: UtbetalingService

    @Autowired
    private lateinit var utbetalingRepository: UtbetalingRepository

    @Autowired
    private lateinit var sakRepository: SakRepository

    @MockitoBean
    private lateinit var kafkaTemplate: KafkaTemplate<String, UtbetalingMelding>

    @MockitoBean
    private lateinit var sakEndringsloggService: EndringsloggService


    private fun lagreSakMedUtbetaling(utbetalingStatus: UtbetalingStatus = UtbetalingStatus.UTKAST): Pair<Sak, Utbetaling> {
        val savedSak = lagreSak()
        val utbetaling = withMockedUser {
            val utbetaling = utbetalingRepository.opprettUtbetaling(savedSak)
            utbetalingRepository.setUtbetalingStatus(utbetaling.transaksjonsId, utbetalingStatus)
            utbetaling
        }
        return Pair(savedSak, utbetalingRepository.findByTransaksjonsId(utbetaling.transaksjonsId)!!)
    }

    private fun lagreSak(
        utbetalingsType: UtbetalingsType = UtbetalingsType.BRUKER,
        vedtaksResultat: VedtaksResultat = VedtaksResultat.INNVILGET,
        sakStatus: SakStatus = SakStatus.FERDIG
    ): Sak {
        val savedSak = withMockedUser {
            val sak = SakTestData.lagreSak(sakRepository, SakTestData.sakMedUtbetaling())
            sakRepository.updateSak(
                sak.saksnummer, UpdateSakDto(
                    status = sakStatus,
                    utbetalingsType = utbetalingsType,
                    vedtaksResultat = vedtaksResultat
                )
            )
        }
        return savedSak
    }

    private fun mockKafkaSuccess() {
        val future = CompletableFuture<SendResult<String, UtbetalingMelding>>()
        future.complete(mock())
        whenever(kafkaTemplate.send(any<String>(), any<String>(), any<UtbetalingMelding>()))
            .thenReturn(future)
    }

    private fun mockKafkaFailure(exception: Exception = KafkaException("Kafka connection failed")) {
        val future = CompletableFuture<SendResult<String, UtbetalingMelding>>()
        future.completeExceptionally(exception)
        whenever(kafkaTemplate.send(any<String>(), any<String>(), any<UtbetalingMelding>()))
            .thenReturn(future)
    }

    @Nested
    inner class SendTilUtbetaling {

        @EnumSource(names = ["UNDER_BEHANDLING", "TIL_ATTESTERING", "FEILREGISTRERT"])
        @ParameterizedTest
        fun `skal kaste exeption når sakstatus er`(sakStatus: SakStatus) {
            val sak = lagreSak(sakStatus = sakStatus)
            assertThrows<IllegalArgumentException> { utbetalingService.sendTilUtbetaling(sak) }
        }

        @EnumSource(names = ["FERDIG_ATTESTERT", "FERDIG"])
        @ParameterizedTest
        fun `skal behandle saker med sakstatus`(sakStatus: SakStatus) {
            mockKafkaSuccess()
            val sak = lagreSak(sakStatus = sakStatus)
            utbetalingService.sendTilUtbetaling(sak)
        }

        @EnumSource(names = ["UTKAST", "KLAR_TIL_UTBETALING"])
        @ParameterizedTest
        fun `skal sende utbetaling når utbetalingstatus er`(utbetalingsStatus: UtbetalingStatus) {
            mockKafkaSuccess()
            val (sak, utbetaling) = lagreSakMedUtbetaling(utbetalingStatus = utbetalingsStatus)

            utbetalingService.sendTilUtbetaling(sak)

            val oppdatertUtbetaling = utbetalingRepository.findByTransaksjonsId(utbetaling.transaksjonsId)
            assertThat(oppdatertUtbetaling?.utbetalingStatus).isEqualTo(UtbetalingStatus.SENDT_TIL_UTBETALING)
            assertThat(oppdatertUtbetaling?.utbetalingTidspunkt).isNotNull
            verify(kafkaTemplate).send(
                any<String>(),
                eq(utbetaling.transaksjonsId.toString()),
                any<UtbetalingMelding>()
            )
        }

        @Test
        fun `skal sende korrekt melding til kafka topic`() {
            mockKafkaSuccess()
            val (sak, utbetaling) = lagreSakMedUtbetaling(utbetalingStatus = UtbetalingStatus.UTKAST)

            utbetalingService.sendTilUtbetaling(sak)

            verify(kafkaTemplate).send(
                any<String>(),
                eq(utbetaling.transaksjonsId.toString()),
                argThat { melding ->
                    melding.id == utbetaling.utbetalingsUuid &&
                            melding.sakId == sak.saksnummer.value &&
                            melding.behandlingId == sak.behandlingsnummer.toString() &&
                            melding.personident == sak.fnr.value &&
                            melding.perioder.first().beløp == sak.belop!!.value &&
                            melding.saksbehandler == sak.saksbehandler.navIdent.value
                }
            )
            val oppdatertUtbetaling = utbetalingRepository.findByTransaksjonsId(utbetaling.transaksjonsId)
            assertThat(oppdatertUtbetaling?.utbetalingStatus).isEqualTo(UtbetalingStatus.SENDT_TIL_UTBETALING)
        }

        @Test
        fun `skal ikke gjøre noe når sak mangler utbetaling`() {
            val sak = lagreSak(utbetalingsType = UtbetalingsType.INGEN)
            utbetalingService.sendTilUtbetaling(sak)
            verify(kafkaTemplate, never()).send(any<String>(), any<String>(), any<UtbetalingMelding>())
        }

        @Test
        fun `skal ikke gjøre noe når sak er forhandstilsagn`() {
            val sak = lagreSak(utbetalingsType = UtbetalingsType.FORHANDSTILSAGN)
            utbetalingService.sendTilUtbetaling(sak)
            verify(kafkaTemplate, never()).send(any<String>(), any<String>(), any<UtbetalingMelding>())
        }

        @EnumSource(names = ["AVSLATT", "HENLAGT"])
        @ParameterizedTest
        fun `skal ikke gjøre noe når sak er`(vedtaksResultat: VedtaksResultat) {
            val sak = lagreSak(utbetalingsType = UtbetalingsType.BRUKER, vedtaksResultat = vedtaksResultat)
            utbetalingService.sendTilUtbetaling(sak)
            verify(kafkaTemplate, never()).send(any<String>(), any<String>(), any<UtbetalingMelding>())
        }

        @EnumSource(names = ["INNVILGET", "DELVIS_INNVILGET"])
        @ParameterizedTest
        fun `skal sende utbetaling ved vedtaksresultat`(vedtaksResultat: VedtaksResultat) {
            mockKafkaSuccess()
            val sak = lagreSak(utbetalingsType = UtbetalingsType.BRUKER, vedtaksResultat = vedtaksResultat)

            utbetalingService.sendTilUtbetaling(sak)

            verify(kafkaTemplate).send(any<String>(), any<String>(), any<UtbetalingMelding>())
        }

        @EnumSource(names = ["SENDT_TIL_UTBETALING", "MOTTATT_AV_UTBETALING", "UTBETALT", "FEILET"])
        @ParameterizedTest
        fun `skal ignorere når utbetaling finnes og status er `(utbetalingsStatus: UtbetalingStatus) {
            val (sak, _) = lagreSakMedUtbetaling(utbetalingStatus = utbetalingsStatus)
            utbetalingService.sendTilUtbetaling(sak)
            verify(kafkaTemplate, never()).send(any<String>(), any<String>(), any<UtbetalingMelding>())
        }

        @Test
        fun `skal ikke sende på nytt for samme behandlingsnummer når allerede sendt`() {
            mockKafkaSuccess()
            val sak = lagreSak()

            utbetalingService.sendTilUtbetaling(sak)
            clearInvocations(kafkaTemplate)

            utbetalingService.sendTilUtbetaling(sak)

            verify(kafkaTemplate, never()).send(any<String>(), any<String>(), any<UtbetalingMelding>())
        }
    }

    @Nested
    inner class KafkaFeil {

        @Test
        fun `skal sette status KLAR_TIL_UTBETALING selv om kafka feiler`() {
            mockKafkaFailure()
            val (sak, utbetaling) = lagreSakMedUtbetaling(utbetalingStatus = UtbetalingStatus.UTKAST)

            assertThrows<Exception> { utbetalingService.sendTilUtbetaling(sak) }

            val oppdatertUtbetaling = utbetalingRepository.findByTransaksjonsId(utbetaling.transaksjonsId)
            assertThat(oppdatertUtbetaling?.utbetalingStatus).isEqualTo(UtbetalingStatus.KLAR_TIL_UTBETALING)
            verify(kafkaTemplate).send(
                any<String>(),
                eq(utbetaling.transaksjonsId.toString()),
                any<UtbetalingMelding>()
            )
        }

        @Test
        fun `skal håndtere kafka broker unavailable exception`() {
            mockKafkaFailure(KafkaException("Broker not available"))
            val (sak, utbetaling) = lagreSakMedUtbetaling(utbetalingStatus = UtbetalingStatus.UTKAST)

            assertThrows<Exception> { utbetalingService.sendTilUtbetaling(sak) }

            val oppdatertUtbetaling = utbetalingRepository.findByTransaksjonsId(utbetaling.transaksjonsId)
            assertThat(oppdatertUtbetaling?.utbetalingStatus).isEqualTo(UtbetalingStatus.KLAR_TIL_UTBETALING)
        }

        @Test
        fun `skal håndtere serialization exception fra kafka`() {
            mockKafkaFailure(RuntimeException("Serialization failed"))
            val (sak, utbetaling) = lagreSakMedUtbetaling(utbetalingStatus = UtbetalingStatus.UTKAST)

            assertThrows<Exception> { utbetalingService.sendTilUtbetaling(sak) }

            val oppdatertUtbetaling = utbetalingRepository.findByTransaksjonsId(utbetaling.transaksjonsId)
            assertThat(oppdatertUtbetaling?.utbetalingStatus).isEqualTo(UtbetalingStatus.KLAR_TIL_UTBETALING)
        }
    }

    @Nested
    inner class Gjenapning {

        @Test
        fun `skal opprette ny utbetaling med nytt behandlingsnummer`() {
            mockKafkaSuccess()
            val (sak, gammelUtbetaling) = lagreSakMedUtbetaling(utbetalingStatus = UtbetalingStatus.UTBETALT)
            val gjenapnetSak = withMockedUser { sakRepository.incrementBehandlingsNummer(sak.saksnummer) }

            utbetalingService.sendTilUtbetaling(gjenapnetSak)

            val gammelOppdatert = utbetalingRepository.findByTransaksjonsId(gammelUtbetaling.transaksjonsId)
            assertThat(gammelOppdatert?.utbetalingStatus).isEqualTo(UtbetalingStatus.UTBETALT)

            val nyUtbetaling = utbetalingRepository.findActiveByBehandling(gjenapnetSak)!!
            assertThat(nyUtbetaling).isNotNull
            assertThat(nyUtbetaling.behandlingsnummer).isEqualTo(gjenapnetSak.behandlingsnummer)
            assertThat(nyUtbetaling.transaksjonsId).isNotEqualTo(gammelUtbetaling.transaksjonsId)
            assertThat(nyUtbetaling.utbetalingsUuid).isEqualTo(gammelUtbetaling.utbetalingsUuid)
            assertThat(nyUtbetaling.utbetalingTidspunkt).isEqualTo(gammelUtbetaling.utbetalingTidspunkt)
            assertThat(nyUtbetaling.utbetalingStatus).isEqualTo(UtbetalingStatus.SENDT_TIL_UTBETALING)
            verify(kafkaTemplate).send(
                any<String>(),
                eq(nyUtbetaling.transaksjonsId.toString()),
                argThat { melding -> melding.id == nyUtbetaling.utbetalingsUuid })

        }

        @EnumSource(names = ["AVSLATT", "HENLAGT"])
        @ParameterizedTest
        fun `skal annullere ved gjenåpning og vedtaksresultat`(resultat: VedtaksResultat) {
            mockKafkaSuccess()
            val (sak, gammelUtbetaling) = lagreSakMedUtbetaling(utbetalingStatus = UtbetalingStatus.UTBETALT)
            withMockedUser { sakRepository.updateSak(sak.saksnummer, UpdateSakDto(vedtaksResultat = resultat)) }
            val gjenapnetSak = withMockedUser { sakRepository.incrementBehandlingsNummer(sak.saksnummer) }

            utbetalingService.sendTilUtbetaling(gjenapnetSak)

            val annullering = utbetalingRepository.findActiveByBehandling(gjenapnetSak)!!
            assertThat(annullering).isNotNull
            assertThat(annullering.belop).isEqualTo(Belop.ZeroBelop)
            assertThat(annullering.utbetalingStatus).isEqualTo(UtbetalingStatus.SENDT_TIL_UTBETALING)
            assertThat(annullering.transaksjonsId).isNotEqualTo(gammelUtbetaling.transaksjonsId)
            assertThat(annullering.utbetalingsUuid).isEqualTo(gammelUtbetaling.utbetalingsUuid)
            assertThat(annullering.utbetalingTidspunkt).isEqualTo(gammelUtbetaling.utbetalingTidspunkt)
            verify(kafkaTemplate).send(
                any<String>(),
                eq(annullering.transaksjonsId.toString()),
                argThat { melding -> melding.id == annullering.utbetalingsUuid && melding.perioder.isEmpty() })
        }

        @EnumSource(names = ["INNVILGET", "DELVIS_INNVILGET"])
        @ParameterizedTest
        fun `skal opprette ny utbetaling ved gjenåpning uten tidligere utbetaling og vedtaksresultat`(resultat: VedtaksResultat) {
            mockKafkaSuccess()
            val sak = lagreSak(vedtaksResultat = resultat)
            withMockedUser { sakRepository.updateSak(sak.saksnummer, UpdateSakDto(vedtaksResultat = resultat)) }
            val gjenapnetSak = withMockedUser { sakRepository.incrementBehandlingsNummer(sak.saksnummer) }

            utbetalingService.sendTilUtbetaling(gjenapnetSak)

            val nyUtbetaling = utbetalingRepository.findActiveByBehandling(gjenapnetSak)
            assertThat(nyUtbetaling).isNotNull()
            assertThat(nyUtbetaling?.utbetalingTidspunkt).isNotNull
            assertThat(nyUtbetaling!!.utbetalingStatus).isEqualTo(UtbetalingStatus.SENDT_TIL_UTBETALING)
            verify(kafkaTemplate).send(any<String>(), any<String>(), any<UtbetalingMelding>())
        }

        @EnumSource(names = ["INNVILGET", "DELVIS_INNVILGET"])
        @ParameterizedTest
        fun `skal opprette ny utbetaling ved gjenåpning med tidligere utbetaling og vedtaksresultat`(resultat: VedtaksResultat) {
            mockKafkaSuccess()
            val (sak, gammelUtbetaling) = lagreSakMedUtbetaling(utbetalingStatus = UtbetalingStatus.UTBETALT)
            withMockedUser { sakRepository.updateSak(sak.saksnummer, UpdateSakDto(vedtaksResultat = resultat)) }
            val gjenapnetSak = withMockedUser { sakRepository.incrementBehandlingsNummer(sak.saksnummer) }

            utbetalingService.sendTilUtbetaling(gjenapnetSak)

            val nyUtbetaling = utbetalingRepository.findActiveByBehandling(gjenapnetSak)!!
            assertThat(nyUtbetaling.transaksjonsId).isNotEqualTo(gammelUtbetaling.transaksjonsId)
            assertThat(nyUtbetaling.utbetalingsUuid).isEqualTo(gammelUtbetaling.utbetalingsUuid)
            assertThat(nyUtbetaling.utbetalingTidspunkt).isEqualTo(gammelUtbetaling.utbetalingTidspunkt)
            assertThat(nyUtbetaling.utbetalingStatus).isEqualTo(UtbetalingStatus.SENDT_TIL_UTBETALING)
            assertThat(nyUtbetaling.belop.value).isGreaterThan(0)
            verify(kafkaTemplate).send(any<String>(), eq(nyUtbetaling.transaksjonsId.toString()), argThat { melding ->
                melding.perioder.isNotEmpty()
            })
        }

        @EnumSource(names = ["AVSLATT", "HENLAGT"])
        @ParameterizedTest
        fun `skal ignorere ved gjenåpning uten tidligere utbetaling og vedtaksresultat`(resultat: VedtaksResultat) {
            mockKafkaSuccess()
            val sak = lagreSak()
            withMockedUser { sakRepository.updateSak(sak.saksnummer, UpdateSakDto(vedtaksResultat = resultat)) }
            val gjenapnetSak = withMockedUser { sakRepository.incrementBehandlingsNummer(sak.saksnummer) }

            utbetalingService.sendTilUtbetaling(gjenapnetSak)

            verify(kafkaTemplate, never()).send(any<String>(), any(), any())
        }


        @Test
        fun `skal annullere ved INNVILGET vedtaksresultat og forhandstilsagn`() {
            mockKafkaSuccess()
            val (sak, tidligereUtbetaling) = lagreSakMedUtbetaling(utbetalingStatus = UtbetalingStatus.UTBETALT)
            withMockedUser {
                sakRepository.updateSak(
                    sak.saksnummer,
                    UpdateSakDto(
                        vedtaksResultat = VedtaksResultat.INNVILGET,
                        utbetalingsType = UtbetalingsType.FORHANDSTILSAGN
                    )
                )
            }
            val gjenapnetSak = withMockedUser { sakRepository.incrementBehandlingsNummer(sak.saksnummer) }

            utbetalingService.sendTilUtbetaling(gjenapnetSak)

            val annullering = utbetalingRepository.findActiveByBehandling(gjenapnetSak)
            assertThat(annullering).isNotNull
            assertThat(annullering!!.annulleres).isTrue()
            assertThat(annullering.utbetalingsUuid).isEqualTo(tidligereUtbetaling.utbetalingsUuid)
            assertThat(annullering.transaksjonsId).isNotEqualTo(tidligereUtbetaling.transaksjonsId)
            assertThat(annullering.utbetalingTidspunkt).isEqualTo(tidligereUtbetaling.utbetalingTidspunkt)
            verify(kafkaTemplate).send(
                any<String>(),
                eq(annullering.transaksjonsId.toString()),
                argThat { melding -> melding.perioder.isEmpty() })
        }
    }

    @Nested
    inner class UpdateUtbetalingsStatus {

        @Test
        fun `skal oppdatere status fra SENDT til MOTTATT`() {
            val (_, utbetaling) = lagreSakMedUtbetaling(utbetalingStatus = UtbetalingStatus.SENDT_TIL_UTBETALING)

            utbetalingService.updateUtbetalingsStatus(utbetaling, UtbetalingStatus.MOTTATT_AV_UTBETALING)

            val oppdatert = utbetalingRepository.findByTransaksjonsId(utbetaling.transaksjonsId)
            assertThat(oppdatert?.utbetalingStatus).isEqualTo(UtbetalingStatus.MOTTATT_AV_UTBETALING)
        }

        @Test
        fun `skal ikke oppdatere status når utbetaling er i final status UTBETALT`() {
            val (_, utbetaling) = lagreSakMedUtbetaling(utbetalingStatus = UtbetalingStatus.UTBETALT)

            utbetalingService.updateUtbetalingsStatus(utbetaling, UtbetalingStatus.MOTTATT_AV_UTBETALING)

            val oppdatert = utbetalingRepository.findByTransaksjonsId(utbetaling.transaksjonsId)
            assertThat(oppdatert?.utbetalingStatus).isEqualTo(UtbetalingStatus.UTBETALT)
            verify(sakEndringsloggService, never()).logChange(any(), any(), any(), any(), anyOrNull(), any())
        }

        @Test
        fun `skal ikke oppdatere status når utbetaling er i final status FEILET`() {
            val (_, utbetaling) = lagreSakMedUtbetaling(utbetalingStatus = UtbetalingStatus.FEILET)

            utbetalingService.updateUtbetalingsStatus(utbetaling, UtbetalingStatus.BEHANDLET_AV_UTBETALING)

            val oppdatert = utbetalingRepository.findByTransaksjonsId(utbetaling.transaksjonsId)
            assertThat(oppdatert?.utbetalingStatus).isEqualTo(UtbetalingStatus.FEILET)
            verify(sakEndringsloggService, never()).logChange(any(), any(), any(), any(), anyOrNull(), any())
        }

        @Test
        fun `skal ikke oppdatere status når ny status er lavere enn eksisterende`() {
            val (_, utbetaling) = lagreSakMedUtbetaling(utbetalingStatus = UtbetalingStatus.MOTTATT_AV_UTBETALING)

            utbetalingService.updateUtbetalingsStatus(utbetaling, UtbetalingStatus.SENDT_TIL_UTBETALING)

            val oppdatert = utbetalingRepository.findByTransaksjonsId(utbetaling.transaksjonsId)
            assertThat(oppdatert?.utbetalingStatus).isEqualTo(UtbetalingStatus.MOTTATT_AV_UTBETALING)
            verify(sakEndringsloggService, never()).logChange(any(), any(), any(), any(), anyOrNull(), any())
        }

        @Test
        fun `skal oppdatere til BEHANDLET_AV_UTBETALING uten endringslogg`() {
            val (_, utbetaling) = lagreSakMedUtbetaling(utbetalingStatus = UtbetalingStatus.MOTTATT_AV_UTBETALING)

            utbetalingService.updateUtbetalingsStatus(utbetaling, UtbetalingStatus.BEHANDLET_AV_UTBETALING)

            val oppdatert = utbetalingRepository.findByTransaksjonsId(utbetaling.transaksjonsId)
            assertThat(oppdatert?.utbetalingStatus).isEqualTo(UtbetalingStatus.BEHANDLET_AV_UTBETALING)
            verify(sakEndringsloggService, never()).logChange(any(), any(), any(), any(), anyOrNull(), any())
        }

        @Test
        fun `skal logge endringslogg UTBETALING_OK og oppdatere status ved UTBETALT`() {
            val (_, utbetaling) = lagreSakMedUtbetaling(utbetalingStatus = UtbetalingStatus.SENDT_TIL_UTBETALING)

            utbetalingService.updateUtbetalingsStatus(utbetaling, UtbetalingStatus.UTBETALT)

            val oppdatert = utbetalingRepository.findByTransaksjonsId(utbetaling.transaksjonsId)
            assertThat(oppdatert?.utbetalingStatus).isEqualTo(UtbetalingStatus.UTBETALT)
            verify(sakEndringsloggService).logChange(
                saksnummer = eq(utbetaling.saksnummer),
                endringsType = eq(EndringsloggType.UTBETALING_OK),
                endring = any(),
                navBruker = any(),
                beskrivelse = anyOrNull(),
                tidspunkt = any()
            )
        }

        @Test
        fun `skal logge endringslogg UTBETALING_FEILET og oppdatere status ved FEILET`() {
            val (_, utbetaling) = lagreSakMedUtbetaling(utbetalingStatus = UtbetalingStatus.SENDT_TIL_UTBETALING)

            utbetalingService.updateUtbetalingsStatus(utbetaling, UtbetalingStatus.FEILET)

            val oppdatert = utbetalingRepository.findByTransaksjonsId(utbetaling.transaksjonsId)
            assertThat(oppdatert?.utbetalingStatus).isEqualTo(UtbetalingStatus.FEILET)
            verify(sakEndringsloggService).logChange(
                saksnummer = eq(utbetaling.saksnummer),
                endringsType = eq(EndringsloggType.UTBETALING_FEILET),
                endring = any(),
                navBruker = any(),
                beskrivelse = anyOrNull(),
                tidspunkt = any()
            )
        }
    }

    @Nested
    inner class RetryUtbetaling {

        @Test
        fun `skal sende utbetaling på nytt ved FEILET status`() {
            mockKafkaSuccess()
            val (sak, utbetaling) = lagreSakMedUtbetaling(utbetalingStatus = UtbetalingStatus.FEILET)

            utbetalingService.retryUtbetaling(sak)

            verify(kafkaTemplate).send(
                any<String>(),
                eq(utbetaling.transaksjonsId.toString()),
                any<UtbetalingMelding>()
            )
            val oppdatert = utbetalingRepository.findByTransaksjonsId(utbetaling.transaksjonsId)
            assertThat(oppdatert?.utbetalingStatus).isEqualTo(UtbetalingStatus.SENDT_TIL_UTBETALING)
        }

        @Test
        fun `skal kaste feil når utbetaling ikke finnes for sak`() {
            val sak = lagreSak()

            assertThrows<IllegalStateException> { utbetalingService.retryUtbetaling(sak) }

            verify(kafkaTemplate, never()).send(any<String>(), any<String>(), any<UtbetalingMelding>())
        }

        @Test
        fun `skal kaste feil når utbetaling ikke er i FEILET status`() {
            val (sak, _) = lagreSakMedUtbetaling(utbetalingStatus = UtbetalingStatus.SENDT_TIL_UTBETALING)

            assertThrows<IllegalStateException> { utbetalingService.retryUtbetaling(sak) }

            verify(kafkaTemplate, never()).send(any<String>(), any<String>(), any<UtbetalingMelding>())
        }
    }


}
