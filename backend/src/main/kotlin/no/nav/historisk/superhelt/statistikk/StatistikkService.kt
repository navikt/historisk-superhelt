package no.nav.historisk.superhelt.statistikk

import no.nav.common.types.defaultEnhetsnummer
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.statistikk.kafka.SakStatistikkKafkaProducer
import no.nav.sakstatistikk.BehandlingType
import no.nav.sakstatistikk.SaksbehandlingsStatistikk
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant

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
            behandlingId = sak.behandlingsnummer.value.toString(),
            behandlingStatus = endringsType,
            sakId = sak.saksnummer.value,
            aktorId = sak.fnr.value,
            endretTid = tidspunkt,
            sakYtelse = sak.type,
            behandlingType = BehandlingType.SØKNAD,
            saksbehandler = sak.saksbehandler.navIdent.value,
            ansvarligEnhet = defaultEnhetsnummer.value,
            fagsystemNavn = "SUPERHELT",
            fagsystemVersjon = appVersion
        )
        return when (endringsType) {
            EndringsloggType.DOKUMENT_MOTTATT -> statistikk.copy(mottattTid = tidspunkt, endretTid = Instant.now())
            EndringsloggType.OPPRETTET_SAK -> statistikk.copy(registrertTid = tidspunkt, opprettetAv = sak.saksbehandler.navIdent.value)
            EndringsloggType.TIL_ATTESTERING -> statistikk.copy(behandlingResultat = sak.vedtaksResultat)
            EndringsloggType.ATTESTERT_SAK -> statistikk.copy(
                behandlingResultat = sak.vedtaksResultat,
                ansvarligBeslutter = sak.attestant?.navIdent?.value
            )

            EndringsloggType.FERDIGSTILT_SAK -> statistikk.copy(
                behandlingStatus = SakStatus.FERDIG,
                behandlingResultat = sak.vedtaksResultat,
                ferdigBehandletTid = tidspunkt,
                ansvarligBeslutter = sak.attestant?.navIdent?.value
            )

            EndringsloggType.ATTESTERING_UNDERKJENT -> statistikk.copy(
                behandlingResultat = sak.vedtaksResultat,
                ansvarligBeslutter = sak.attestant?.navIdent?.value
            )

            EndringsloggType.GJENAPNET_SAK -> statistikk.copy(
                registrertTid = tidspunkt,
                opprettetAv = sak.saksbehandler.navIdent.value,
                behandlingType = BehandlingType.REVURDERING
            )

            EndringsloggType.SENDT_BREV -> null
            EndringsloggType.UTBETALING_OK -> null
            EndringsloggType.UTBETALING_FEILET -> null
            EndringsloggType.FEILREGISTERT -> statistikk.copy(
                behandlingStatus = SakStatus.FERDIG,
                behandlingResultat = SakStatus.FEILREGISTRERT,
                ferdigBehandletTid = tidspunkt)
            EndringsloggType.HENLAGT_SAK -> statistikk.copy(ferdigBehandletTid = tidspunkt)
            EndringsloggType.TILBAKESTILT_SAK -> statistikk.copy(
                behandlingStatus = SakStatus.FERDIG,
                behandlingResultat = EndringsloggType.TILBAKESTILT_SAK,
                ferdigBehandletTid = tidspunkt
            )
        }
    }
}
