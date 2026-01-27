package no.nav.historisk.superhelt.utbetaling.kafka

import no.nav.historisk.superhelt.sak.StonadsType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class KlassekodeMapperTest {

    @ParameterizedTest
    @EnumSource(StonadsType::class)
    fun `skal mappe alle StonadsType til KlasseKode`(stonadsType: StonadsType) {
        assertThat(stonadsType.klassekode).isNotNull
    }
}