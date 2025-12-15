package no.nav.historisk.superhelt.dokarkiv.config


import no.nav.historisk.superhelt.infrastruktur.mdc.CallIdClientRequestInterceptor
import no.nav.historisk.superhelt.infrastruktur.token.NaisTokenClientRequestInterceptor
import no.nav.historisk.superhelt.infrastruktur.token.NaisTokenService
import no.nav.pdl.SafGraphqlClient
import no.nav.saf.rest.SafRestClient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(SafProperties::class)
class SafConfig(private val properties: SafProperties, private val tokenService: NaisTokenService) {

    @Bean
    fun safGraphqlClient(): SafGraphqlClient = SafGraphqlClient(restClient())

    @Bean
    fun safRestClient(): SafRestClient = SafRestClient(restClient())


    private fun restClient(): RestClient = RestClient.builder()
        .baseUrl(properties.url)
        .requestInterceptor(CallIdClientRequestInterceptor("Nav-Callid"))
        .requestInterceptor(NaisTokenClientRequestInterceptor(tokenService, properties.audience))
        .build()

}

@ConfigurationProperties(prefix = "app.saf")
data class SafProperties(
    val url: String,
    /** api://<cluster>.<namespace>.<other-api-app-name>/.default The intended audience (target API or recipient) of the new token. */
    val audience: String,
)




