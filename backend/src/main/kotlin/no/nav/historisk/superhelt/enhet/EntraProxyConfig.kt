package no.nav.historisk.superhelt.enhet

import no.nav.entraproxy.EntraProxyClient
import no.nav.historisk.superhelt.infrastruktur.mdc.CallIdClientRequestInterceptor
import no.nav.historisk.superhelt.infrastruktur.token.NaisTokenClientRequestInterceptor
import no.nav.historisk.superhelt.infrastruktur.token.NaisTokenService
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(EntraProxyProperties::class)
class EntraProxyConfig {

    @Bean
    fun entraProxyClient(properties: EntraProxyProperties, tokenService: NaisTokenService): EntraProxyClient {
        val restClient = RestClient.builder()
            .baseUrl(properties.url)
            .requestInterceptor(CallIdClientRequestInterceptor("Nav-Call-Id"))
            .requestInterceptor(NaisTokenClientRequestInterceptor(tokenService, properties.audience))
            .build()

        return EntraProxyClient(restClient)
    }
}

@ConfigurationProperties(prefix = "app.entra-proxy")
data class EntraProxyProperties(
    val url: String,
    /** api://<cluster>.<namespace>.<app-name>/.default */
    val audience: String,
)
