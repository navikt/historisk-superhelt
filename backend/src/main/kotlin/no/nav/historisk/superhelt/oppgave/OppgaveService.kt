package no.nav.historisk.superhelt.oppgave

import no.nav.common.types.NavIdent
import no.nav.oppgave.OppgaveClient
import no.nav.oppgave.model.FinnOppgaverParams
import no.nav.oppgave.model.Oppgave
import org.springframework.stereotype.Service

@Service
class OppgaveService(private val oppgaveClient: OppgaveClient) {

    fun hentOppgaverForSaksbehandler(navident: NavIdent): List<Oppgave> {
        return oppgaveClient.finnOppgaver(
            FinnOppgaverParams(
                tilordnetRessurs = navident,
                statuskategori = "AAPEN",
                tema = listOf("HEL")
            )
        ).oppgaver ?: emptyList()

    }
}