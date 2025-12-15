package no.nav.historisk.superhelt.dokarkiv.config

import no.nav.dokdist.DokdistClient
import no.nav.historisk.superhelt.infrastruktur.mdc.CallIdClientRequestInterceptor
import no.nav.historisk.superhelt.infrastruktur.token.NaisTokenClientRequestInterceptor
import no.nav.historisk.superhelt.infrastruktur.token.NaisTokenService
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(DokdistProperties::class)
class DokdistConfig {

    @Bean
    internal fun dokdistClient(properties: DokdistProperties , tokenService: NaisTokenService): DokdistClient {
        val restClient = RestClient.builder()
            .baseUrl(properties.url)
            .requestInterceptor(CallIdClientRequestInterceptor("Nav-Callid"))
            .requestInterceptor(NaisTokenClientRequestInterceptor(tokenService, properties.audience))
            .build()

        return DokdistClient(restClient)
    }


}

@ConfigurationProperties(prefix = "app.dokdist")
internal data class DokdistProperties(
    val url: String,
    /** api://<cluster>.<namespace>.<other-api-app-name>/.default The intended audience (target API or recipient) of the new token. */
    val audience: String,
)

