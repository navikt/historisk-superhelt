package no.nav.historisk.superhelt.brev

import net.datafaker.Faker

object BrevTestdata {

    private val faker: Faker = Faker()


    fun vedtaksbrevBruker(): BrevUtkast{
        return brevUtkast().copy(type = BrevType.VEDTAKSBREV, mottakerType = BrevMottaker.BRUKER)
    }

    fun brevUtkast(): BrevUtkast {
        return BrevUtkast(
            tittel= faker.lorem().sentence(),
            innhold = faker.lorem().paragraphs(2).joinToString("<br/>"),
            uuid = BrevId.random(),
            type = faker.options().option(BrevType::class.java),
            mottakerType = faker.options().option(BrevMottaker::class.java),
            status = BrevStatus.UNDER_ARBEID,
        )
    }
}