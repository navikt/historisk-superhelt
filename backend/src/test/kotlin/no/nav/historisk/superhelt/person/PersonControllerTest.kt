package no.nav.historisk.superhelt.person


import no.nav.common.types.FolkeregisterIdent
import no.nav.historisk.superhelt.person.tilgangsmaskin.TilgangsmaskinService
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.person.Persondata
import no.nav.tilgangsmaskin.Avvisningskode
import no.nav.tilgangsmaskin.TilgangsmaskinClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester


@MockedSpringBootTest
@AutoConfigureMockMvc
@WithMockUser(authorities = ["READ"])
class PersonControllerTest {

    @MockitoBean
    private lateinit var tilgangsmaskinService: TilgangsmaskinService

    @MockitoBean
    private lateinit var personService: PersonService

    @Autowired
    private lateinit var mockMvc: MockMvcTester


    @Test
    fun `skal hente person `() {
        val fnr = FolkeregisterIdent("12345678901")
        val foedselsdato = "2000-01-01"
        val testPerson = PersonTestData.testPerson.copy(fnr = fnr, foedselsdato = foedselsdato)
        mockPerson(fnr, testPerson)

        assertThat(getPersonByMaskertId(fnr))
            .hasStatusOk()
            .bodyJson()
            .convertTo(Person::class.java)
            .satisfies({
                assertThat(it.fnr).isEqualTo(fnr)
                assertThat(it.navn).isEqualTo(testPerson.navn)
                assertThat(it.maskertPersonident).isEqualTo(fnr.toMaskertPersonIdent())
                assertThat(it.avvisningsKode).isNull()
                assertThat(it.foedselsdato).isEqualTo(java.time.LocalDate.parse(foedselsdato))
                assertThat(it.alder).isEqualTo(26)
            })
    }

    @Test
    fun `skal feile når maskertpersonid er ugyldig`() {
        assertThat(mockMvc.get().uri("/api/person/{maskertPersonident}", "123123"))
            .hasStatus(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `skal returnere person med tilgang når fnr er gyldig`() {
        val fnr = FolkeregisterIdent("12345678901")
        val foedselsdato = "2000-01-01"
        val testPerson = PersonTestData.testPerson.copy(fnr = fnr, foedselsdato = foedselsdato)
        mockPerson(fnr, testPerson)

        assertThat(findPersonByFnr(fnr))
            .hasStatusOk()
            .bodyJson()
            .convertTo(Person::class.java)
            .satisfies({
                assertThat(it.fnr).isEqualTo(fnr)
                assertThat(it.navn).isEqualTo(testPerson.navn)
                assertThat(it.maskertPersonident).isEqualTo(fnr.toMaskertPersonIdent())
                assertThat(it.avvisningsKode).isNull()
                assertThat(it.foedselsdato).isEqualTo(java.time.LocalDate.parse(foedselsdato))
                assertThat(it.alder).isEqualTo(26)
            })
    }

    @Test
    fun `skal returnere person med avvisningskode når bruker mangler tilgang`() {
        val fnr = FolkeregisterIdent("12345678901")
        val foedselsdato = "2000-01-01"
        val testPerson = PersonTestData.testPerson.copy(fnr = fnr, navn = "***", harTilgang = false, foedselsdato = foedselsdato)
        mockPerson(fnr, testPerson, Avvisningskode.AVVIST_HABILITET)

        assertThat(findPersonByFnr(fnr))
            .hasStatusOk()
            .bodyJson()
            .convertTo(Person::class.java)
            .satisfies({
                assertThat(it.fnr).isEqualTo(fnr)
                assertThat(it.navn).contains("***")
                assertThat(it.maskertPersonident).isEqualTo(fnr.toMaskertPersonIdent())
                assertThat(it.avvisningsKode).isEqualTo(Avvisningskode.AVVIST_HABILITET)
                assertThat(it.foedselsdato).isEqualTo(java.time.LocalDate.parse(foedselsdato))
                assertThat(it.alder).isEqualTo(26)
            })
    }

    @Test
    fun `skal returnere 404 når person ikke finnes i PDL`() {
        val fnr = FolkeregisterIdent("12345678901")
        mockPerson(fnr, null, Avvisningskode.UKJENT_PERSON)

        assertThat(findPersonByFnr(fnr))
            .hasStatus(HttpStatus.NOT_FOUND)
    }

    @WithMockUser()
    @Test
    fun `Må ha lese rettighet for å gjøre kall til get `() {
        val fnr = FolkeregisterIdent("12345678901")
        assertThat(getPersonByMaskertId(fnr))
            .hasStatus(HttpStatus.FORBIDDEN)
    }

    private fun getPersonByMaskertId(fnr: FolkeregisterIdent): MockMvcTester.MockMvcRequestBuilder =
        mockMvc.get().uri("/api/person/{maskertPersonident}", fnr.toMaskertPersonIdent().value)

    @WithMockUser()
    @Test
    fun `Må ha lese rettighet for å gjøre kall til post`() {
        val fnr = FolkeregisterIdent("12345678901")
        assertThat(findPersonByFnr(fnr))
            .hasStatus(HttpStatus.FORBIDDEN)
    }

    private fun findPersonByFnr(fnr: FolkeregisterIdent): MockMvcTester.MockMvcRequestBuilder = mockMvc.post().uri("/api/person")
        .with(csrf())
        .contentType("application/json")
        .content("""{"fnr": "${fnr.value}"}""")

    @Test
    fun `Validering av fnr`() {
        val fnr = FolkeregisterIdent("1234")
        assertThat(findPersonByFnr(fnr))
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyText()
            .contains("Fødselsnummer må være 11 tegn")
    }


    private fun mockPerson(
        fnr: FolkeregisterIdent,
        testPerson: Persondata? = PersonTestData.testPerson,
        avvisningskode: Avvisningskode? = null
    ) {
        whenever(personService.hentPerson(fnr)) doReturn testPerson?.copy(fnr = fnr)
        if (avvisningskode == null) {
            whenever(tilgangsmaskinService.sjekkKomplettTilgang(fnr)) doReturn TilgangsmaskinClient.TilgangResult(
                harTilgang = true,
                response = null
            )
        } else {
            whenever(tilgangsmaskinService.sjekkKomplettTilgang(fnr)) doReturn TilgangsmaskinClient.TilgangResult(
                harTilgang = false,
                TilgangsmaskinTestData.problemDetailResponse.copy(title = avvisningskode)
            )
        }
    }

}

