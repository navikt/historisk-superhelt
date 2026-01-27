package no.nav.historisk.superhelt.dokarkiv

import net.datafaker.Faker
import no.nav.common.types.EksternJournalpostId
import no.nav.dokarkiv.AvsenderMottakerIdType
import no.nav.dokarkiv.BrukerIdType
import no.nav.dokarkiv.EksternDokumentInfoId
import no.nav.saf.graphql.*

object DokarkivTestdata {
    private val faker: Faker = Faker()

    fun journalPost(journalpostId: EksternJournalpostId = EksternJournalpostId(faker.numerify("########"))) =
        Journalpost(
            journalpostId = journalpostId,
            journalstatus = JournalStatus.entries.random(),
            tittel = faker.lorem().sentence(),
            sak = JournalpostSak(
                fagsaksystem = faker.options().option("AO01", "FS22", "K9", "IT01"),
                fagsakId = faker.number().digits(8)
            ),
            bruker = JournalpostBruker(
                id = faker.number().digits(11),
                type = BrukerIdType.entries.random()
            ),
            avsenderMottaker = JournalpostAvsenderMottaker(
                id = faker.number().digits(11),
                type = AvsenderMottakerIdType.entries.random(),
                navn = faker.name().fullName()
            ),
            dokumenter = List(faker.number().numberBetween(1, 4)) { journalpostDokumentInfo() }
        )

    private fun journalpostDokumentInfo() = JournalpostDokumentInfo(
        tittel = faker.lorem().sentence(),
        dokumentInfoId = EksternDokumentInfoId(faker.number().digits(9)),
        dokumentvarianter = List(faker.number().numberBetween(1, 3)) { journalpostDokumentVariant() }
    )

    private fun journalpostDokumentVariant() = JournalpostDokumentVariant(
        filtype = faker.options().option("PDF", "PDFA", "XML", "JSON"),
        filnavn = "${faker.file().fileName()}.${faker.options().option("pdf", "xml")}",
        saksbehandlerHarTilgang = faker.bool().bool()
    )
}