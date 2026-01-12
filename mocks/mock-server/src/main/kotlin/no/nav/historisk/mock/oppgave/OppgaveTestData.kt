package no.nav.historisk.mock.oppgave

import net.datafaker.Faker

import no.nav.historisk.mock.pdl.fakeAktoerIdFromFnr
import no.nav.oppgave.OppgaveTypeTemaHel
import no.nav.oppgave.models.BrukerDto
import no.nav.oppgave.models.Oppgave
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

val defaultSaksbehandler = "SARAH"

val faker= Faker()

fun generateOppgave(fnr:String?= null, tilordnetRessurs: String?= null): Oppgave{
    val ident = fnr?: faker.numerify("5##########")
    return Oppgave(
        id = faker.number().positive().toLong(),
        tildeltEnhetsnr = "1234",
        tilordnetRessurs = tilordnetRessurs,
        opprettetAvEnhetsnr = "5678",
        journalpostId = faker.internet().uuid(),
        opprettetAv = faker.numerify("Z######"),
        opprettetTidspunkt = faker.timeAndDate().past().atOffset(ZoneOffset.UTC),
        fristFerdigstillelse = LocalDate.ofInstant(faker.timeAndDate().future(), ZoneOffset.UTC),
        tema = "HEL",
        behandlingstema = "ab0013",
        behandlingstype = "ae0034",
        oppgavetype = OppgaveTypeTemaHel.JFR.oppgavetype,
        status = Oppgave.Status.OPPRETTET,
        prioritet = Oppgave.Prioritet.NORM,
        versjon = 1,
        aktoerId = fakeAktoerIdFromFnr(ident),
        aktivDato = LocalDate.ofInstant(faker.timeAndDate().past(3,TimeUnit.DAYS), ZoneOffset.UTC),
        bruker = BrukerDto(
            ident = ident,
            type = BrukerDto.Type.PERSON,
        )
    )
}