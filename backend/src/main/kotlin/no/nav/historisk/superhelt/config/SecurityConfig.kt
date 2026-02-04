package no.nav.historisk.superhelt.config

import no.nav.historisk.superhelt.infrastruktur.authentication.NavJwtAuthenticationConverter
import no.nav.historisk.superhelt.infrastruktur.authentication.Permission
import no.nav.historisk.superhelt.infrastruktur.authentication.Role
import no.nav.historisk.superhelt.infrastruktur.mdc.MdcFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val navJwtAuthenticationConverter: NavJwtAuthenticationConverter,
    private val mdcFilter: MdcFilter
) {

    private val publicGetPaths = listOf(
        "/actuator/**",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/*.js",
        "/*.css",
        "/*.html",
        "/*.ico",
        "/*.png",
        "/*.jpg",
        "/*.svg",
        "/static/**",
        "/assets/**"
    )

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                publicGetPaths.forEach { path ->
                    authorize(HttpMethod.GET, path, permitAll)
                }

                authorize("/api/user", authenticated)
                authorize("/api/**", hasAuthority(Permission.READ.name))
                authorize("/admin/**", hasRole(Role.DRIFT.name))
                authorize(anyRequest, authenticated)
            }
            oauth2ResourceServer {
                jwt {
                    jwtAuthenticationConverter = navJwtAuthenticationConverter
                }
            }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
            headers {
                frameOptions { sameOrigin = true }
            }
            addFilterAfter<BearerTokenAuthenticationFilter>(mdcFilter)

        }
        return http.build()
    }
}