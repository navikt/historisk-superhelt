package no.nav.historisk.superhelt.dokarkiv.config

import no.nav.dokarkiv.DokarkivClient
import no.nav.historisk.superhelt.infrastruktur.mdc.CallIdClientRequestInterceptor
import no.nav.historisk.superhelt.infrastruktur.token.NaisTokenClientRequestInterceptor
import no.nav.historisk.superhelt.infrastruktur.token.NaisTokenService
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(DokarkivProperties::class)
class DokarkivClientConfig {

    @Bean
    internal fun dokarkivClient(properties: DokarkivProperties, tokenService: NaisTokenService): DokarkivClient {
        val restClient = RestClient.builder()
            .baseUrl(properties.url)
            .requestInterceptor(CallIdClientRequestInterceptor("Nav-Callid"))
            .requestInterceptor(NaisTokenClientRequestInterceptor(tokenService, properties.audience))
            .build()

        return DokarkivClient(restClient)
    }


}

@ConfigurationProperties(prefix = "app.dokarkiv")
internal data class DokarkivProperties(
    val url: String,
    /** api://<cluster>.<namespace>.<other-api-app-name>/.default The intended audience (target API or recipient) of the new token. */
    val audience: String,
)

