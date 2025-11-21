package no.nav.historisk.superhelt.sak

import net.datafaker.Faker
import no.nav.common.types.Behandlingsnummer
import no.nav.common.types.Fnr
import no.nav.common.types.NavIdent
import no.nav.historisk.superhelt.sak.db.SakJpaEntity
import no.nav.historisk.superhelt.utbetaling.UtbetalingTestData
import no.nav.historisk.superhelt.vedtak.VedtaksResultat
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object SakTestData {

    private val faker: Faker = Faker()

    fun sakUtenUtbetaling(): Sak {
        val saksnummer = faker.numerify("Mock-#####")
        return Sak(
            saksnummer = Saksnummer(saksnummer),
            behandlingsnummer = Behandlingsnummer(saksnummer, 1),
            type = faker.options().option(StonadsType::class.java),
            fnr = Fnr(faker.numerify("###########")),
            tittel = faker.greekPhilosopher().quote(),
            soknadsDato = LocalDate.ofInstant(
                faker.timeAndDate().past(30, TimeUnit.DAYS),
                ZoneId.systemDefault()
            ),
            status = SakStatus.UNDER_BEHANDLING,
            vedtaksResultat = faker.options().option(VedtaksResultat::class.java),
            opprettetDato = faker.timeAndDate().past(1, TimeUnit.DAYS),
            saksbehandler = NavIdent(faker.bothify("???###")),
            attestant = null,
            utbetaling = null,
            forhandstilsagn = null
        )
    }

    fun sakMedUtbetaling() = sakUtenUtbetaling().copy(
        utbetaling = UtbetalingTestData.utbetalingMinimum()
    )

    fun sakEntityMinimum(fnr: Fnr = Fnr(faker.numerify("###########"))): SakJpaEntity {
        return SakJpaEntity(
            type = faker.options().option(StonadsType::class.java),
            fnr = fnr,
            status = SakStatus.UNDER_BEHANDLING,
            saksbehandler = NavIdent(faker.greekPhilosopher().name())
        )
    }

    fun sakEntityCompleteUtbetaling(
        fnr: Fnr = Fnr(faker.numerify("###########")),
        sakStatus: SakStatus = SakStatus.UNDER_BEHANDLING
    ): SakJpaEntity {
        val sak = sakEntityMinimum(fnr)
        with(sak) {
            tittel = faker.harryPotter().quote()
            soknadsDato = LocalDate.ofInstant(
                faker.timeAndDate().past(30, TimeUnit.DAYS),
                ZoneId.systemDefault()
            )
            begrunnelse = faker.yoda().quote()
            status = sakStatus
            vedtaksResultat = faker.options().option(VedtaksResultat::class.java)
            saksbehandler = NavIdent(faker.bothify("s??###"))

            setOrUpdateUtbetaling(faker.number().numberBetween(10, 99999))
        }
        return sak

    }


}