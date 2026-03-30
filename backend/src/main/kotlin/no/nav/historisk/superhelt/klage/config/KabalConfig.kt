package no.nav.historisk.superhelt.klage.config

import no.nav.historisk.superhelt.infrastruktur.http.RequestBodyLoggingInterceptor
import no.nav.historisk.superhelt.infrastruktur.mdc.CallIdClientRequestInterceptor
import no.nav.historisk.superhelt.infrastruktur.token.NaisTokenClientRequestInterceptor
import no.nav.historisk.superhelt.infrastruktur.token.NaisTokenService
import no.nav.kabal.KabalClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(KabalProperties::class)
class KabalConfig(
    @Value("\${APP_ENV:local}") private val env: String,
) {

    @Bean
    fun kabalClient(
        properties: KabalProperties,
        tokenService: NaisTokenService,
        builder: RestClient.Builder,
    ): KabalClient {
        val restClientBuilder = builder
            .baseUrl(properties.url)
            .requestInterceptor(CallIdClientRequestInterceptor("X-Correlation-ID"))
            .requestInterceptor(NaisTokenClientRequestInterceptor(tokenService, properties.audience))

        if (env != "prod") {
            restClientBuilder.requestInterceptor(RequestBodyLoggingInterceptor())
        }

        return KabalClient(restClientBuilder.build())
    }
}

@ConfigurationProperties(prefix = "app.kabal")
data class KabalProperties(
    val url: String,
    /** api://<cluster>.<namespace>.<other-api-app-name>/.default */
    val audience: String,
)

