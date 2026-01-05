package no.nav.historisk.superhelt.infrastruktur

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

class GlobalControllerAdviceTest() {

    val mockMvc = MockMvcBuilders.standaloneSetup(TestController())
        .setControllerAdvice(GlobalControllerAdvice())
        .build()

    @Test
    fun `should format exception as JSON with ProblemDetail structure`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/test-exception"))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PROBLEM_JSON))

            // TODO Finne ut om Spring har sluttet å sette "type" feltet automatisk for generelle exceptions
//            .andExpect(MockMvcResultMatchers.jsonPath("$.type").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("RuntimeException"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(500))
            .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Test exception message"))
    }

    @Test
    fun `ingen tilgang`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/noaccess"))
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
//            .andExpect(MockMvcResultMatchers.jsonPath("$.type").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").exists())
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