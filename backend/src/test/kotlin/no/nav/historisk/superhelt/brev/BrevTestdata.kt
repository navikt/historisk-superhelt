package no.nav.historisk.superhelt.brev

import net.datafaker.Faker
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.test.withMockedUser
import java.time.Instant

object BrevTestdata {

    private val faker: Faker = Faker()


    fun vedtaksbrevBruker(): Brev {
        return brevUtkast().copy(type = BrevType.VEDTAKSBREV, mottakerType = BrevMottaker.BRUKER)
    }
    fun fritekstbrevBruker(): Brev {
        return brevUtkast().copy(type = BrevType.FRITEKSTBREV, mottakerType = BrevMottaker.BRUKER)
    }
    fun henleggBrev(): Brev {
        return brevUtkast().copy(type = BrevType.HENLEGGESEBREV, mottakerType = BrevMottaker.BRUKER)
    }

    fun brevUtkast(): Brev {
        return Brev(
            saksnummer = Saksnummer(faker.number().randomNumber()),
            tittel = faker.lorem().sentence(),
            innhold = faker.lorem().paragraphs(2).joinToString("<br/>"),
            uuid = BrevId.random(),
            type = faker.options().option(BrevType::class.java),
            mottakerType = faker.options().option(BrevMottaker::class.java),
            status = BrevStatus.UNDER_ARBEID,
            opprettetTidspunkt = Instant.now()
        )
    }

    fun lagreBrev(
        brevRepository: BrevRepository,
        saksnummer: Saksnummer,
        brev: Brev = vedtaksbrevBruker()): Brev {
        return withMockedUser {
            brevRepository.opprettBrev(brev.copy(saksnummer = saksnummer))
        }
    }
}