package no.nav.historisk.superhelt.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
//@SecurityScheme(name = "Bearer Authentication", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
class OpenApiConfig{
    @Bean
    fun openApi(): OpenAPI {
        return OpenAPI()
            .security(listOf(SecurityRequirement().addList("bearer-key")))
            .components(
                Components()
                    .addSecuritySchemes(
                        "bearer-key", SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                    )
            )
    }
}