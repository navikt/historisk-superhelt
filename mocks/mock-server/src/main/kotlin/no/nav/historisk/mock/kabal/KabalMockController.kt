package no.nav.historisk.mock.kabal

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("kabal-mock")
class KabalMockController {

    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/api/oversendelse/v4/sak")
    fun sendSak(@RequestBody body: Map<String, Any?>): ResponseEntity<Map<String, String>> {
        logger.info("Kabal mock: mottatt klagebsending: $body")
        return ResponseEntity.ok(mapOf("mottakId" to UUID.randomUUID().toString()))
    }
}

