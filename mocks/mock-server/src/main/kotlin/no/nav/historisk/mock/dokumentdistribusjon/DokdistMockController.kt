package no.nav.historisk.mock.dokumentdistribusjon

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("dokdist-mock")
class DokdistMockController {

    @PostMapping("/rest/v1/distribuerjournalpost")
    fun distribuerJournalpostMock() : DistribuerJournalpostResponseTo{
        return DistribuerJournalpostResponseTo(UUID.randomUUID().toString())
    }
}

data class DistribuerJournalpostResponseTo(
    val bestillingsId: String,
)
