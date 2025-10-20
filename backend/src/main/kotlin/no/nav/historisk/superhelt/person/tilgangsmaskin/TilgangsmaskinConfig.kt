package no.nav.historisk.superhelt.person.tilgangsmaskin


import no.nav.historisk.superhelt.infrastruktur.token.NaisTokenClientRequestInterceptor
import no.nav.historisk.superhelt.infrastruktur.token.NaisTokenService
import no.nav.tilgangsmaskin.TilgangsmaskinClient
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(TilgangsmaskinProperties::class)
class TilgangsmaskinConfig() {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun tilgangsmaskinService(properties: TilgangsmaskinProperties, tokenService: NaisTokenService): TilgangsmaskinService {

        val restClient = RestClient.builder()
            .baseUrl(properties.url)
//            .requestInterceptor (CallIdClientRequestInterceptor("Nav-Call-Id"))
            .requestInterceptor (NaisTokenClientRequestInterceptor(tokenService, properties.audience))
            .build()

        val tilgangsmaskinClient = TilgangsmaskinClient(restClient = restClient)
        return TilgangsmaskinService(tilgangsmaskinClient)
    }
}


@ConfigurationProperties(prefix = "app.tilgangsmaskin")
data class TilgangsmaskinProperties(
    val url: String,
    /** api://<cluster>.<namespace>.<other-api-app-name>/.default The intended audience (target API or recipient) of the new token. */
    val audience: String,
)


