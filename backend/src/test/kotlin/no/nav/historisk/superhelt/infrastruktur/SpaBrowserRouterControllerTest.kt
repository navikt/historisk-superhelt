package no.nav.historisk.superhelt.infrastruktur


import no.nav.historisk.superhelt.test.MockedSpringBootTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.assertj.MockMvcTester
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert


@MockedSpringBootTest
@AutoConfigureMockMvc
@WithMockUser(authorities = ["READ"])
class SpaBrowserRouterControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvcTester

    @ParameterizedTest
    @ValueSource(
        strings = [
            "/saker",
            "/saker/123",
            "/saker/123/detaljer",
            "/bruker/profil"
        ]
    )
    fun `should forward SPA route to index`(route: String) {
        assertThat(mockMvc.get().uri(route))
            .isForwarded()
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "/assets/main.js",
            "/assets/styles.css",
            "/assets/images/logo.png"
        ]
    )
    fun `should not forward asset request`(path: String) {
        assertThat(mockMvc.get().uri(path))
            .isNotForwarded()
    }

    @Test
    fun `should not forward swagger-ui requests`() {
        assertThat(mockMvc.get().uri("/swagger-ui/index.html"))
            .hasStatusOk()
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "/favicon.ico",
            "/index.html",
            "/manifest.json"
        ]
    )
    fun `should not forward static file request`(file: String) {
        assertThat(mockMvc.get().uri(file))
            .isNotForwarded()
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "/api/user"
        ]
    )
    fun `should not forward known api calls`(path: String) {
        assertThat(mockMvc.get().uri(path))
            .isNotForwarded()
    }

}

private fun MvcTestResultAssert.isForwarded(): MvcTestResultAssert {
    viewName().describedAs { "viewName" }.isEqualTo("forward:/")
    forwardedUrl().describedAs { "forwardedUrl" }.isEqualTo("/")
    return this
}

private fun MvcTestResultAssert.isNotForwarded(): MvcTestResultAssert {
    forwardedUrl().describedAs { "forwardedUrl" }.isNullOrEmpty()
    return this
}


