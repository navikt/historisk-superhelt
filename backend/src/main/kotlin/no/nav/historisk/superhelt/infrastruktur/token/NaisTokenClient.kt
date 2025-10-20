package no.nav.historisk.superhelt.infrastruktur.token

import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

class NaisTokenClient(val client: RestClient,
                      val oboEndpoint: String,
                      val m2mEndpoint: String) {


    /** Token on behalf of a user
     *
     * https://doc.nais.io/auth/entra-id/how-to/consume-obo/
     * */
    fun oboToken(target: String, token: String): TexasResponse {
        return client.post()
            .uri(oboEndpoint)
            .contentType(MediaType.APPLICATION_JSON)
            .body(OboRequest(target = target, user_token = token))
            .retrieve()
            .body(TexasResponse::class.java)!!
    }


    /** Token as an application (machine to machine)
     *
     * https://doc.nais.io/auth/entra-id/how-to/consume-m2m/
     * */
    fun m2mToken(target: String): TexasResponse {
        return client.post()
            .uri(m2mEndpoint)
            .contentType(MediaType.APPLICATION_JSON)
            .body(M2MRequest(target = target))
            .retrieve()
            .body(TexasResponse::class.java)!!
    }
}
