package no.nav.historisk.superhelt.sak

import no.nav.historisk.superhelt.infrastruktur.Role
import no.nav.historisk.superhelt.test.WithAttestant
import no.nav.historisk.superhelt.test.WithMockJwtAuth
import no.nav.historisk.superhelt.test.WithSaksbehandler
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
class SakRettigheterTest {

    @Nested
    @WithMockJwtAuth(roles = [Role.LES])
    inner class `Bruker med kun leserettighet` {

        @Test
        fun `får kun LES-rettighet når sak er under behandling`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(status = SakStatus.UNDER_BEHANDLING)
            assertThat(sak.rettigheter).containsExactlyInAnyOrder(SakRettighet.LES)
        }

        @Test
        fun `får kun LES-rettighet når sak er til attestering`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(status = SakStatus.TIL_ATTESTERING)
            assertThat(sak.rettigheter).containsExactlyInAnyOrder(SakRettighet.LES)
        }

        @Test
        fun `får kun LES-rettighet når sak er ferdig`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(status = SakStatus.FERDIG)
            assertThat(sak.rettigheter).containsExactlyInAnyOrder(SakRettighet.LES)
        }
    }

    @Nested
    @WithSaksbehandler
    inner class Saksbehandler {

        @Test
        fun `får LES og SAKSBEHANDLE når sak er under behandling`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(
                status = SakStatus.UNDER_BEHANDLING,
                saksbehandler = "annen-saksbehandler"
            )
            assertThat(sak.rettigheter).containsExactlyInAnyOrder(SakRettighet.LES, SakRettighet.SAKSBEHANDLE)
        }

        @Test
        fun `får kun LES når sak er til attestering`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(
                status = SakStatus.TIL_ATTESTERING,
                saksbehandler = "annen-saksbehandler"
            )
            assertThat(sak.rettigheter).containsExactlyInAnyOrder(SakRettighet.LES)
        }

        @Test
        fun `får LES og GJENAPNE når sak er ferdig`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(
                status = SakStatus.FERDIG,
                saksbehandler = "annen-saksbehandler"
            )
            assertThat(sak.rettigheter).containsExactlyInAnyOrder(SakRettighet.LES, SakRettighet.GJENAPNE)
        }
    }

    @Nested
    @WithAttestant
    inner class Attestant {

        @Test
        fun `får kun LES når sak er under behandling`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(
                status = SakStatus.UNDER_BEHANDLING,
                saksbehandler = "saks-1"
            )
            assertThat(sak.rettigheter).containsExactlyInAnyOrder(SakRettighet.LES)
        }

        @Test
        fun `får FERDIGSTILL når sak er til attestering og attestant er ikke saksbehandler`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(
                status = SakStatus.TIL_ATTESTERING,
                saksbehandler = "saks-1"
            )
            assertThat(sak.rettigheter).containsExactlyInAnyOrder(SakRettighet.LES, SakRettighet.FERDIGSTILLE)
        }

        @Test
        @WithAttestant(navIdent = "saks-1")
        fun `får kun LES når sak er til attestering og attestant er samme som saksbehandler`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(
                status = SakStatus.TIL_ATTESTERING,
                saksbehandler = "saks-1"
            )
            assertThat(sak.rettigheter).containsExactlyInAnyOrder(SakRettighet.LES)
        }

        @Test
        fun `får kun LES når sak er ferdig`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(
                status = SakStatus.FERDIG,
                saksbehandler = "saks-1"
            )
            assertThat(sak.rettigheter).containsExactlyInAnyOrder(SakRettighet.LES)
        }
    }

}