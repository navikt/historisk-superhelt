package no.nav.historisk.superhelt.brev

import no.nav.historisk.superhelt.sak.Sak
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
            <h1>Henleggelsebrev</h1>
            <p>Søknaden av  ${sak.soknadsDato?.format(navDateFormatter)} om ${sak.type.navn.lowercase()} er henlagt.</p
        """.trimIndent()
    }

    private fun generateVedtaksbrev(mottaker: BrevMottaker): String {
        when (mottaker) {
            BrevMottaker.BRUKER -> {
                return """
                    <h1>Vedtaksbrev</h1>
                    <p>Søknaden din av ${sak.soknadsDato?.format(navDateFormatter)} om ${sak.type.navn.lowercase()} er ${sak.vedtaksResultat?.navn}.</p>
                 
                    <p>For ${sak.tildelingsAar} får du</p>
                    <ul>
                        ${if (sak.utbetaling != null) "<li>Utbetaling: ${sak.utbetaling.belop} kr til din konto</li>" else ""}
                        ${if (sak.forhandstilsagn != null) "<li>Forhåndstilsagn: ${sak.forhandstilsagn.belop} kr</li>" else ""}
                      </ul> 
                """.trimIndent()
            }

            BrevMottaker.SAMHANDLER -> {
                return """
                    <h1>Informasjon til samhandler</h1>
                    <p>Søknaden av  ${sak.soknadsDato?.format(navDateFormatter)} om ${sak.type.navn.lowercase()} er ${sak.vedtaksResultat?.navn}</p>
         
                    <p>For ${sak.tildelingsAar} får du</p>
                    <ul>
                        ${if (sak.utbetaling != null) "<li>Utbetaling: ${sak.utbetaling.belop} kr</li>" else ""}
                        ${if (sak.forhandstilsagn != null) "<li>Forhåndstilsagn på inntil ${sak.forhandstilsagn.belop} kr</li>" else ""}
                      </ul> 
                """.trimIndent()
            }
        }
    }

    fun generateTittel(type: BrevType, mottaker: BrevMottaker): String {
        return when (type) {
            BrevType.VEDTAKSBREV -> when (mottaker) {
                BrevMottaker.BRUKER -> "Vedtaksbrev, ${sak.vedtaksResultat?.navn?.lowercase()} for ${sak.type.navn.lowercase()}"
                BrevMottaker.SAMHANDLER -> "Vedtaksbrev til samhandler, ${sak.vedtaksResultat?.navn?.lowercase()} for ${sak.type.navn.lowercase()}"
            }
            BrevType.HENLEGGESEBREV -> "Henleggelsebrev for ${sak.type.navn.lowercase()}"
            BrevType.FRITEKSTBREV -> ""
        }
    }

}
