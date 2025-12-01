package no.nav.historisk.superhelt.brev.pdfgen


import no.nav.historisk.pdfgen.PdfgenClient
import no.nav.historisk.superhelt.infrastruktur.mdc.CallIdClientRequestInterceptor
import no.nav.historisk.superhelt.infrastruktur.token.NaisTokenService
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(PdfgenProperties::class)
class PdfgenConfig() {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun pdfgenClient(properties: PdfgenProperties, tokenService: NaisTokenService): PdfgenClient {

        val restClient = RestClient.builder()
            .baseUrl(properties.url)
            .requestInterceptor (CallIdClientRequestInterceptor("Nav-Call-Id"))
//            .requestInterceptor (NaisTokenClientRequestInterceptor(tokenService, properties.audience))
            .build()

        return PdfgenClient(restClient = restClient)
    }
}


@ConfigurationProperties(prefix = "app.pdfgen")
data class PdfgenProperties(
    val url: String,
    /** api://<cluster>.<namespace>.<other-api-app-name>/.default The intended audience (target API or recipient) of the new token. */
//    val audience: String,
)


