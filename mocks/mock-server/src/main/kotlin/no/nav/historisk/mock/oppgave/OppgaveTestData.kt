package no.nav.historisk.mock.oppgave

import net.datafaker.Faker
import no.nav.common.types.*
import no.nav.historisk.mock.pdl.fakeAktoerIdFromFnr
import no.nav.oppgave.OppgaveType
import no.nav.oppgave.model.OppgaveDto
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

val defaultSaksbehandler = "SARAH"

val faker= Faker()

fun generateOppgave(fnr:String?= null, tilordnetRessurs: NavIdent?= null): OppgaveDto {
    val ident = fnr?: faker.numerify("5##########")
    return OppgaveDto(
        id = EksternOppgaveId(faker.number().positive().toLong()),
        tildeltEnhetsnr = Enhetsnummer("1234"),
        tilordnetRessurs = tilordnetRessurs,
        opprettetAvEnhetsnr = "5678",
        journalpostId = EksternJournalpostId(faker.internet().uuid()),
        opprettetAv = faker.numerify("Z######"),
        opprettetTidspunkt = faker.timeAndDate().past().atOffset(ZoneOffset.UTC),
        fristFerdigstillelse = LocalDate.ofInstant(faker.timeAndDate().future(), ZoneOffset.UTC),
        tema = "HEL",
        behandlingstema = "ab0013",
        behandlingstype = "ae0034",
        oppgavetype = OppgaveType.JFR.oppgavetype,
        status = OppgaveDto.Status.OPPRETTET,
        prioritet = OppgaveDto.Prioritet.NORM,
        versjon = 1,
        aktoerId = AktorId(fakeAktoerIdFromFnr(ident)),
        aktivDato = LocalDate.ofInstant(faker.timeAndDate().past(3,TimeUnit.DAYS), ZoneOffset.UTC),
        bruker = OppgaveDto.Bruker(
            ident = ident,
            type = OppgaveDto.Bruker.BrukerType.PERSON,
        )
    )
}