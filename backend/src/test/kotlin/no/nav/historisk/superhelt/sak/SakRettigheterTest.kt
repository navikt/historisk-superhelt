package no.nav.historisk.superhelt.sak

import no.nav.common.types.Behandlingsnummer
import no.nav.common.types.NavIdent
import no.nav.historisk.superhelt.infrastruktur.authentication.NavUser
import no.nav.historisk.superhelt.infrastruktur.authentication.Role
import no.nav.historisk.superhelt.test.WithAttestant
import no.nav.historisk.superhelt.test.WithMockJwtAuth
import no.nav.historisk.superhelt.test.WithSaksbehandler
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
class SakRettigheterTest {

    @Nested
    @WithMockJwtAuth(roles = [Role.LES])
    inner class `Bruker med kun leserettighet` {

        @EnumSource(SakStatus::class)
        @ParameterizedTest
        fun `får kun LES-rettighet`(status: SakStatus) {
            val sak = SakTestData.sakUtenUtbetaling().copy(status = status)
            assertThat(sak.rettigheter).containsExactlyInAnyOrder(SakRettighet.LES)
        }
    }

    @Nested
    @WithSaksbehandler
    inner class Saksbehandler {

        @EnumSource(SakStatus::class)
        @ParameterizedTest
        fun `har leserettighet`(status: SakStatus) {
            val sak = SakTestData.sakUtenUtbetaling().copy(status = status)
            assertThat(sak.rettigheter).contains(SakRettighet.LES)
        }

        @Test
        fun `får flere rettigheter når sak er under behandling`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(
                status = SakStatus.UNDER_BEHANDLING
            )

            assertThat(sak.rettigheter).containsExactlyInAnyOrder(
                SakRettighet.LES,
                SakRettighet.SAKSBEHANDLE,
                SakRettighet.FEILREGISTERE,
                SakRettighet.HENLEGGE
            )
        }

        @Test
        fun `får ikke feilregister og henlegg når sak er gjenåpnet`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(
                status = SakStatus.UNDER_BEHANDLING,
                behandlingsnummer = Behandlingsnummer(2)
            )
            assertThat( sak.gjenapnet ).isTrue
            assertThat(sak.rettigheter).containsExactlyInAnyOrder(
                SakRettighet.LES,
                SakRettighet.SAKSBEHANDLE,
                SakRettighet.TILBAKESTILL_GJENAPNING
            )
        }

        @Test
        fun `får kun LES når sak er til attestering`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(
                status = SakStatus.TIL_ATTESTERING
            )
            assertThat(sak.rettigheter).containsExactlyInAnyOrder(SakRettighet.LES)
        }

        @Test
        fun `får LES, GJENAPNE og SEND_KLAGE når sak er ferdig`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(
                status = SakStatus.FERDIG
            )
            assertThat(sak.rettigheter).containsExactlyInAnyOrder(
                SakRettighet.LES,
                SakRettighet.GJENAPNE,
                SakRettighet.SEND_KLAGE,
            )
        }

        @Test
        fun `får kun LES når sak er feilregistert`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(
                status = SakStatus.FEILREGISTRERT
            )
            assertThat(sak.rettigheter).containsExactlyInAnyOrder(SakRettighet.LES)
        }
    }

    @Nested
    @WithAttestant
    inner class Attestant {

        @Test
        fun `får kun LES når sak er under behandling`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(
                status = SakStatus.UNDER_BEHANDLING,

                )
            assertThat(sak.rettigheter).containsExactlyInAnyOrder(SakRettighet.LES)
        }

        @Test
        fun `får FERDIGSTILLE når sak er til attestering og attestant er ikke saksbehandler`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(
                status = SakStatus.TIL_ATTESTERING,
                saksbehandler = NavUser(NavIdent("saks-1"), "Saks Behandler")
            )
            assertThat(sak.rettigheter).containsExactlyInAnyOrder(SakRettighet.LES, SakRettighet.ATTESTERE)
        }

        @Test
        @WithAttestant(navIdent = "saks-1")
        fun `får kun LES når sak er til attestering og attestant er samme som saksbehandler`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(
                status = SakStatus.TIL_ATTESTERING,
                saksbehandler = NavUser(NavIdent("saks-1"), "Saks Behandler")
            )
            assertThat(sak.rettigheter).containsExactlyInAnyOrder(SakRettighet.LES)
        }

        @Test
        fun `får kun LES når sak er ferdig`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(
                status = SakStatus.FERDIG,
            )
            assertThat(sak.rettigheter).containsExactlyInAnyOrder(SakRettighet.LES)
        }

        @Test
        fun `får kun LES når sak er feilregistert`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(
                status = SakStatus.FEILREGISTRERT
            )
            assertThat(sak.rettigheter).containsExactlyInAnyOrder(SakRettighet.LES)
        }
    }

}
