package no.nav.historisk.superhelt.oppgave

import net.datafaker.Faker
import no.nav.common.types.*
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.oppgave.OppgaveGjelder
import no.nav.oppgave.OppgaveType
import no.nav.oppgave.model.OppgaveDto
import java.time.LocalDate
import java.time.OffsetDateTime

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

    fun oppgaveMedSak() = oppgaveUtenSak().copy(
        saksnummer = Saksnummer(faker.number().positive().toLong()),
        sakStatus = SakStatus.entries.random()
    )


    fun oppgaveUtenSak() = OppgaveMedSak(
        oppgaveId = EksternOppgaveId(faker.number().positive().toLong()),
        tildeltEnhetsnr = Enhetsnummer("1234"),
        oppgavetype = faker.options().option(OppgaveType::class.java),
        oppgavestatus = faker.options().option(OppgaveDto.Status::class.java),
        journalpostId = EksternJournalpostId(faker.number().positive().toString()),
        tilordnetRessurs = NavIdent(faker.bothify("?#####")),
        fnr = FolkeregisterIdent(faker.numerify("###########")),
        oppgaveGjelder = OppgaveGjelder.entries.random(),
        beskrivelse = faker.lorem().sentence(),
        fristFerdigstillelse = LocalDate.now().plusDays(1),
        opprettetTidspunkt = OffsetDateTime.now(),
        behandlesAvApplikasjon = null,
        opprettetAv = faker.bothify("?#####"),
        saksnummer = null,
        sakStatus = null
    )
}