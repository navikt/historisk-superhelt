package no.nav.historisk.superhelt.infotrygd

import no.nav.entraproxy.InfotrygdClient
import no.nav.historisk.superhelt.infrastruktur.mdc.CallIdClientRequestInterceptor
import no.nav.historisk.superhelt.infrastruktur.token.M2mNaisTokenClientRequestInterceptor
import no.nav.historisk.superhelt.infrastruktur.token.NaisTokenService
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(InfotrygdProperties::class)
class InfotrygdConfig {

    @Bean
    internal fun infotrygdClient(properties: InfotrygdProperties, tokenService: NaisTokenService): InfotrygdClient {
        val restClient = RestClient.builder()
            .baseUrl(properties.url)
            .requestInterceptor(CallIdClientRequestInterceptor("Nav-Call-Id"))
            .requestInterceptor(M2mNaisTokenClientRequestInterceptor(tokenService, properties.audience))
            .build()

        return InfotrygdClient(restClient)
    }
}

@ConfigurationProperties(prefix = "app.infotrygd")
internal data class InfotrygdProperties(
    val url: String,
    /** api://<cluster>.<namespace>.<app-name>/.default */
    val audience: String,
)
