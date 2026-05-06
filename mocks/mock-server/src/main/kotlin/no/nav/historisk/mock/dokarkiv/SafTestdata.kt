package no.nav.historisk.mock.dokarkiv

import net.datafaker.Faker
import no.nav.common.types.EksternJournalpostId
import no.nav.dokarkiv.BrukerIdType
import no.nav.dokarkiv.EksternDokumentInfoId
import no.nav.dokarkiv.JournalpostType
import no.nav.historisk.mock.classpathAsStream
import no.nav.saf.graphql.*
import java.time.LocalDateTime
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
    val datoOpprettet: LocalDateTime = faker.timeAndDate().past(10, TimeUnit.DAYS).let { LocalDateTime.parse(it.toString()) }
    return journalpost.copy(
        journalpostId = journalpostId,
        tittel = tittel,
        dokumenter = dokumenter,
        datoOpprettet = datoOpprettet,
        journalposttype = JournalpostType.I,
    )
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
        datoOpprettet = LocalDateTime.now(),
        journalposttype = JournalpostType.I,
    )

val pdfdoc = classpathAsStream("/saf/saf_doc.pdf").readBytes()
