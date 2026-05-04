package no.nav.historisk.mock.dokarkiv

import net.datafaker.Faker
import no.nav.common.types.EksternJournalpostId
import no.nav.dokarkiv.BrukerIdType
import no.nav.dokarkiv.EksternDokumentInfoId
import no.nav.historisk.mock.classpathAsStream
import no.nav.saf.graphql.*
import java.util.concurrent.TimeUnit

val faker = Faker()

fun generateJournalpost(
    journalpostId: EksternJournalpostId,
    antallEkstraVedlegg: Int = 0,
): Journalpost {
    val tittel = "NAV 10-07.10 Søknad om ${faker.food().dish()}"
    val dokumenter =
        mutableListOf(
            dokumentInfo.copy(
                dokumentInfoId = EksternDokumentInfoId(faker.numerify("#########")),
                tittel = tittel,
            ),
        )
    repeat(antallEkstraVedlegg) {
        dokumenter.add(
            dokumentInfo.copy(
                dokumentInfoId = EksternDokumentInfoId(faker.numerify("#########")),
                tittel = "Vedlegg: ${faker.book().title()}",
            ),
        )
    }
    val datoSortering = faker.timeAndDate().past(10, TimeUnit.DAYS).toString()
    return journalpost.copy(journalpostId = journalpostId, tittel = tittel, dokumenter = dokumenter, datoSortering = datoSortering)
}

val dokumentInfo =
    JournalpostDokumentInfo(
        tittel = "NAV 10-07.10 Mockopediske hjelpemidler",
        dokumentInfoId = EksternDokumentInfoId("454256793"),
        dokumentvarianter =
            listOf(
                JournalpostDokumentVariant(
                    filtype = "PDF",
                    filnavn = "test.pdf",
                    saksbehandlerHarTilgang = true,
                ),
            ),
    )
private val journalpost =
    Journalpost(
        journalpostId = EksternJournalpostId("453863071"),
        tittel = "NAV 10-07.10 Ortopediske hjelpemidler",
        journalstatus = JournalStatus.MOTTATT,
        bruker =
            JournalpostBruker(
                id = "11111111111",
                type = BrukerIdType.FNR,
            ),
        dokumenter = emptyList(),
        datoSortering = "1970-01-01T00:00:00Z",
    )

val pdfdoc = classpathAsStream("/saf/saf_doc.pdf").readBytes()
