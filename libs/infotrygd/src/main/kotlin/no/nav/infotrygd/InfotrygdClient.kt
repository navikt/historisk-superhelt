package no.nav.entraproxy

import no.nav.common.types.FolkeregisterIdent
import no.nav.infotrygd.InfotrygdHistorikk
import no.nav.infotrygd.InfotrygdHistorikkRequest
import no.nav.infotrygd.InfotrygdHistorikkResponse
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class InfotrygdClient(private val restClient: RestClient) {

    fun hentHistorikk(fnr: FolkeregisterIdent): List<InfotrygdHistorikk> {
        val response = restClient.post()
            .uri("/api/hentData")
            .body(
                InfotrygdHistorikkRequest(
                    fnr = setOf(fnr)
                )
            )
            .retrieve()
            .body<InfotrygdHistorikkResponse>()!!
        return response.personkort.map {
            InfotrygdHistorikk(
                dato = it.dato,
                fom = it.fom,
                tom = it.tom,
                tekst = it.tekst,
                kontonummer = it.kontonummer,
                belop = it.bevilgetBelop ?: it.betaltBelop,
            )
        }
    }


}
