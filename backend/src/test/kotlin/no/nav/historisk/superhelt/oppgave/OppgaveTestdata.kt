package no.nav.historisk.superhelt.oppgave

import net.datafaker.Faker
import no.nav.common.consts.FellesKodeverkTema
import no.nav.common.types.EksternJournalpostId
import no.nav.common.types.EksternOppgaveId
import no.nav.common.types.Enhetsnummer
import no.nav.common.types.NavIdent
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.StonadsType
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.oppgave.OppgaveType
import no.nav.oppgave.model.OppgaveDto
import java.time.LocalDate
import java.time.OffsetDateTime

object OppgaveTestdata {
    private val faker: Faker = Faker()

    fun opprettOppgave(bruker: String = faker.numerify("###########"), tema: FellesKodeverkTema= FellesKodeverkTema.HEL) =
        OppgaveDto(
            id = EksternOppgaveId(faker.number().positive().toLong()),
            tildeltEnhetsnr = Enhetsnummer("1234"),
            oppgavetype = faker.options().option(OppgaveType::class.java).oppgavetype,
            tema = tema.kode,
            status = faker.options().option(OppgaveDto.Status::class.java),
            journalpostId = EksternJournalpostId(faker.number().positive().toString()),
            aktoerId = null,
            tilordnetRessurs = NavIdent(faker.bothify("?#####")),
            fristFerdigstillelse = LocalDate.now().plusDays(2),
            opprettetTidspunkt = OffsetDateTime.now().minusDays(4),
            versjon = 1,
            prioritet = OppgaveDto.Prioritet.NORM,
            aktivDato = LocalDate.now(),
            bruker = OppgaveDto.Bruker(bruker, OppgaveDto.Bruker.BrukerType.PERSON)
        )

    fun oppgaveMedSak() = oppgaveUtenSak().copy(
        saksnummer = Saksnummer(faker.number().positive().toLong()),
        sakStatus = SakStatus.entries.random(),
        stonadsType = StonadsType.entries.random(),
        sakBeskrivelse = faker.ghostbusters().quote()
    )

    fun oppgaveUtenSak()= opprettOppgave().toOppgaveMedSak(null)

}
