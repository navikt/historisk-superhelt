package no.nav.historisk.superhelt.auth.token


import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated

@Configuration
@EnableConfigurationProperties(NaisTokenProperties::class)
@Validated
class NaisTokenConfig() {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun tokenService(naisToken: NaisTokenProperties): NaisTokenService {

        return NaisTokenService(
            oboEndpoint = naisToken.exchange.endpoint,
            m2mEndpoint = naisToken.endpoint
        )
    }
}

/**
 * Samme struktur som nais environment properties https://doc.nais.io/auth/reference/#environment-variables
 */

@ConfigurationProperties(prefix = "nais.token")
data class NaisTokenProperties(
    val endpoint: String,
    @NestedConfigurationProperty
    val exchange: NaisTokenExchangeProperties
)


data class NaisTokenExchangeProperties(
    val endpoint: String
)


