package no.nav.historisk.superhelt.sak

import no.nav.historisk.superhelt.brev.BrevStatus
import no.nav.historisk.superhelt.brev.BrevTestdata
import no.nav.historisk.superhelt.infrastruktur.validation.TilstandStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SakTilstandTest {

    @Nested
    inner class Opplysninger {

        @Test
        fun `opplysninger er IKKE_STARTET når sak ikke er startet`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(
                beskrivelse = null,
                begrunnelse = null,
                utbetaling = null,
                forhandstilsagn = null,
                vedtaksResultat = null
            )
            val tilstand = SakTilstand(sak)
            assertThat(tilstand.opplysninger).isEqualTo(TilstandStatus.IKKE_STARTET)
        }

        @Test
        fun `opplysninger er VALIDERING_FEILET når det finnes valideringsfeil`() {
            val sak = SakTestData.sakMedStatus(SakStatus.UNDER_BEHANDLING).copy(
                beskrivelse = "",
                vedtaksResultat = null
            )
            val tilstand = SakTilstand(sak)
            assertThat(tilstand.opplysninger).isEqualTo(TilstandStatus.VALIDERING_FEILET)
        }

        @Test
        fun `opplysninger er OK når sak er startet og ingen valideringsfeil`() {
            val sak = SakTestData.sakMedStatus(SakStatus.UNDER_BEHANDLING)
            val tilstand = SakTilstand(sak)
            assertThat(tilstand.opplysninger).isEqualTo(TilstandStatus.OK)
        }
    }

    @Nested
    inner class VedtaksbrevBruker {
        private val brev = BrevTestdata.vedtaksbrevBruker()

        @Test
        fun `vedtaksbrevBruker er IKKE_STARTET når brev er null`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(vedtaksbrevBruker = null)
            val tilstand = SakTilstand(sak)
            assertThat(tilstand.vedtaksbrevBruker).isEqualTo(TilstandStatus.IKKE_STARTET)
        }

        @Test
        fun `vedtaksbrevBruker er IKKE_STARTET når brev er nytt`() {

            val sak = SakTestData.sakUtenUtbetaling().copy(vedtaksbrevBruker = brev.copy(status = BrevStatus.NY))
            val tilstand = SakTilstand(sak)
            assertThat(tilstand.vedtaksbrevBruker).isEqualTo(TilstandStatus.IKKE_STARTET)
        }

        @Test
        fun `vedtaksbrevBruker er VALIDERING_FEILET når brev har valideringsfeil`() {

            val sak = SakTestData.sakUtenUtbetaling().copy(vedtaksbrevBruker = brev.copy(innhold = "", tittel = ""))
            val tilstand = SakTilstand(sak)
            assertThat(tilstand.vedtaksbrevBruker).isEqualTo(TilstandStatus.VALIDERING_FEILET)
        }

        @Test
        fun `vedtaksbrevBruker er OK når brev er ferdig uten feil`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(vedtaksbrevBruker = brev)
            val tilstand = SakTilstand(sak)
            assertThat(tilstand.vedtaksbrevBruker).isEqualTo(TilstandStatus.OK)
        }
    }

    @Nested
    inner class Oppsummering {

        @Test
        fun `oppsummering er IKKE_STARTET når sak er UNDER_BEHANDLING`() {
            val sak = SakTestData.sakMedStatus(SakStatus.UNDER_BEHANDLING)
            val tilstand = SakTilstand(sak)
            assertThat(tilstand.oppsummering).isEqualTo(TilstandStatus.IKKE_STARTET)
        }

        @Test
        fun `oppsummering er IKKE_STARTET når sak er TIL_ATTESTERING`() {
            val sak = SakTestData.sakMedStatus(SakStatus.TIL_ATTESTERING)
            val tilstand = SakTilstand(sak)
            assertThat(tilstand.oppsummering).isEqualTo(TilstandStatus.IKKE_STARTET)
        }

        @Test
        fun `oppsummering er OK når sak er FERDIG`() {
            val sak = SakTestData.sakMedStatus(SakStatus.FERDIG)
            val tilstand = SakTilstand(sak)
            assertThat(tilstand.oppsummering).isEqualTo(TilstandStatus.OK)
        }
    }
}
