package no.nav.kabal.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class KabalUtfallTest {

    @ParameterizedTest
    @EnumSource(KlageUtfall::class)
    @EnumSource(AnkeUtfall::class)
    @EnumSource(AnkeITrygderettenUtfall::class)
    @EnumSource(OmgjoeringskravUtfall::class)
    @EnumSource(GjenopptaksUtfall::class)
    fun `parse mulige utfall til kabalutfall`(utfall: Enum<*>) {
        assertEquals(utfall.name, KabalUtfall.valueOf(utfall.name).name)
    }

}
