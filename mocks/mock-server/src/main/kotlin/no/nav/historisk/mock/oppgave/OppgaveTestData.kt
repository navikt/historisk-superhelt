package no.nav.historisk.mock.oppgave

import net.datafaker.Faker
import no.nav.common.types.AktorId
import no.nav.common.types.EksternJournalpostId
import no.nav.common.types.EksternOppgaveId
import no.nav.common.types.Enhetsnummer
import no.nav.common.types.NavIdent
import no.nav.historisk.mock.pdl.fakeAktoerIdFromFnr
import no.nav.oppgave.Behandlingstema
import no.nav.oppgave.Behandlingstype
import no.nav.oppgave.OppgaveKodeverkValidator
import no.nav.oppgave.OppgaveType
import no.nav.oppgave.model.OppgaveDto
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

val defaultSaksbehandler = "SARAH"

val faker= Faker()

private val standardBehandlingstema = listOf(
    Behandlingstema.ORTOPEDISKE_HJELPEMIDLER,
    Behandlingstema.PARYKK_HODEPLAGG,
    Behandlingstema.REISEUTGIFTER,
    Behandlingstema.BRYSTPROTESE_PROTESEBH,
    Behandlingstema.ANSIKTSDEFEKTSPROTESE,
    Behandlingstema.OYEPROTESE,
    Behandlingstema.REISEPENGER_UTPROVING_ORT_TEKNISKE_HJELPEMIDLER,
    Behandlingstema.FORNYELSESSOKNAD_ORTOPEDISKE_HJELPEMIDLER,
)

private fun randomBehandlingstema(): Behandlingstema =
    if (faker.number().randomDouble(2, 0, 1) < 0.80) {
        standardBehandlingstema.random()
    } else {
        faker.options().option(Behandlingstema::class.java)
    }

private fun randomBehandlingstype(behandlingstema: Behandlingstema): Behandlingstype? =
    if (faker.number().randomDouble(2, 0, 1) < 0.50) {
        null
    } else {
        return OppgaveKodeverkValidator.gyldigeTyperFor(behandlingstema)
            .randomOrNull()
    }

fun generateOppgave(fnr:String?= null, tilordnetRessurs: NavIdent?= null): OppgaveDto {
    val ident = fnr?: faker.numerify("5##########")
    val behandlingstema= randomBehandlingstema()
    val behandlingstype=randomBehandlingstype(behandlingstema)
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
        behandlingstema = behandlingstema.kode,
        behandlingstype = behandlingstype?.kode,
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
