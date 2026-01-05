package no.nav.historisk.superhelt.person.tilgangsmaskin


import no.nav.historisk.superhelt.infrastruktur.token.NaisTokenClientRequestInterceptor
import no.nav.historisk.superhelt.infrastruktur.token.NaisTokenService
import no.nav.tilgangsmaskin.TilgangsmaskinClient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(TilgangsmaskinProperties::class)
class TilgangsmaskinConfig() {

    @Bean
    fun tilgangsmaskinService(
        properties: TilgangsmaskinProperties,
        tokenService: NaisTokenService,
        cacheManager: CacheManager): TilgangsmaskinService {

        val restClient = RestClient.builder()
            .baseUrl(properties.url)
//            .requestInterceptor (CallIdClientRequestInterceptor("Nav-Call-Id"))
            .requestInterceptor(NaisTokenClientRequestInterceptor(tokenService, properties.audience))
            .build()

        val tilgangsmaskinClient = TilgangsmaskinClient(restClient = restClient)
        val cache = cacheManager.getCache("tilgangsmaskinCache")
            ?: throw IllegalStateException("Cache 'tilgangsmaskinCache' not found")
        return TilgangsmaskinService(tilgangsmaskinClient, cache)
    }
}


@ConfigurationProperties(prefix = "app.tilgangsmaskin")
data class TilgangsmaskinProperties(
    val url: String,
    /** api://<cluster>.<namespace>.<other-api-app-name>/.default The intended audience (target API or recipient) of the new token. */
    val audience: String,
)


