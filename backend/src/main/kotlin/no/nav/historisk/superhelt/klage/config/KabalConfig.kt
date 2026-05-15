package no.nav.historisk.superhelt.klage.config

import no.nav.historisk.superhelt.infrastruktur.mdc.CallIdClientRequestInterceptor
import no.nav.historisk.superhelt.infrastruktur.token.M2mNaisTokenClientRequestInterceptor
import no.nav.historisk.superhelt.infrastruktur.token.NaisTokenService
import no.nav.kabal.KabalClient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(KabalProperties::class)
class KabalConfig{

    @Bean
    fun kabalClient(
        properties: KabalProperties,
        tokenService: NaisTokenService,
        builder: RestClient.Builder,
    ): KabalClient {
        val restClientBuilder = builder
            .baseUrl(properties.url)
            .requestInterceptor(CallIdClientRequestInterceptor("X-Correlation-ID"))
            .requestInterceptor(M2mNaisTokenClientRequestInterceptor(tokenService, properties.audience))

        return KabalClient(restClientBuilder.build())
    }
}

@ConfigurationProperties(prefix = "app.kabal")
data class KabalProperties(
    val url: String,
    /** api://<cluster>.<namespace>.<other-api-app-name>/.default */
    val audience: String,
    /** Kafka topic: klage.behandling-events.v1 */
    val behandlingEventTopic: String,
)

