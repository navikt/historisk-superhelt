package no.nav.kabal.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class SendSakV4RequestTest {

    @Test
    fun `SendSakV4Request should have correct default values`() {
        // Forbered og utfør
        val request = SendSakV4Request(
            type = SakType.KLAGE,
            sakenGjelder = SakenGjelder(Ident(IdentType.PERSON, "12345678901")),
            klager = Klager(Ident(IdentType.PERSON, "12345678901")),
            fagsak = Fagsak("123456", "SUPERHELT"),
            kildeReferanse = "kilde-ref-123",
            forrigeBehandlendeEnhet = "4201",
            ytelse = "HEL_HEL",
        )

        // Verifiser
        assertThat(request.type).isEqualTo(SakType.KLAGE)
        assertThat(request.prosessfullmektig).isNull()
        assertThat(request.hjemler).isEmpty()
        assertThat(request.tilknyttedeJournalposter).isEmpty()
        assertThat(request.hindreAutomatiskSvarbrev).isFalse()
        assertThat(request.kommentar).isNull()
    }


    @Test
    fun `SendSakV4Request should store all optional fields`() {
        // Forbered
        val prosessfullmektig = Prosessfullmektig(
            id = Ident(IdentType.PERSON, "98765432101"),
            navn = "Advokat Hansen",
            adresse = Adresse(adresselinje1 = "Storgata 10", postnummer = "0157")
        )
        val journalposter = listOf(
            TilknyttetJournalpost(JournalpostType.BRUKERS_KLAGE, "jp-123")
        )
        val hjemler = listOf(Hjemmel.FVL_11.id, Hjemmel.FVL_12.id)
        val frist = LocalDate.of(2026, 6, 1)

        // Utfør
        val request = SendSakV4Request(
            type = SakType.KLAGE,
            sakenGjelder = SakenGjelder(Ident(IdentType.PERSON, "12345678901")),
            klager = Klager(Ident(IdentType.PERSON, "12345678901")),
            prosessfullmektig = prosessfullmektig,
            fagsak = Fagsak("123456", "SUPERHELT"),
            kildeReferanse = "kilde-ref-123",
            forrigeBehandlendeEnhet = "4201",
            ytelse = "HEL_HEL",
            hjemler = hjemler,
            tilknyttedeJournalposter = journalposter,
            frist = frist,
            kommentar = "Klager er uenig",
            hindreAutomatiskSvarbrev = true
        )

        // Verifiser
        assertThat(request.prosessfullmektig).isEqualTo(prosessfullmektig)
        assertThat(request.hjemler).isEqualTo(hjemler)
        assertThat(request.tilknyttedeJournalposter).isEqualTo(journalposter)
        assertThat(request.frist).isEqualTo(frist)
        assertThat(request.kommentar).isEqualTo("Klager er uenig")
        assertThat(request.hindreAutomatiskSvarbrev).isTrue()
    }

    @Test
    fun `Ident should support PERSON type`() {
        // Forbered og utfør
        val ident = Ident(IdentType.PERSON, "12345678901")

        // Verifiser
        assertThat(ident.type).isEqualTo(IdentType.PERSON)
        assertThat(ident.verdi).isEqualTo("12345678901")
    }

    @Test
    fun `Ident should support VIRKSOMHET type`() {
        // Forbered og utfør
        val ident = Ident(IdentType.VIRKSOMHET, "987654321")

        // Verifiser
        assertThat(ident.type).isEqualTo(IdentType.VIRKSOMHET)
        assertThat(ident.verdi).isEqualTo("987654321")
    }

    @Test
    fun `Prosessfullmektig should have optional adresse`() {
        // Forbered og utfør
        val prosessfullmektig = Prosessfullmektig(
            id = Ident(IdentType.PERSON, "98765432101"),
            navn = "Advokat Hansen"
        )

        // Verifiser
        assertThat(prosessfullmektig.adresse).isNull()
    }

    @Test
    fun `Adresse should have all fields optional except adresselinje1`() {
        // Forbered og utfør
        val adresse = Adresse(adresselinje1 = "Storgata 10")

        // Verifiser
        assertThat(adresse.adresselinje1).isEqualTo("Storgata 10")
        assertThat(adresse.adresselinje2).isNull()
        assertThat(adresse.adresselinje3).isNull()
        assertThat(adresse.postnummer).isNull()
        assertThat(adresse.poststed).isNull()
        assertThat(adresse.land).isNull()
    }

    @Test
    fun `JournalpostType should have all valid types`() {
        // Verifiser
        assertThat(JournalpostType.values()).contains(
            JournalpostType.BRUKERS_SOEKNAD,
            JournalpostType.OPPRINNELIG_VEDTAK,
            JournalpostType.BRUKERS_KLAGE,
            JournalpostType.BRUKERS_ANKE,
            JournalpostType.BRUKERS_OMGJOERINGSKRAV,
            JournalpostType.BRUKERS_BEGJAERING_OM_GJENOPPTAK,
            JournalpostType.OVERSENDELSESBREV,
            JournalpostType.KLAGE_VEDTAK,
            JournalpostType.ANNET
        )
    }

    @Test
    fun `TilknyttetJournalpost should store type and journalpostId`() {
        // Forbered og utfør
        val journalpost = TilknyttetJournalpost(
            type = JournalpostType.BRUKERS_KLAGE,
            journalpostId = "jp-123"
        )

        // Verifiser
        assertThat(journalpost.type).isEqualTo(JournalpostType.BRUKERS_KLAGE)
        assertThat(journalpost.journalpostId).isEqualTo("jp-123")
    }

    @Test
    fun `Fagsak should store fagsakId and fagsystem`() {
        // Forbered og utfør
        val fagsak = Fagsak(fagsakId = "123456", fagsystem = "SUPERHELT")

        // Verifiser
        assertThat(fagsak.fagsakId).isEqualTo("123456")
        assertThat(fagsak.fagsystem).isEqualTo("SUPERHELT")
    }

    @Test
    fun `SendSakV4Request should support multiple hjemler`() {
        // Forbered
        val hjemler = listOf(Hjemmel.FVL_11.id, Hjemmel.FVL_12.id, Hjemmel.FVL_14.id, Hjemmel.FTRL_10_3.id)

        // Utfør
        val request = SendSakV4Request(
            type = SakType.KLAGE,
            sakenGjelder = SakenGjelder(Ident(IdentType.PERSON, "12345678901")),
            klager = Klager(Ident(IdentType.PERSON, "12345678901")),
            fagsak = Fagsak("123456", "SUPERHELT"),
            kildeReferanse = "kilde-ref-123",
            forrigeBehandlendeEnhet = "4201",
            ytelse = "HEL_HEL",
            hjemler = hjemler
        )

        // Verifiser
        assertThat(request.hjemler).hasSize(4)
        assertThat(request.hjemler).containsAll(hjemler)
    }

    @Test
    fun `SendSakV4Request should support multiple journalposter`() {
        // Forbered
        val journalposter = listOf(
            TilknyttetJournalpost(JournalpostType.BRUKERS_KLAGE, "jp-111"),
            TilknyttetJournalpost(JournalpostType.OPPRINNELIG_VEDTAK, "jp-222"),
            TilknyttetJournalpost(JournalpostType.OVERSENDELSESBREV, "jp-333")
        )

        // Utfør
        val request = SendSakV4Request(
            type = SakType.KLAGE,
            sakenGjelder = SakenGjelder(Ident(IdentType.PERSON, "12345678901")),
            klager = Klager(Ident(IdentType.PERSON, "12345678901")),
            fagsak = Fagsak("123456", "SUPERHELT"),
            kildeReferanse = "kilde-ref-123",
            forrigeBehandlendeEnhet = "4201",
            ytelse = "HEL_HEL",
            tilknyttedeJournalposter = journalposter
        )

        // Verifiser
        assertThat(request.tilknyttedeJournalposter).hasSize(3)
        assertThat(request.tilknyttedeJournalposter).containsAll(journalposter)
    }

    @Test
    fun `SendSakV4Request should support date and datetime fields`() {
        // Forbered
        val mottattDato = LocalDate.of(2026, 3, 1)
        val frist = LocalDate.of(2026, 6, 1)
        val sakMottattTidspunkt = LocalDateTime.of(2026, 3, 5, 10, 30)

        // Utfør
        val request = SendSakV4Request(
            type = SakType.KLAGE,
            sakenGjelder = SakenGjelder(Ident(IdentType.PERSON, "12345678901")),
            klager = Klager(Ident(IdentType.PERSON, "12345678901")),
            fagsak = Fagsak("123456", "SUPERHELT"),
            kildeReferanse = "kilde-ref-123",
            forrigeBehandlendeEnhet = "4201",
            ytelse = "HEL_HEL",
            brukersKlageMottattVedtaksinstans = mottattDato,
            frist = frist,
            sakMottattKaTidspunkt = sakMottattTidspunkt
        )

        // Verifiser
        assertThat(request.brukersKlageMottattVedtaksinstans).isEqualTo(mottattDato)
        assertThat(request.frist).isEqualTo(frist)
        assertThat(request.sakMottattKaTidspunkt).isEqualTo(sakMottattTidspunkt)
    }
}
