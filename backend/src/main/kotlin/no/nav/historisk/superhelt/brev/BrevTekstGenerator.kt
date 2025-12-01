package no.nav.historisk.superhelt.brev

import no.nav.historisk.superhelt.sak.Sak

class BrevTekstGenerator(private val sak: Sak) {
    fun generate(type: BrevType, mottaker: BrevMottaker): String {
        return when (type) {
            BrevType.VEDTAKSBREV -> generateVedtaksbrev(mottaker)
            BrevType.INFORMASJONSBREV -> "informasjonsbrev innhold"
            BrevType.INNHENTINGSBREV -> "innhentingsbrev innhold"
        }
    }

    private fun generateVedtaksbrev(mottaker: BrevMottaker): String {
        when (mottaker) {
            BrevMottaker.BRUKER -> {
                return """
                    <h1>Vedtaksbrev</h1>
                    <p>Søknaden din av ${sak.soknadsDato} er ${sak.vedtaksResultat}</p>
                    <p>${sak.tittel}</p>
                    <p>Du får støtte til følgende</p>
                    <ul>
                        ${if (sak.utbetaling != null) "<li>Utbetaling: ${sak.utbetaling?.belop} kr</li>" else ""}
                        ${if (sak.forhandstilsagn != null) "<li>Forhåndstilsagn: ${sak.forhandstilsagn?.belop} kr</li>" else ""}
                      </ul> 
                """.trimIndent()
            }
            BrevMottaker.SAMHANDLER -> {
                return """
                    <h1>Informasjon til samhandler</h1>
                    <p>Søknaden av ${sak.soknadsDato} er ${sak.vedtaksResultat}</p>
                    <p>${sak.tittel}</p>
                    <p>Du får støtte til følgende</p>
                    <ul>
                        ${if (sak.utbetaling != null) "<li>Utbetaling: ${sak.utbetaling?.belop} kr</li>" else ""}
                        ${if (sak.forhandstilsagn != null) "<li>Forhåndstilsagn: ${sak.forhandstilsagn?.belop} kr</li>" else ""}
                      </ul> 
                """.trimIndent()
            }
        }
    }

}
