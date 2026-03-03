package no.nav.historisk.superhelt.brev

import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.utbetaling.UtbetalingsType
import java.time.format.DateTimeFormatter
import java.util.*

class BrevTekstGenerator(private val sak: Sak) {

    private val navDateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd. MMMM yyyy", Locale.forLanguageTag("no"))

    fun generateInnhold(type: BrevType, mottaker: BrevMottaker): String {
        return when (type) {
            BrevType.VEDTAKSBREV -> generateVedtaksbrev(mottaker)
            BrevType.FRITEKSTBREV -> ""
            BrevType.HENLEGGESEBREV -> generateHenleggelsebrev()
        }
    }

    private fun generateHenleggelsebrev(): String {
        return """
            <h1>Søknad om ${sak.type.navn.lowercase()} er henlagt</h1>
            <p>Søknad datert ${sak.soknadsDato?.format(navDateFormatter)} om ${sak.type.navn.lowercase()} er henlagt.</p>
        """.trimIndent()
    }

    private fun generateVedtaksbrev(mottaker: BrevMottaker): String {
        when (mottaker) {
            BrevMottaker.BRUKER, BrevMottaker.VERGE -> {
                val showUtbetaling =
                    sak.utbetalingsType == UtbetalingsType.BRUKER && sak.vedtaksResultat?.isInnvilget() == true && sak.belop != null

                return """
                    <h1>Vedtak om ${sak.type.navn.lowercase()}</h1>
                    <p>Søknaden din av ${sak.soknadsDato?.format(navDateFormatter)} om ${sak.type.navn.lowercase()} er ${sak.vedtaksResultat?.navn}.</p>
                    <p></p>
                    ${if (showUtbetaling) "<p>Vi har utbetalt ${sak.belop} kr til konto som er registrert hos Nav. Pengene vil være på din konto innen 1-4 virkedager</p>" else ""}
                           
                """.trimIndent()
            }

            BrevMottaker.SAMHANDLER -> {
                return """
                    <h1>Informasjon til samhandler</h1>
                    <p>Søknaden av  ${sak.soknadsDato?.format(navDateFormatter)} om ${sak.type.navn.lowercase()} er ${sak.vedtaksResultat?.navn}</p>
         
                    <p>For ${sak.tildelingsAar} får du</p>
                    <ul>
                        ${if (sak.utbetalingsType == UtbetalingsType.BRUKER && sak.belop != null) "<li>Utbetaling: ${sak.belop} kr</li>" else ""}
                        ${if (sak.utbetalingsType == UtbetalingsType.FORHANDSTILSAGN && sak.belop != null) "<li>Forhåndstilsagn på inntil ${sak.belop} kr</li>" else ""}
                      </ul> 
                """.trimIndent()
            }
        }
    }

    fun generateTittel(type: BrevType, mottaker: BrevMottaker): String {
        return when (type) {
            BrevType.VEDTAKSBREV -> generateVedtaksbrevTittel(mottaker)
            BrevType.HENLEGGESEBREV -> "Henleggelsebrev for ${sak.type.navn.lowercase()}"
            BrevType.FRITEKSTBREV -> ""
        }
    }

    private fun generateVedtaksbrevTittel(mottaker: BrevMottaker): String {
        return when (mottaker) {
            BrevMottaker.BRUKER, BrevMottaker.VERGE -> {
                if (sak.gjenapnet) {
                    "Korrigert vedtaksbrev om ${sak.type.navn.lowercase()}, ${sak.vedtaksResultat?.navn?.lowercase()}"
                } else {
                    "Vedtaksbrev om ${sak.type.navn.lowercase()}, ${sak.vedtaksResultat?.navn?.lowercase()}"
                }
            }

            BrevMottaker.SAMHANDLER -> "Vedtaksbrev til samhandler om ${sak.type.navn.lowercase()}, ${sak.vedtaksResultat?.navn?.lowercase()}"
        }
    }

}
