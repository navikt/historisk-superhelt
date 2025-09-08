package no.nav.historisk.superhelt

import no.nav.historisk.superhelt.auth.NavJwtAuthenticationConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain


@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val navJwtAuthenticationConverter: NavJwtAuthenticationConverter
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
//                authorize("/actuator/**", permitAll)
//                authorize ( "*.js", permitAll )
//                authorize ( "*.css", permitAll )

                authorize("/api/**", authenticated)
                authorize(anyRequest, permitAll)
            }
            oauth2ResourceServer {
                jwt {
                    jwtAuthenticationConverter = navJwtAuthenticationConverter
                }
            }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
        }
        return http.build()
    }
}