package no.nav.tilgangsmaskin


import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate
import tools.jackson.databind.json.JsonMapper

class TilgangsmaskinClientTest {

    private val objectMapper = JsonMapper.builder()
        .findAndAddModules()
        .build()


    private val restTemplate: RestTemplate = RestTemplate()
    private var mockServer: MockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()

    private val restClient = RestClient.builder(restTemplate).build()
    private val tilgangsmaskinClient = TilgangsmaskinClient(restClient)

    @Test
    fun `komplett returnerer tilgang når bruker har tilgang (204 OK)`() {
        // Arrange
        val personident = "12345678901"

        mockServer.expect(requestTo("/api/v1/komplett"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string(personident))
            .andRespond(withStatus(HttpStatus.NO_CONTENT))

        // Act
        val result = tilgangsmaskinClient.komplett(personident)

        // Assert
        assertThat(result.harTilgang).isTrue()
        assertThat(result.response).isNull()
        mockServer.verify()
    }

    @Test
    fun `komplett returnerer ingen tilgang ved strengt fortrolig adresse (403 Forbidden)`() {
        // Arrange
        val personident = "12345678901"
        val jsonResponse = """
            {
                "type": "https://nav.no/tilgangskontroll/strengt-fortrolig-adresse",
                "title": "AVVIST_STRENGT_FORTROLIG_ADRESSE",
                "status": 403,
                "instance": "/api/v1/komplett",
                "brukerIdent": "$personident",
                "navIdent": "Z999999",
                "begrunnelse": "Brukeren har strengt fortrolig adresse (kode 6)",
                "traceId": "trace-123",
                "kanOverstyres": false
            }
        """.trimIndent()

        mockServer.expect(requestTo("/api/v1/komplett"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withStatus(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonResponse)
            )

        // Act
        val result = tilgangsmaskinClient.komplett(personident)

        // Assert
        assertThat(result.harTilgang).isFalse()
        assertThat(result.response).isNotNull
        assertThat(result.response?.title).isEqualTo(Avvisningskode.AVVIST_STRENGT_FORTROLIG_ADRESSE)
        assertThat(result.response?.status).isEqualTo(403)
        assertThat(result.response?.kanOverstyres).isFalse()
        mockServer.verify()
    }

    @Test
    fun `komplett returnerer ingen tilgang ved strengt fortrolig utland (403 Forbidden)`() {
        // Arrange
        val personident = "12345678901"
        val problemResponse = ProblemDetaljResponse(
            type = "https://nav.no/tilgangskontroll/strengt-fortrolig-utland",
            title = Avvisningskode.AVVIST_STRENGT_FORTROLIG_UTLAND,
            status = 403,
            instance = "/api/v1/komplett",
            brukerIdent = personident,
            navIdent = "Z999999",
            begrunnelse = "Brukeren har strengt fortrolig adresse i utlandet",
            traceId = "trace-123",
            kanOverstyres = false
        )

        mockServer.expect(requestTo("/api/v1/komplett"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withStatus(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(problemResponse))
            )

        // Act
        val result = tilgangsmaskinClient.komplett(personident)

        // Assert
        assertThat(result.harTilgang).isFalse()
        assertThat(result.response).isNotNull
        assertThat(result.response?.title).isEqualTo(Avvisningskode.AVVIST_STRENGT_FORTROLIG_UTLAND)
        assertThat(result.response?.status).isEqualTo(403)
        mockServer.verify()
    }

    @Test
    fun `komplett returnerer ingen tilgang ved fortrolig adresse (403 Forbidden)`() {
        // Arrange
        val personident = "12345678901"
        val problemResponse = ProblemDetaljResponse(
            type = "https://nav.no/tilgangskontroll/fortrolig-adresse",
            title = Avvisningskode.AVVIST_FORTROLIG_ADRESSE,
            status = 403,
            instance = "/api/v1/komplett",
            brukerIdent = personident,
            navIdent = "Z999999",
            begrunnelse = "Brukeren har fortrolig adresse (kode 7)",
            traceId = "trace-123",
            kanOverstyres = false
        )

        mockServer.expect(requestTo("/api/v1/komplett"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withStatus(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(problemResponse))
            )

        // Act
        val result = tilgangsmaskinClient.komplett(personident)

        // Assert
        assertThat(result.harTilgang).isFalse()
        assertThat(result.response).isNotNull
        assertThat(result.response?.title).isEqualTo(Avvisningskode.AVVIST_FORTROLIG_ADRESSE)
        assertThat(result.response?.status).isEqualTo(403)
        mockServer.verify()
    }

    @Test
    fun `komplett returnerer ingen tilgang ved skjerming (403 Forbidden)`() {
        // Arrange
        val personident = "12345678901"
        val problemResponse = ProblemDetaljResponse(
            type = "https://nav.no/tilgangskontroll/skjerming",
            title = Avvisningskode.AVVIST_SKJERMING,
            status = 403,
            instance = "/api/v1/komplett",
            brukerIdent = personident,
            navIdent = "Z999999",
            begrunnelse = "Brukeren er skjermet (egen ansatt)",
            traceId = "trace-123",
            kanOverstyres = true
        )

        mockServer.expect(requestTo("/api/v1/komplett"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withStatus(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(problemResponse))
            )

        // Act
        val result = tilgangsmaskinClient.komplett(personident)

        // Assert
        assertThat(result.harTilgang).isFalse()
        assertThat(result.response).isNotNull
        assertThat(result.response?.title).isEqualTo(Avvisningskode.AVVIST_SKJERMING)
        assertThat(result.response?.kanOverstyres).isTrue()
        mockServer.verify()
    }

    @Test
    fun `komplett returnerer ingen tilgang ved geografisk tilgangskontroll (403 Forbidden)`() {
        // Arrange
        val personident = "12345678901"
        val problemResponse = ProblemDetaljResponse(
            type = "https://nav.no/tilgangskontroll/geografisk",
            title = Avvisningskode.AVVIST_GEOGRAFISK,
            status = 403,
            instance = "/api/v1/komplett",
            brukerIdent = personident,
            navIdent = "Z999999",
            begrunnelse = "Saksbehandler har ikke geografisk tilgang",
            traceId = "trace-123",
            kanOverstyres = false
        )

        mockServer.expect(requestTo("/api/v1/komplett"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withStatus(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(problemResponse))
            )

        // Act
        val result = tilgangsmaskinClient.komplett(personident)

        // Assert
        assertThat(result.harTilgang).isFalse()
        assertThat(result.response).isNotNull
        assertThat(result.response?.title).isEqualTo(Avvisningskode.AVVIST_GEOGRAFISK)
        mockServer.verify()
    }

    @Test
    fun `komplett returnerer ingen tilgang ved habilitet (403 Forbidden)`() {
        // Arrange
        val personident = "12345678901"
        val problemResponse = ProblemDetaljResponse(
            type = "https://nav.no/tilgangskontroll/habilitet",
            title = Avvisningskode.AVVIST_HABILITET,
            status = 403,
            instance = "/api/v1/komplett",
            brukerIdent = personident,
            navIdent = "Z999999",
            begrunnelse = "Saksbehandler er inhabil",
            traceId = "trace-123",
            kanOverstyres = false
        )

        mockServer.expect(requestTo("/api/v1/komplett"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withStatus(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(problemResponse))
            )

        // Act
        val result = tilgangsmaskinClient.komplett(personident)

        // Assert
        assertThat(result.harTilgang).isFalse()
        assertThat(result.response).isNotNull
        assertThat(result.response?.title).isEqualTo(Avvisningskode.AVVIST_HABILITET)
        mockServer.verify()
    }

    @Test
    fun `komplett returnerer ingen tilgang ved person i utlandet (403 Forbidden)`() {
        // Arrange
        val personident = "12345678901"
        val problemResponse = ProblemDetaljResponse(
            type = "https://nav.no/tilgangskontroll/person-utland",
            title = Avvisningskode.AVVIST_PERSON_UTLAND,
            status = 403,
            instance = "/api/v1/komplett",
            brukerIdent = personident,
            navIdent = "Z999999",
            begrunnelse = "Person er bosatt i utlandet",
            traceId = "trace-123",
            kanOverstyres = false
        )

        mockServer.expect(requestTo("/api/v1/komplett"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withStatus(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(problemResponse))
            )

        // Act
        val result = tilgangsmaskinClient.komplett(personident)

        // Assert
        assertThat(result.harTilgang).isFalse()
        assertThat(result.response).isNotNull
        assertThat(result.response?.title).isEqualTo(Avvisningskode.AVVIST_PERSON_UTLAND)
        mockServer.verify()
    }

    @Test
    fun `komplett returnerer ingen tilgang ved ukjent bosted (403 Forbidden)`() {
        // Arrange
        val personident = "12345678901"
        val problemResponse = ProblemDetaljResponse(
            type = "https://nav.no/tilgangskontroll/ukjent-bosted",
            title = Avvisningskode.AVVIST_UKJENT_BOSTED,
            status = 403,
            instance = "/api/v1/komplett",
            brukerIdent = personident,
            navIdent = "Z999999",
            begrunnelse = "Person har ukjent bosted",
            traceId = "trace-123",
            kanOverstyres = false
        )

        mockServer.expect(requestTo("/api/v1/komplett"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withStatus(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(problemResponse))
            )

        // Act
        val result = tilgangsmaskinClient.komplett(personident)

        // Assert
        assertThat(result.harTilgang).isFalse()
        assertThat(result.response).isNotNull
        assertThat(result.response?.title).isEqualTo(Avvisningskode.AVVIST_UKJENT_BOSTED)
        mockServer.verify()
    }

    @Test
    fun `komplett returnerer ingen tilgang ved avdød person (403 Forbidden)`() {
        // Arrange
        val personident = "12345678901"
        val problemResponse = ProblemDetaljResponse(
            type = "https://nav.no/tilgangskontroll/avdød",
            title = Avvisningskode.AVVIST_AVDØD,
            status = 403,
            instance = "/api/v1/komplett",
            brukerIdent = personident,
            navIdent = "Z999999",
            begrunnelse = "Person er avdød",
            traceId = "trace-123",
            kanOverstyres = false
        )

        mockServer.expect(requestTo("/api/v1/komplett"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withStatus(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(problemResponse))
            )

        // Act
        val result = tilgangsmaskinClient.komplett(personident)

        // Assert
        assertThat(result.harTilgang).isFalse()
        assertThat(result.response).isNotNull
        assertThat(result.response?.title).isEqualTo(Avvisningskode.AVVIST_AVDØD)
        mockServer.verify()
    }

    @Test
    fun `komplett returnerer ingen tilgang ved person ikke funnet (404 Not Found)`() {
        // Arrange
        val personident = "99999999999"

        mockServer.expect(requestTo("/api/v1/komplett"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.NOT_FOUND))

        // Act
        val result = tilgangsmaskinClient.komplett(personident)

        // Assert
        assertThat(result.harTilgang).isFalse()
        assertThat(result.response).isNotNull
        assertThat(result.response?.title).isEqualTo(Avvisningskode.UKJENT_PERSON)
        assertThat(result.response?.status).isEqualTo(404)
        assertThat(result.response?.begrunnelse).isEqualTo("Personen finnes ikke")
        mockServer.verify()
    }

    @Test
    fun `komplett kaster exception ved uventet statuskode (500 Internal Server Error)`() {
        // Arrange
        val personident = "12345678901"

        mockServer.expect(requestTo("/api/v1/komplett"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR))

        // Act & Assert
        assertThatThrownBy {
            tilgangsmaskinClient.komplett(personident)
        }.isInstanceOf(RuntimeException::class.java)
        mockServer.verify()
    }

    @Test
    fun `komplett kaster exception ved bad request (400 Bad Request)`() {
        // Arrange
        val personident = "invalid"

        mockServer.expect(requestTo("/api/v1/komplett"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST))

        // Act & Assert
        assertThatThrownBy {
            tilgangsmaskinClient.komplett(personident)
        }.isInstanceOf(RuntimeException::class.java)
        mockServer.verify()
    }
}
