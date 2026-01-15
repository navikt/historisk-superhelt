package no.nav.historisk.superhelt.infrastruktur


import no.nav.historisk.superhelt.test.MockedSpringBootTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.assertj.MockMvcTester

/**
 * Tester at OpenAPI (Swagger UI) er tilgjengelig og ser rett ut
 */
@MockedSpringBootTest
@AutoConfigureMockMvc
class OpenApiTest {

    @Autowired
    private lateinit var mockMvc: MockMvcTester


    @Test
    fun `should not forward swagger-ui requests`() {
        assertThat(mockMvc.get().uri("/swagger-ui/index.html"))
            .hasStatusOk()
    }
    @Test
    fun `kotlin inline types should be ok`() {
        assertThat(mockMvc.get().uri("/v3/api-docs"))
            .hasStatusOk()
            .hasContentType(MediaType.APPLICATION_JSON)
            .bodyJson()
            .hasPathSatisfying("components.schemas.Sak.properties.saksnummer.type") { type ->
                assertThat(type).isEqualTo("string")
            }
    }
    @Test
    fun `normal types should be ok`() {
        assertThat(mockMvc.get().uri("/v3/api-docs"))
            .hasStatusOk()
            .hasContentType(MediaType.APPLICATION_JSON)
            .bodyJson()
            .hasPathSatisfying("components.schemas.Sak.properties.status.type") { type ->
                assertThat(type).isEqualTo("string")
            }
    }


}


