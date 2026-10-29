package no.nav.historisk.superhelt.brev

import no.nav.common.types.EksternJournalpostId
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.WithSaksbehandler
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired

@MockedSpringBootTest
@WithSaksbehandler
class BrevRepositoryTest {

    @Autowired
    private lateinit var brevRepository: BrevRepository

    @Autowired
    private lateinit var sakRepository: SakRepository

    @ParameterizedTest
    @CsvSource("UTKAST,SENDT", "ARKIVERT,KLAR_TIL_SENDING")
    fun `skal ignorere hele oppdateringen ved ugyldig statusovergang`(
        gjeldendeStatus: BrevStatus,
        nyStatus: BrevStatus,
    ) {
        val brev = lagreBrev(status = gjeldendeStatus)

        val resultat = brevRepository.oppdaterStatus(
            brev.uuid,
            status = nyStatus,
            journalpostId = EksternJournalpostId("Skal ikke lagres"),
            feil = true,
        )

        assertThat(resultat).isEqualTo(brev)
        assertThat(brevRepository.getByUUid(brev.uuid)).isEqualTo(brev)
    }

    @Test
    fun `skal ignorere hele oppdateringen ved motstridende journalpostreferanse`() {
        val brev = lagreBrev(
            status = BrevStatus.KLAR_TIL_SENDING,
            journalpostId = EksternJournalpostId("Opprinnelig"),
        )

        val resultat = brevRepository.oppdaterStatus(
            brev.uuid,
            status = BrevStatus.ARKIVERT,
            journalpostId = EksternJournalpostId("En annen journalpost"),
            feil = true,
        )

        assertThat(resultat).isEqualTo(brev)
        assertThat(brevRepository.getByUUid(brev.uuid)).isEqualTo(brev)
    }

    @Test
    fun `skal tillate samme status og journalpostreferanse ved gjentatt oppdatering`() {
        val brev = lagreBrev(
            status = BrevStatus.ARKIVERT,
            journalpostId = EksternJournalpostId("Opprinnelig"),
        )

        brevRepository.oppdaterStatus(
            brev.uuid,
            status = brev.status,
            journalpostId = brev.journalpostId,
            feil = true,
        )

        val forventetBrev = brev.copy(feil = true)
        assertThat(brevRepository.getByUUid(brev.uuid)).isEqualTo(forventetBrev)
    }

    @Test
    fun `skal ignorere alle endringer på et sendt brev`() {
        val brev = lagreBrev(
            status = BrevStatus.SENDT,
            journalpostId = EksternJournalpostId("Opprinnelig"),
        )

        val resultat = brevRepository.oppdaterStatus(
            brev.uuid,
            status = BrevStatus.SENDT,
            journalpostId = EksternJournalpostId("En annen journalpost"),
            feil = true,
        )

        assertThat(resultat).isEqualTo(brev)
        assertThat(brevRepository.getByUUid(brev.uuid)).isEqualTo(brev)
    }

    private fun lagreBrev(
        status: BrevStatus = BrevStatus.UTKAST,
        journalpostId: EksternJournalpostId? = null,
    ): Brev {
        val sak = SakTestData.lagreSak(sakRepository, SakTestData.sakUtenUtbetaling())
        val brev = BrevTestdata.lagreBrev(
            brevRepository = brevRepository,
            saksnummer = sak.saksnummer,
            brev = BrevTestdata.fritekstbrevBruker().copy(
                status = status,
                tittel = "Test tittel",
                innhold = "Test innhold",
                journalpostId = journalpostId,
            ),
        )
        return brevRepository.getByUUid(brev.uuid)
    }
}
