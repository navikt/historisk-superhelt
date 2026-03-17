package no.nav.historisk.superhelt.vedtak

import net.datafaker.Faker
import no.nav.common.types.NavIdent
import no.nav.historisk.superhelt.infrastruktur.authentication.NavUser
import no.nav.historisk.superhelt.sak.Sak
import java.time.Instant
import java.time.LocalDate

object VedtakTestData {

    private val faker = Faker()

    fun vedtakForSak(sak: Sak): Vedtak {
        return Vedtak(
            saksnummer = sak.saksnummer,
            behandlingsnummer = sak.behandlingsnummer,
            stonadstype = sak.type,
            fnr = sak.fnr,
            beskrivelse = faker.lorem().sentence(),
            soknadsDato = sak.soknadsDato ?: LocalDate.now(),
            tildelingsAar = sak.tildelingsAar,
            begrunnelse = faker.lorem().sentence(),
            resultat = sak.vedtaksResultat ?: VedtaksResultat.INNVILGET,
            vedtaksTidspunkt = Instant.now(),
            saksbehandler = sak.saksbehandler,
            attestant = sak.attestant ?: NavUser(NavIdent(faker.bothify("A??###")), faker.name().name()),
            utbetalingsType = sak.utbetalingsType,
            belop = sak.belop,
        )
    }
}
