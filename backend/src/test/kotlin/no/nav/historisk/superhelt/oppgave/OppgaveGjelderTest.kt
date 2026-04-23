package no.nav.historisk.superhelt.oppgave

import no.nav.historisk.superhelt.StonadsType
import no.nav.oppgave.OppgaveKodeverkValidator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class OppgaveGjelderTest {

    @ParameterizedTest
    @EnumSource(StonadsType::class)
    fun `skal mappe alle StonadsType til Oppgavegjelder`(stonadsType: StonadsType) {
        assertThat(stonadsType.tilOppgaveGjelder()).isNotNull
    }

    @ParameterizedTest
    @EnumSource(OppgaveGjelder::class)
    fun `oppgavegjelder har gyldig kombinasjon`(oppgaveGjelder: OppgaveGjelder) {
        assertThat(OppgaveKodeverkValidator.erGyldig(oppgaveGjelder.tema, oppgaveGjelder.type)).isTrue
    }

}
