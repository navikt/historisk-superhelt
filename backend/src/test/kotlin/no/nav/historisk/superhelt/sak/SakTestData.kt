package no.nav.historisk.superhelt.sak

import net.datafaker.Faker
import no.nav.common.types.Aar
import no.nav.common.types.Behandlingsnummer
import no.nav.common.types.Belop
import no.nav.common.types.FolkeregisterIdent
import no.nav.common.types.NavIdent
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.StonadsType
import no.nav.historisk.superhelt.infrastruktur.authentication.NavUser
import no.nav.historisk.superhelt.test.withMockedUser
import no.nav.historisk.superhelt.utbetaling.UtbetalingTestData
import no.nav.historisk.superhelt.utbetaling.UtbetalingsType
import no.nav.historisk.superhelt.vedtak.VedtaksResultat
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object SakTestData {

    private val faker: Faker = Faker()
    private val stonadstyperMedUtbetaling = StonadsType.entries.filter { it.kanUtbetales }

    fun sakMedStatus(sakStatus: SakStatus) = when (sakStatus) {
        SakStatus.UNDER_BEHANDLING -> sakMedUtbetaling().copy(status = sakStatus)
        SakStatus.TIL_ATTESTERING -> sakMedUtbetaling().copy(status = sakStatus)
        SakStatus.FERDIG -> sakMedUtbetaling().copy(status = sakStatus, attestant = navUser())
        SakStatus.FERDIG_ATTESTERT -> sakMedUtbetaling().copy(status = sakStatus, attestant = navUser())
        SakStatus.FEILREGISTRERT -> sakUtenUtbetaling().copy(status = sakStatus)
    }

    fun sakMedUtbetaling(): Sak {
        val sakUtenUtbetaling = sakUtenUtbetaling()

        return sakUtenUtbetaling.copy(
            type = stonadstyperMedUtbetaling.random(),
            utbetalingsType = UtbetalingsType.BRUKER,
            belop = UtbetalingTestData.utbetalingMinimum().belop,
        )
    }

    fun sakUtenUtbetaling(): Sak {
        val saksnummer = Saksnummer(faker.numerify("Mock-#####"))
        return Sak(
            saksnummer = saksnummer,
            behandlingsnummer = Behandlingsnummer(1),
            type = faker.options().option(StonadsType::class.java),
            fnr = FolkeregisterIdent(faker.numerify("###########")),
            beskrivelse = faker.greekPhilosopher().quote(),
            soknadsDato = LocalDate.ofInstant(
                faker.timeAndDate().past(30, TimeUnit.DAYS),
                ZoneId.systemDefault()
            ),
            status = SakStatus.UNDER_BEHANDLING,
            vedtaksResultat = faker.options().option(VedtaksResultat::class.java),
            opprettetDato = faker.timeAndDate().past(1, TimeUnit.DAYS),
            saksbehandler = navUser(),
            tildelingsAar = Aar(faker.number().numberBetween(2020, 2026)),
            begrunnelse = faker.lebowski().quote(),
            attestant = null,
            utbetalingsType = null,
        )
    }

    private fun navUser(): NavUser = NavUser(NavIdent(faker.bothify("???###")), faker.name().name())

    fun nySakMinimum(fnr: FolkeregisterIdent = FolkeregisterIdent(faker.numerify("###########"))): OpprettSakDto {
        return OpprettSakDto(
            type = faker.options().option(StonadsType::class.java),
            fnr = fnr,
            properties = UpdateSakDto(
                status = SakStatus.UNDER_BEHANDLING,
                saksbehandler = navUser()
            )
        )
    }

    fun nySakCompleteUtbetaling(
        fnr: FolkeregisterIdent = FolkeregisterIdent(faker.numerify("###########")),
        sakStatus: SakStatus = SakStatus.UNDER_BEHANDLING,
        saksbehandlerIdent: String = faker.bothify("s??###")
    ): OpprettSakDto {
        val properties = UpdateSakDto(
            beskrivelse = faker.harryPotter().spell(),
            soknadsDato = LocalDate.ofInstant(
                faker.timeAndDate().past(30, TimeUnit.DAYS),
                ZoneId.systemDefault()
            ),
            begrunnelse = faker.yoda().quote().take(250),
            status = sakStatus,
            vedtaksResultat = faker.options().option(VedtaksResultat::class.java),
            saksbehandler = NavUser(NavIdent(saksbehandlerIdent), faker.name().name()),
            utbetalingsType = UtbetalingsType.BRUKER,
            belop = Belop(faker.number().numberBetween(10, 99999)),
        )
        return nySakMinimum(fnr)
            .copy(type = stonadstyperMedUtbetaling.random(), properties = properties)
    }

    @Deprecated("Bruk lagreSak f")
    fun lagreNySak(repository: SakRepository, sak: OpprettSakDto = nySakMinimum()): Sak {
        return withMockedUser {
            repository.opprettNySak(sak)
        }
    }

    fun lagreSak(repository: SakRepository, sak: Sak = sakMedUtbetaling()): Sak {
        return withMockedUser {
            repository.opprettNySak(
                OpprettSakDto(
                    type = sak.type,
                    fnr = sak.fnr,
                    properties = UpdateSakDto(
                        beskrivelse = sak.beskrivelse,
                        soknadsDato = sak.soknadsDato,
                        begrunnelse = sak.begrunnelse,
                        status = sak.status,
                        vedtaksResultat = sak.vedtaksResultat,
                        saksbehandler = sak.saksbehandler,
                        attestant = sak.attestant,
                        utbetalingsType = sak.utbetalingsType,
                        belop = sak.belop,
                        tildelingsAar = sak.tildelingsAar
                    )
                )
            )
        }
    }
}
