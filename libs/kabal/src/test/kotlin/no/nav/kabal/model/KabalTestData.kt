package no.nav.kabal.model

import java.time.LocalDate
import java.time.LocalDateTime

object KabalTestData {

    fun createValidSendSakV4Request(
        type: SakType = SakType.KLAGE,
        sakenGjelderIdent: String = "12345678901",
        klagerIdent: String = "12345678901",
        fagsakId: String = "123456",
        fagsystem: String = "HJE",
        kildeReferanse: String = "kilde-ref-123",
        forrigeBehandlendeEnhet: String = "4201",
        ytelse: String = "HEL_HEL",
        kommentar: String? = null,
        hjemler: List<String> = emptyList()
    ): SendSakV4Request {
        return SendSakV4Request(
            type = type,
            sakenGjelder = SakenGjelder(Ident(IdentType.PERSON, sakenGjelderIdent)),
            klager = Klager(Ident(IdentType.PERSON, klagerIdent)),
            fagsak = Fagsak(fagsakId, fagsystem),
            kildeReferanse = kildeReferanse,
            forrigeBehandlendeEnhet = forrigeBehandlendeEnhet,
            ytelse = ytelse,
            kommentar = kommentar,
            hjemler = hjemler
        )
    }

    fun createSendSakV4RequestWithProsessfullmektig(
        type: SakType = SakType.KLAGE,
        prosessfullmektigIdent: String = "98765432101",
        prosessfullmektigNavn: String = "Advokat Hansen"
    ): SendSakV4Request {
        return SendSakV4Request(
            type = type,
            sakenGjelder = SakenGjelder(Ident(IdentType.PERSON, "12345678901")),
            klager = Klager(Ident(IdentType.PERSON, "12345678901")),
            prosessfullmektig = Prosessfullmektig(
                id = Ident(IdentType.PERSON, prosessfullmektigIdent),
                navn = prosessfullmektigNavn,
                adresse = Adresse(
                    adresselinje1 = "Storgata 10",
                    postnummer = "0157",
                    poststed = "Oslo",
                    land = "Norge"
                )
            ),
            fagsak = Fagsak("123456", "SUPERHELT"),
            kildeReferanse = "kilde-ref-123",
            forrigeBehandlendeEnhet = "4201",
            ytelse = "HEL_HEL",
        )
    }

    fun createSendSakV4RequestWithJournalposter(
        journalposter: List<TilknyttetJournalpost> = listOf(
            TilknyttetJournalpost(JournalpostType.BRUKERS_KLAGE, "jp-123")
        )
    ): SendSakV4Request {
        return SendSakV4Request(
            type = SakType.KLAGE,
            sakenGjelder = SakenGjelder(Ident(IdentType.PERSON, "12345678901")),
            klager = Klager(Ident(IdentType.PERSON, "12345678901")),
            fagsak = Fagsak("123456", "SUPERHELT"),
            kildeReferanse = "kilde-ref-123",
            forrigeBehandlendeEnhet = "4201",
            ytelse = "HEL_HEL",
            tilknyttedeJournalposter = journalposter
        )
    }

    fun createSendSakV4RequestWithAllFields(
        type: SakType = SakType.KLAGE,
        hjemler: List<String> = listOf(Hjemmel.FVL_11.id, Hjemmel.FVL_12.id),
        ytelse: String = "OMS_OMP"
    ): SendSakV4Request {
        return SendSakV4Request(
            type = type,
            sakenGjelder = SakenGjelder(Ident(IdentType.PERSON, "12345678901")),
            klager = Klager(Ident(IdentType.PERSON, "12345678901")),
            prosessfullmektig = Prosessfullmektig(
                id = Ident(IdentType.PERSON, "98765432101"),
                navn = "Advokat Hansen",
                adresse = Adresse(
                    adresselinje1 = "Storgata 10",
                    adresselinje2 = "Leilighet 5",
                    postnummer = "0157",
                    poststed = "Oslo",
                    land = "Norge"
                )
            ),
            fagsak = Fagsak("123456", "SUPERHELT"),
            kildeReferanse = "kilde-ref-123",
            dvhReferanse = "dvh-ref-456",
            hjemler = hjemler,
            forrigeBehandlendeEnhet = "NAV Oslo",
            tilknyttedeJournalposter = listOf(
                TilknyttetJournalpost(JournalpostType.BRUKERS_KLAGE, "jp-123"),
                TilknyttetJournalpost(JournalpostType.OPPRINNELIG_VEDTAK, "jp-456")
            ),
            brukersKlageMottattVedtaksinstans = LocalDate.of(2026, 3, 1),
            frist = LocalDate.of(2026, 6, 1),
            sakMottattKaTidspunkt = LocalDateTime.of(2026, 3, 5, 10, 0),
            ytelse = ytelse,
            kommentar = "Klager er uenig i vedtaket",
            hindreAutomatiskSvarbrev = true,
            saksbehandlerIdentForTildeling = "Z123456"
        )
    }


    fun createIdent(
        type: IdentType = IdentType.PERSON,
        verdi: String = "12345678901"
    ): Ident {
        return Ident(type = type, verdi = verdi)
    }

    fun createAdresse(
        adresselinje1: String = "Storgata 10",
        adresselinje2: String? = null,
        postnummer: String = "0157",
        poststed: String = "Oslo",
        land: String = "Norge"
    ): Adresse {
        return Adresse(
            adresselinje1 = adresselinje1,
            adresselinje2 = adresselinje2,
            postnummer = postnummer,
            poststed = poststed,
            land = land
        )
    }

    fun createFagsak(
        fagsakId: String = "123456",
        fagsystem: String = "SUPERHELT"
    ): Fagsak {
        return Fagsak(fagsakId = fagsakId, fagsystem = fagsystem)
    }

    fun createTilknyttetJournalpost(
        type: JournalpostType = JournalpostType.BRUKERS_KLAGE,
        journalpostId: String = "jp-123"
    ): TilknyttetJournalpost {
        return TilknyttetJournalpost(type = type, journalpostId = journalpostId)
    }
}
