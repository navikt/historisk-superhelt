package no.nav.historisk.mock.dokarkiv

import net.datafaker.Faker
import no.nav.common.types.EksternJournalpostId
import no.nav.dokarkiv.BrukerIdType
import no.nav.dokarkiv.EksternDokumentInfoId
import no.nav.historisk.mock.classpathAsStream
import no.nav.saf.graphql.JournalStatus
import no.nav.saf.graphql.Journalpost
import no.nav.saf.graphql.JournalpostBruker
import no.nav.saf.graphql.JournalpostDokumentInfo
import no.nav.saf.graphql.JournalpostDokumentVariant
import no.nav.saf.graphql.SafJournalpostType
import java.time.LocalDateTime

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
    val datoOpprettet = LocalDateTime.now().minusDays(faker.number().numberBetween(1, 10).toLong())
    return journalpost.copy(
        journalpostId = journalpostId,
        tittel = tittel,
        dokumenter = dokumenter,
        datoOpprettet = datoOpprettet,
        journalposttype = SafJournalpostType.I,
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
        journalposttype = SafJournalpostType.I,
    )

val pdfdoc = classpathAsStream("/saf/saf_doc.pdf").readBytes()
