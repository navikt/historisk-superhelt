package no.nav.oppgave

import no.nav.common.types.EksternFellesKodeverkTema
import no.nav.common.types.EksternOppgaveId
import no.nav.common.types.NavIdent
import no.nav.oppgave.model.FinnOppgaverParams
import no.nav.oppgave.model.OpprettOppgaveRequest
import no.nav.oppgave.model.PatchOppgaveRequest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestClient
import java.time.LocalDate
import java.util.*

@Disabled
class OppgaveClientForUtvikler {

    /**
     * Genereres i Ida  auth token med client id dev-fss.oppgavehandtering.oppgave
     */

    val accessToken =
        """
        <lim inn ditt token her fra ida >
         """.trimIndent()

//    private val baseUrl = "https://oppgave.intern.dev.nav.no"

    private val baseUrl = "http://localhost:9080/oppgave-mock"
    val client = OppgaveClient(getRestClient())

    @Test // IntelliJ bryr seg ikke om @Disabled derfor må denne kommenteres inn før kjøring
    fun `test hent saksbehandlers oppgaver`() {
        val hentet = client.finnOppgaver(
            FinnOppgaverParams(
                statuskategori = "AAPEN",
                tilordnetRessurs = NavIdent("Z990749"),
                tema = listOf("HEL"),
            )
        )
        println(hentet.oppgaver?.joinToString(separator = "\n\n"))
    }

    @Test
    fun `test hent oppgaver på sak`() {
        val hentet = client.finnOppgaver(
            FinnOppgaverParams(
                statuskategori = "AAPEN",
                saksreferanse = listOf("Test-123"),
                tema = listOf("HEL"),
            )
        )
        println(hentet.oppgaver?.joinToString(separator = "\n\n"))
    }

    @Test // Bruk en id som finnes
    fun `opprett oppgave`() {
        val opprettet =
            client.opprettOppgave(
                OpprettOppgaveRequest(
                    personident = "28497016101",
                    tema = EksternFellesKodeverkTema.HEL.name,
                    oppgavetype = OppgaveTypeTemaHel.BEH_SAK.oppgavetype,
                    behandlingstema = OppgaveGjelderTemaHel.REISEUTGIFTER.behandlingstema,
                    behandlingstype = OppgaveGjelderTemaHel.REISEUTGIFTER.behandlingstype,
                    beskrivelse = "Test oppgave fra utvikler",
                    uuid = UUID.randomUUID(),
                )
            )
        println(opprettet)
    }


    @Test // Bruk en id som finnes
    fun `hent og oppdater`() {

        val oppgaveId = EksternOppgaveId(368496)
        val hentet = client.hentOppgave(oppgaveId)
        println(hentet)

        client.patchOppgave(
            hentet.id, PatchOppgaveRequest(
                versjon = hentet.versjon,
                tilordnetRessurs = NavIdent("Z990749"),
                behandlesAvApplikasjon = "HELT",
                saksreferanse = "Test-123",
                fristFerdigstillelse = LocalDate.now().plusDays(5),
            )
        )

        val hentet2 = client.hentOppgave(oppgaveId)
        println(hentet2)

    }

    private fun getRestClient(): RestClient {
        return RestClient.builder()
            .baseUrl(baseUrl)
            .requestInterceptor(bearerTokenInterceptor())
            .defaultHeaders { headers ->
                headers.set("X-Correlation-ID", UUID.randomUUID().toString())
            }
            .build()
    }

    private fun bearerTokenInterceptor(): ClientHttpRequestInterceptor {
        return ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray?, execution: ClientHttpRequestExecution ->
            request.headers.setBearerAuth(accessToken)
            execution.execute(request, body!!)
        }
    }
}