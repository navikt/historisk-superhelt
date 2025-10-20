package no.nav.historisk.superhelt.person.pdl


import no.nav.historisk.superhelt.infrastruktur.mdc.CallIdClientRequestInterceptor
import no.nav.historisk.superhelt.infrastruktur.token.NaisTokenClientRequestInterceptor
import no.nav.historisk.superhelt.infrastruktur.token.NaisTokenService
import no.nav.pdl.PdlClient
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(PdlProperties::class)
class PdlConfig() {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun pdlClient(properties: PdlProperties, tokenService: NaisTokenService): PdlClient {

        val restClient = RestClient.builder()
            .baseUrl(properties.url)
            .requestInterceptor (CallIdClientRequestInterceptor("Nav-Call-Id"))
            .requestInterceptor (NaisTokenClientRequestInterceptor(tokenService, properties.audience))
            .build()

        return PdlClient(restClient = restClient,
            behandlingsnummer = properties.behandlingsnummer
        )
    }
}


@ConfigurationProperties(prefix = "app.pdl")
data class PdlProperties(
    val url: String,
    /** api://<cluster>.<namespace>.<other-api-app-name>/.default The intended audience (target API or recipient) of the new token. */
    val audience: String,
    val behandlingsnummer: String
)


