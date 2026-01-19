package no.nav.historisk.superhelt.oppgave

import net.datafaker.Faker
import no.nav.common.types.EksternJournalpostId
import no.nav.common.types.EksternOppgaveId
import no.nav.common.types.Enhetsnummer
import no.nav.common.types.NavIdent
import no.nav.oppgave.OppgaveType
import no.nav.oppgave.model.OppgaveDto
import java.time.LocalDate

object OppgaveTestdata {
    private val faker: Faker = Faker()

    fun opprettOppgave(bruker: String = faker.numerify("###########")) =
        OppgaveDto(
            id = EksternOppgaveId(faker.number().positive().toLong()),
            tildeltEnhetsnr = Enhetsnummer("1234"),
            oppgavetype = faker.options().option(OppgaveType::class.java).oppgavetype,
            tema = "HEL",
            status = faker.options().option(OppgaveDto.Status::class.java),
            journalpostId = EksternJournalpostId(faker.number().positive().toString()),
            aktoerId = null,
            tilordnetRessurs = NavIdent(faker.bothify("?#####")),
            versjon = 1,
            prioritet = OppgaveDto.Prioritet.NORM,
            aktivDato = LocalDate.now(),
            bruker = OppgaveDto.Bruker(bruker, OppgaveDto.Bruker.BrukerType.PERSON),

            )
}