package no.nav.dokarkiv

import no.nav.common.types.Saksnummer
import java.io.File

fun getMockJournalpostRequest() =
    JournalpostRequest(
        tittel = "Dummy Journalpost Title2",
        journalpostType = JournalpostType.UTGAAENDE,
        tema = EksternFellesKodeverkTema.HEL,
        avsenderMottaker =
            AvsenderMottaker(
                id = "12345678901",
                idType = AvsenderMottakerIdType.FNR,
                navn = "Dummy Sender/Receiver",
            ),
        eksternReferanseId = "eksternRefHistorisk8",
        dokumenter =
            listOf(
                Dokument(
                    tittel = "Dummy Document Title",
                    brevkode = "DUMMY_CODE",
                    dokumentvarianter =
                        listOf(
                            DokumentVariant(
                                filtype = Filtype.PDF,
                                fysiskDokument = fileToByteArray("src/test/resources/brev.pdf"),
                                variantformat = Variantformat.ARKIV,
                            ),
                        ),
                ),
            ),
        bruker =
            Bruker(
                id = "20896997794",
                idType = BrukerIdType.FNR,
            ),
        kanal = Kanal.NAV_NO,
        sak =
            Sak(
                fagsakId = Saksnummer(123),
                fagsaksystem = "SUPERHELT",
            ),
        journalfoerendeEnhet = Enhetsnummer("9999"),
    )

private fun fileToByteArray(filePath: String): ByteArray {
    val file = File(filePath)
    return file.readBytes()
}
