package no.nav.historisk.superhelt.oppgave

import no.nav.historisk.superhelt.sak.StonadsType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class TilOppgaveGjelderTest {
    @ParameterizedTest
    @EnumSource(StonadsType::class)
    fun `skal mappe alle StonadsType til Oppgavegjelder`(stonadsType: StonadsType) {
        assertThat(stonadsType.tilOppgaveGjelder()).isNotNull
    }

}