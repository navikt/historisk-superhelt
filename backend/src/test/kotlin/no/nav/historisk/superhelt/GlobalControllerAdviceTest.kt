package no.nav.historisk.superhelt

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


class GlobalControllerAdviceTest() {

    val mockMvc = MockMvcBuilders.standaloneSetup(TestController())
        .setControllerAdvice(GlobalControllerAdvice())
        .build()

    @Test
    fun `should format exception as JSON with ProblemDetail structure`() {
        mockMvc.perform(get("/test-exception"))
            .andExpect(status().isInternalServerError)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").exists())
            .andExpect(jsonPath("$.title").value("RuntimeException"))
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.detail").value("Test exception message"))
    }

    @Test
    fun `ingen tilgang`() {
        mockMvc.perform(get("/noaccess"))
            .andExpect(status().isForbidden)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").exists())
            .andExpect(jsonPath("$.title").exists())
    }

    @RestController
    internal class TestController {
        @GetMapping("/test-exception")
        fun throwException() {
            throw RuntimeException("Test exception message")
        }

        @GetMapping("/noaccess")
        fun noAccess() {
            throw AuthorizationDeniedException("Spring sec Har ikke tilgang")
        }
    }
}
