package no.nav.historisk.superhelt.samhandler

import no.nav.common.types.Organisasjonsnummer
import no.nav.ereg.EregAdresse
import no.nav.ereg.EregClient
import no.nav.ereg.EregOrganisasjon
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SamhandlerServiceTest {

    private val eregClient: EregClient = mock()
    private val service = SamhandlerService(eregClient)

    @Test
    fun `hentSamhandler returnerer null når organisasjon ikke finnes`() {
        val orgnr = Organisasjonsnummer("000000000")
        whenever(eregClient.hentOrganisasjon(orgnr)).thenReturn(null)

        val result = service.hentSamhandler(orgnr)

        assertThat(result).isNull()
    }

    @Test
    fun `hentSamhandler mapper organisasjonsnummer og navn`() {
        val orgnr = Organisasjonsnummer("123456789")
        whenever(eregClient.hentOrganisasjon(orgnr)).thenReturn(
            EregOrganisasjon(
                organisasjonsnummer = orgnr,
                navn = "FIRMA AS",
                postadresse = null,
                forretningsadresse = null,
            )
        )

        val result = service.hentSamhandler(orgnr)

        assertThat(result).isNotNull
        assertThat(result!!.organisasjonsnummer).isEqualTo(orgnr)
        assertThat(result.navn).isEqualTo("FIRMA AS")
        assertThat(result.postadresse).isNull()
        assertThat(result.forretningsadresse).isNull()
    }

    @Test
    fun `hentSamhandler mapper postadresse og forretningsadresse korrekt`() {
        val orgnr = Organisasjonsnummer("987654321")
        whenever(eregClient.hentOrganisasjon(orgnr)).thenReturn(
            EregOrganisasjon(
                organisasjonsnummer = orgnr,
                navn = "APOTEK AS",
                postadresse = EregAdresse(
                    adresselinje1 = "Postboks 1",
                    adresselinje2 = null,
                    adresselinje3 = null,
                    postnummer = "0001",
                    poststed = "OSLO",
                    landkode = "NO",
                    land = "NORGE",
                ),
                forretningsadresse = EregAdresse(
                    adresselinje1 = "Storgata 1",
                    adresselinje2 = "2. etasje",
                    adresselinje3 = null,
                    postnummer = "0010",
                    poststed = "OSLO",
                    landkode = "NO",
                    land = "NORGE",
                ),
            )
        )

        val result = service.hentSamhandler(orgnr)

        assertThat(result).isNotNull
        assertThat(result!!.postadresse).isNotNull
        assertThat(result.postadresse!!.adresselinje1).isEqualTo("Postboks 1")
        assertThat(result.postadresse.postnummer).isEqualTo("0001")
        assertThat(result.postadresse.poststed).isEqualTo("OSLO")
        assertThat(result.postadresse.landkode).isEqualTo("NO")
        assertThat(result.postadresse.land).isEqualTo("NORGE")
        assertThat(result.forretningsadresse).isNotNull
        assertThat(result.forretningsadresse!!.adresselinje1).isEqualTo("Storgata 1")
        assertThat(result.forretningsadresse.adresselinje2).isEqualTo("2. etasje")
    }

    @Test
    fun `hentSamhandler håndterer adresse med kun nullfelter`() {
        val orgnr = Organisasjonsnummer("111222333")
        whenever(eregClient.hentOrganisasjon(orgnr)).thenReturn(
            EregOrganisasjon(
                organisasjonsnummer = orgnr,
                navn = "FORENING",
                postadresse = EregAdresse(
                    adresselinje1 = null,
                    adresselinje2 = null,
                    adresselinje3 = null,
                    postnummer = null,
                    poststed = null,
                    landkode = null,
                    land = null,
                ),
                forretningsadresse = null,
            )
        )

        val result = service.hentSamhandler(orgnr)

        assertThat(result).isNotNull
        assertThat(result!!.postadresse).isNotNull
        assertThat(result.postadresse!!.adresselinje1).isNull()
        assertThat(result.postadresse.postnummer).isNull()
    }
}
