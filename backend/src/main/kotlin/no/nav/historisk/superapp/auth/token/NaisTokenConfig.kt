package no.nav.historisk.superapp.auth.token


import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@Configuration
@EnableConfigurationProperties(NaisTokenProperties::class)
@Validated
class NaisTokenConfig() {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun tokenService(naisToken: NaisTokenProperties): NaisTokenService {

        return NaisTokenService(
            oboEndpoint = naisToken.endpoint,
            m2mEndpoint = naisToken.exchange.endpoint
        )
    }
}

/**
 * Samme struktur som nais environment properties https://doc.nais.io/auth/reference/#environment-variables
 */
@Validated
@ConfigurationProperties(prefix = "nais.token")
data class NaisTokenProperties(
    @field:NotBlank(message = "nais.token.endpoint må være satt")
    val endpoint: String,

    @field:Valid
    val exchange: NaisTokenExchangeProperties
)


data class NaisTokenExchangeProperties(
    @field:NotBlank(message = "nais.token.exchange.endpoint må være satt")
    val endpoint: String
)


