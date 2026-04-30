package no.nav.historisk.superhelt.sak

import no.nav.common.types.Aar
import no.nav.common.types.Belop
import no.nav.common.types.NavIdent
import no.nav.helved.KlasseKode
import no.nav.historisk.superhelt.StonadsType
import no.nav.historisk.superhelt.infrastruktur.authentication.NavUser
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.historisk.superhelt.utbetaling.UtbetalingsType
import no.nav.historisk.superhelt.vedtak.VedtaksResultat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

@MockedSpringBootTest
@WithSaksbehandler
class SakRepositoryTest {

    @Autowired
    private lateinit var sakRepository: SakRepository

    @Nested
    inner class UpdateSak {

        @Test
        fun `skal oppdatere beskrivelse`() {
            val sak = SakTestData.lagreSak(sakRepository)

            sakRepository.updateSak(sak.saksnummer, UpdateSakDto(beskrivelse = "Ny beskrivelse"))

            val oppdatert = sakRepository.getSak(sak.saksnummer)
            assertThat(oppdatert.beskrivelse).isEqualTo("Ny beskrivelse")
        }

        @Test
        fun `skal oppdatere begrunnelse`() {
            val sak = SakTestData.lagreSak(sakRepository)

            sakRepository.updateSak(sak.saksnummer, UpdateSakDto(begrunnelse = "Ny begrunnelse"))

            val oppdatert = sakRepository.getSak(sak.saksnummer)
            assertThat(oppdatert.begrunnelse).isEqualTo("Ny begrunnelse")
        }

        @Test
        fun `skal oppdatere status`() {
            val sak = SakTestData.lagreSak(sakRepository)

            sakRepository.updateSak(sak.saksnummer, UpdateSakDto(status = SakStatus.TIL_ATTESTERING))

            val oppdatert = sakRepository.getSak(sak.saksnummer)
            assertThat(oppdatert.status).isEqualTo(SakStatus.TIL_ATTESTERING)
        }

        @Test
        fun `skal oppdatere soknadsDato og tildelingsAar`() {
            val sak = SakTestData.lagreSak(sakRepository)
            val nyDato = LocalDate.of(2024, 3, 15)
            val nyttAar = Aar(2024)

            sakRepository.updateSak(
                sak.saksnummer,
                UpdateSakDto(soknadsDato = nyDato, tildelingsAar = nyttAar)
            )

            val oppdatert = sakRepository.getSak(sak.saksnummer)
            assertThat(oppdatert.soknadsDato).isEqualTo(nyDato)
            assertThat(oppdatert.tildelingsAar).isEqualTo(nyttAar)
        }

        @Test
        fun `skal oppdatere vedtaksResultat`() {
            val sak = SakTestData.lagreSak(sakRepository)

            sakRepository.updateSak(sak.saksnummer, UpdateSakDto(vedtaksResultat = VedtaksResultat.INNVILGET))

            val oppdatert = sakRepository.getSak(sak.saksnummer)
            assertThat(oppdatert.vedtaksResultat).isEqualTo(VedtaksResultat.INNVILGET)
        }

        @Test
        fun `skal oppdatere saksbehandler`() {
            val sak = SakTestData.lagreSak(sakRepository)
            val nySaksbehandler = NavUser(NavIdent("A123456"), "Ny Saksbehandler")

            sakRepository.updateSak(sak.saksnummer, UpdateSakDto(saksbehandler = nySaksbehandler))

            val oppdatert = sakRepository.getSak(sak.saksnummer)
            assertThat(oppdatert.saksbehandler).isEqualTo(nySaksbehandler)
        }

        @Test
        fun `skal sette attestant og deretter nullstille med NULL_VALUE`() {
            val sak = SakTestData.lagreSak(sakRepository)
            val attestant = NavUser(NavIdent("B654321"), "Attestant Person")

            sakRepository.updateSak(sak.saksnummer, UpdateSakDto(attestant = attestant))
            val medAttestant = sakRepository.getSak(sak.saksnummer)
            assertThat(medAttestant.attestant).isEqualTo(attestant)

            sakRepository.updateSak(sak.saksnummer, UpdateSakDto(attestant = NavUser.NULL_VALUE))
            val utenAttestant = sakRepository.getSak(sak.saksnummer)
            assertThat(utenAttestant.attestant).isNull()
        }

        @Nested
        inner class UtbetalingsTypeOgBelop {

            @Test
            fun `skal sette belop og klassekode når utbetalingsType er BRUKER`() {
                val sak = SakTestData.lagreSak(
                    sakRepository,
                    SakTestData.sakMedUtbetaling()
                        .copy(type = StonadsType.PARYKK, utbetalingsType = UtbetalingsType.BRUKER, belop = Belop(5000))
                )

                val oppdatert = sakRepository.getSak(sak.saksnummer)
                assertThat(oppdatert.utbetalingsType).isEqualTo(UtbetalingsType.BRUKER)
                assertThat(oppdatert.belop).isEqualTo(Belop(5000))
                assertThat(oppdatert.klasseKode).isEqualTo(KlasseKode.PARYKK)
            }


            @Test
            fun `skal nullstille belop og klassekode når utbetalingsType er INGEN`() {
                val sak = SakTestData.lagreSak(
                    sakRepository,
                    SakTestData.sakMedUtbetaling()
                        .copy(type = StonadsType.PARYKK, utbetalingsType = UtbetalingsType.BRUKER, belop = Belop(2000))
                )

                sakRepository.updateSak(
                    sak.saksnummer,
                    UpdateSakDto(utbetalingsType = UtbetalingsType.INGEN)
                )

                val oppdatert = sakRepository.getSak(sak.saksnummer)
                assertThat(oppdatert.belop).isNull()
                assertThat(oppdatert.lagretKlassekodeForTest()).isNull()
            }
        }

        @Nested
        inner class TypeEndring {

            @Test
            fun `skal nullstille klassekode når type endres`() {
                val sak = SakTestData.lagreSak(
                    sakRepository,
                    SakTestData.sakMedUtbetaling()
                        .copy(type = StonadsType.PARYKK, utbetalingsType = UtbetalingsType.BRUKER, belop = Belop(5000))
                )

                sakRepository.updateSak(sak.saksnummer, UpdateSakDto(type = StonadsType.FOTTOY))

                val oppdatert = sakRepository.getSak(sak.saksnummer)
                assertThat(oppdatert.type).isEqualTo(StonadsType.FOTTOY)
                assertThat(oppdatert.lagretKlassekodeForTest()).isNull()
            }

            @Test
            fun `skal beholde klassekode når type ikke endres`() {
                val sak = SakTestData.lagreSak(
                    sakRepository,
                    SakTestData.sakMedUtbetaling()
                        .copy(type = StonadsType.PARYKK, utbetalingsType = UtbetalingsType.BRUKER, belop = Belop(1500))
                )

                sakRepository.updateSak(sak.saksnummer, UpdateSakDto(beskrivelse = "Endret beskrivelse"))

                val oppdatert = sakRepository.getSak(sak.saksnummer)
                assertThat(oppdatert.klasseKode).isEqualTo(KlasseKode.PARYKK)
            }
        }
    }
}

/** Eksponerer lagretKlassekode for testformål uten å endre produksjonsmodellen. */
private fun Sak.lagretKlassekodeForTest(): KlasseKode? {
    val field = Sak::class.java.getDeclaredField("lagretKlassekode")
    field.isAccessible = true
    return field.get(this) as KlasseKode?
}
