package no.nav.historisk.superhelt.samhandler

import no.nav.ereg.EregClient
import no.nav.historisk.superhelt.infrastruktur.mdc.CallIdClientRequestInterceptor
import no.nav.historisk.superhelt.infrastruktur.token.NaisTokenClientRequestInterceptor
import no.nav.historisk.superhelt.infrastruktur.token.NaisTokenService
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(EregProperties::class)
class EregConfig {

    @Bean
    fun eregClient(properties: EregProperties, tokenService: NaisTokenService): EregClient {
        val restClient = RestClient.builder()
            .baseUrl(properties.url)
            .requestInterceptor(CallIdClientRequestInterceptor("Nav-Call-Id"))
            .requestInterceptor(NaisTokenClientRequestInterceptor(tokenService, properties.audience))
            .build()

        return EregClient(restClient)
    }
}

@ConfigurationProperties(prefix = "app.ereg")
data class EregProperties(
    val url: String,
    /** api://<cluster>.<namespace>.ereg.ereg-services/.default */
    val audience: String,
)
