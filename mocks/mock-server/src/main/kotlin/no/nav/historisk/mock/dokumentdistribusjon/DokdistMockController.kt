package no.nav.historisk.mock.dokumentdistribusjon

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("dokdist-mock")
class DokdistMockController {

    @PostMapping("/rest/v1/distribuerjournalpost")
    fun distribuerJournalpostMock(@RequestBody request: DistribuerJournalpostRequestTo): ResponseEntity<DistribuerJournalpostResponseTo> {
        if (request.journalpostId == "MANGLER-ADRESSE") {
            return ResponseEntity.status(HttpStatus.GONE).build()
        }
        return ResponseEntity.ok(DistribuerJournalpostResponseTo(UUID.randomUUID().toString()))
    }
}

data class DistribuerJournalpostRequestTo(
    val journalpostId: String,
)

data class DistribuerJournalpostResponseTo(
    val bestillingsId: String,
)
