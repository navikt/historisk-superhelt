package no.nav.infotrygd

import no.nav.common.types.FolkeregisterIdent
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
                kontonummer = it.kontonummer ?: "-1",
                kontonavn = InfotrygdKontonummer.fraKode(it.kontonummer).navn,
                belop = it.bevilgetBelop ?: it.betaltBelop,
            )
        }
    }
}
