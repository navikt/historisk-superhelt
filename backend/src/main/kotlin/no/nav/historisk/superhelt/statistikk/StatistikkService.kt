package no.nav.historisk.superhelt.statistikk

import no.nav.common.types.defaultEnhetsnummer
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.statistikk.kafka.SakStatistikkKafkaProducer
import no.nav.historisk.superhelt.vedtak.VedtaksResultat
import no.nav.sakstatistikk.BehandlingStatus
import no.nav.sakstatistikk.BehandlingType
import no.nav.sakstatistikk.SaksbehandlingsStatistikk
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneOffset


@Service
class StatistikkService(
    private val statistikkKafkaProducer: SakStatistikkKafkaProducer,
    @param:Value("\${info.nais.image}") private val appVersion: String) {

    private val logger = LoggerFactory.getLogger(StatistikkService::class.java)

    /** Sender statistikk failsafe */
    fun handleEvent(endringsType: EndringsloggType, tidspunkt: Instant, sak: Sak) {
        try {
            val statistikk = statistikkContent(sak, tidspunkt, endringsType)
            statistikk?.let { statistikkKafkaProducer.registrerStatistikk(it) }
        } catch (t: Throwable) {
            logger.error("Feil ved håndtering av statistikk for sak ${sak.saksnummer.value} og endringstype $endringsType", t)
        }
    }

    private fun statistikkContent(
        sak: Sak,
        tidspunkt: Instant,
        endringsType: EndringsloggType): SaksbehandlingsStatistikk? {

        val statistikk = SaksbehandlingsStatistikk(
            saksnummer = sak.saksnummer.value,
            behandlingId = "${sak.saksnummer}-${sak.behandlingsnummer}",
            behandlingStatus = BehandlingStatus.UNDER_BEHANDLING,
            fnr = sak.fnr.value,
            endretTid = tidspunkt,
            registrertTid = sak.opprettetDato,
            mottattTid = finnMottattTid(sak),
            sakYtelse = sak.type,
            behandlingType = BehandlingType.SØKNAD,
            saksbehandler = sak.saksbehandler.navIdent.value,
            ansvarligEnhet = defaultEnhetsnummer.value,
            fagsystemNavn = "SUPERHELT",
            fagsystemVersjon = appVersion
        )
        return when (endringsType) {
            EndringsloggType.DOKUMENT_MOTTATT -> statistikk.copy(
                behandlingStatus = BehandlingStatus.MOTTATT,
                mottattTid = tidspunkt,
                endretTid = Instant.now()
            )

            EndringsloggType.OPPRETTET_SAK -> statistikk.copy(
                behandlingStatus = BehandlingStatus.OPPRETTET,
                opprettetAv = sak.saksbehandler.navIdent.value
            )

            EndringsloggType.TIL_ATTESTERING -> statistikk.copy(
                behandlingStatus = BehandlingStatus.TIL_ATTESTERING,
            )

            EndringsloggType.ATTESTERT_SAK -> statistikk.copy(
                behandlingStatus = BehandlingStatus.ATTESTERT,
                ansvarligBeslutter = sak.attestant?.navIdent?.value
            )

            EndringsloggType.FERDIGSTILT_SAK -> statistikk.copy(
                behandlingStatus = BehandlingStatus.FERDIG,
                behandlingResultat = sak.vedtaksResultat,
                ferdigBehandletTid = tidspunkt,
                ansvarligBeslutter = sak.attestant?.navIdent?.value
            )

            EndringsloggType.ATTESTERING_UNDERKJENT -> statistikk.copy(
                behandlingStatus = BehandlingStatus.UNDER_BEHANDLING,
                ansvarligBeslutter = sak.attestant?.navIdent?.value
            )

            EndringsloggType.GJENAPNET_SAK -> statistikk.copy(
                behandlingStatus = BehandlingStatus.OPPRETTET,
                opprettetAv = sak.saksbehandler.navIdent.value,
                behandlingType = BehandlingType.REVURDERING,
            )

            EndringsloggType.FEILREGISTERT -> statistikk.copy(
                behandlingStatus = BehandlingStatus.FERDIG,
                behandlingResultat = SakStatus.FEILREGISTRERT,
                ferdigBehandletTid = tidspunkt
            )

            EndringsloggType.HENLAGT_SAK -> statistikk.copy(
                behandlingStatus = BehandlingStatus.FERDIG,
                behandlingResultat = VedtaksResultat.HENLAGT,
                ferdigBehandletTid = tidspunkt
            )

            EndringsloggType.TILBAKESTILT_SAK -> statistikk.copy(
                behandlingStatus = BehandlingStatus.ANNULLERT,
                behandlingResultat = EndringsloggType.TILBAKESTILT_SAK,
                ferdigBehandletTid = tidspunkt
            )

            else -> null
        }
    }

    /** Mottatttid skal attid med. Den må være før registrert tid */
    private fun finnMottattTid(sak: Sak): Instant {
        val registertTid = sak.opprettetDato
        val mottattTid = sak.soknadsDato?.atTime(8, 0)?.toInstant(ZoneOffset.UTC) ?: registertTid
        if (mottattTid.isBefore(registertTid)) {
            return mottattTid
        }
        return registertTid
    }
}

