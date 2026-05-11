package no.nav.historisk.superhelt.sak

import no.nav.common.consts.FellesKodeverkTema
import no.nav.common.types.Aar
import no.nav.common.types.Belop
import no.nav.common.types.FolkeregisterIdent
import no.nav.common.types.NavIdent
import no.nav.common.types.Saksnummer
import no.nav.helved.KlasseKode
import no.nav.historisk.superhelt.StonadsType
import no.nav.historisk.superhelt.infrastruktur.authentication.NavUser
import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.historisk.superhelt.person.TilgangsmaskinTestData
import no.nav.historisk.superhelt.person.tilgangsmaskin.TilgangsmaskinService
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.WithLeseBruker
import no.nav.historisk.superhelt.test.WithMockJwtAuth
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.historisk.superhelt.utbetaling.UtbetalingsType
import no.nav.historisk.superhelt.vedtak.VedtaksResultat
import no.nav.tilgangsmaskin.TilgangsmaskinClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDate

@MockedSpringBootTest
class SakRepositoryTest {

    @Autowired
    private lateinit var sakRepository: SakRepository

    @MockitoBean
    private lateinit var tilgangsmaskinService: TilgangsmaskinService

    @BeforeEach
    fun setupTilgangsmaskin() {
        whenever(tilgangsmaskinService.sjekkKomplettTilgang(any())) doReturn TilgangsmaskinClient.TilgangResult(
            harTilgang = true
        )
    }

    @WithSaksbehandler
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
            fun `skal nullstille belop når utbetalingsType er INGEN`() {
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
                assertThat(oppdatert.klasseKode).isEqualTo(KlasseKode.PARYKK)
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
                assertThat(oppdatert.klasseKode).isEqualTo(KlasseKode.VANLIGE_SKO)
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

    @WithLeseBruker
    @Nested
    inner class HentSak {

        @Test
        fun `skal returnere korrekt sak`() {
            val lagretSak = SakTestData.lagreSak(sakRepository)

            val hentetSak = sakRepository.getSak(lagretSak.saksnummer)

            assertThat(hentetSak.saksnummer).isEqualTo(lagretSak.saksnummer)
            assertThat(hentetSak.fnr).isEqualTo(lagretSak.fnr)
            assertThat(hentetSak.type).isEqualTo(lagretSak.type)
            assertThat(hentetSak.status).isEqualTo(lagretSak.status)
        }

        @Test
        fun `skal kaste IkkeFunnetException for ukjent saksnummer`() {
            val ikkeEksisterendeSaksnummer = Saksnummer("SH-999999")

            assertThatThrownBy {
                sakRepository.getSak(ikkeEksisterendeSaksnummer)
            }.isInstanceOf(IkkeFunnetException::class.java)
        }

        @WithMockJwtAuth(permissions = [])
        @Test
        fun `skal nekte tilgang uten READ-tillatelse`() {
            val lagretSak = SakTestData.lagreSak(sakRepository)

            assertThatThrownBy { sakRepository.getSak(lagretSak.saksnummer) }.isInstanceOf(AuthorizationDeniedException::class.java)
        }

        @Test
        fun `skal nekte tilgang når tilgangsmaskin avviser`() {
            val lagretSak = SakTestData.lagreSak(sakRepository)
            whenever(tilgangsmaskinService.sjekkKomplettTilgang(any())) doReturn TilgangsmaskinClient.TilgangResult(
                harTilgang = false,
                response = TilgangsmaskinTestData.problemDetailResponse
            )

            assertThatThrownBy {
                sakRepository.getSak(lagretSak.saksnummer)
            }.isInstanceOf(AuthorizationDeniedException::class.java)
        }

        @WithSaksbehandler(tema = [FellesKodeverkTema.HEL])
        @Test
        fun `skal nekte tilgang for feil tema`() {
            val hjeSak = SakTestData.lagreSak(
                sakRepository,
                SakTestData.sakUtenUtbetaling().copy(type = StonadsType.HOREAPPARAT)
            )

            assertThatThrownBy { sakRepository.getSak(hjeSak.saksnummer) }.isInstanceOf(AuthorizationDeniedException::class.java)
                .hasMessageContaining("Mangler tilgang til tema")
        }
    }

    @WithLeseBruker
    @Nested
    inner class FinnSaker {

        @Test
        fun `skal returnere alle saker for en person`() {
            val baseSak = SakTestData.sakUtenUtbetaling()
            val fnr = baseSak.fnr
            val sak1 = SakTestData.lagreSak(sakRepository, baseSak.copy(type = StonadsType.PARYKK))
            val sak2 = SakTestData.lagreSak(sakRepository, SakTestData.sakUtenUtbetaling().copy(fnr = fnr, type = StonadsType.PARYKK))

            val saker = sakRepository.finnSaker(fnr)

            assertThat(saker).hasSize(2)
            assertThat(saker.map { it.saksnummer }).containsExactlyInAnyOrder(sak1.saksnummer, sak2.saksnummer)
        }

        @Test
        fun `skal returnere tom liste for person uten saker`() {
            val fnrUtenSaker = FolkeregisterIdent("99999999999")

            val saker = sakRepository.finnSaker(fnrUtenSaker)

            assertThat(saker).isEmpty()
        }

        @WithSaksbehandler(tema = [FellesKodeverkTema.HEL])
        @Test
        fun `skal filtrere bort saker med tema bruker ikke har tilgang til`() {
            val baseSak = SakTestData.sakUtenUtbetaling()
            val fnr = baseSak.fnr
            val helSak = SakTestData.lagreSak(sakRepository, baseSak.copy(type = StonadsType.PARYKK))
            SakTestData.lagreSak(sakRepository, SakTestData.sakUtenUtbetaling().copy(fnr = fnr, type = StonadsType.HOREAPPARAT))

            val saker = sakRepository.finnSaker(fnr)

            assertThat(saker).hasSize(1)
            assertThat(saker.first().saksnummer).isEqualTo(helSak.saksnummer)
        }

        @WithMockJwtAuth(permissions = [])
        @Test
        fun `skal nekte tilgang uten READ-tillatelse`() {
            val fnr = FolkeregisterIdent("12345678901")

            assertThatThrownBy { sakRepository.finnSaker(fnr) }
                .isInstanceOf(AuthorizationDeniedException::class.java)
        }

        @Test
        fun `skal nekte tilgang når tilgangsmaskin avviser`() {
            val fnr = FolkeregisterIdent("12345678901")
            whenever(tilgangsmaskinService.sjekkKomplettTilgang(any())) doReturn TilgangsmaskinClient.TilgangResult(
                harTilgang = false,
                response = TilgangsmaskinTestData.problemDetailResponse
            )

            assertThatThrownBy { sakRepository.finnSaker(fnr) }
                .isInstanceOf(AuthorizationDeniedException::class.java)
        }
    }
}
