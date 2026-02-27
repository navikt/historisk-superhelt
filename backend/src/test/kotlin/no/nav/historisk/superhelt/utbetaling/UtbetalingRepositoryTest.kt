package no.nav.historisk.superhelt.utbetaling

import no.nav.common.types.Behandlingsnummer
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.withMockedUser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser

@MockedSpringBootTest
@WithMockUser(authorities = ["READ", "WRITE"])
class UtbetalingRepositoryTest {

    @Autowired
    private lateinit var utbetalingRepository: UtbetalingRepository

    @Autowired
    private lateinit var sakRepository: SakRepository

    @Test
    fun `opprettUtbetaling lagrer korrekt behandlingsnummer og beløp`() {
        val sak = withMockedUser { SakTestData.lagreNySak(sakRepository, SakTestData.nySakCompleteUtbetaling()) }

        val utbetaling = utbetalingRepository.opprettUtbetaling(sak)

        assertThat(utbetaling.behandlingsnummer).isEqualTo(sak.behandlingsnummer)
        assertThat(utbetaling.belop).isEqualTo(sak.belop)
        assertThat(utbetaling.saksnummer).isEqualTo(sak.saksnummer)
        assertThat(utbetaling.utbetalingStatus).isEqualTo(UtbetalingStatus.UTKAST)
        assertThat(utbetaling.utbetalingTidspunkt).isNull()
    }

    @Test
    fun `opprettUtbetaling kaster feil når sak mangler beløp`() {
        val sakUtenBelop = SakTestData.sakUtenUtbetaling()

        assertThrows<Exception> {
            utbetalingRepository.opprettUtbetaling(sakUtenBelop)
        }
    }

    @Test
    fun `findActiveByBehandling returnerer aktiv utbetaling for gjeldende behandlingsnummer`() {
        val sak = withMockedUser { SakTestData.lagreNySak(sakRepository, SakTestData.nySakCompleteUtbetaling()) }
        utbetalingRepository.opprettUtbetaling(sak)

        val funnet = utbetalingRepository.findActiveByBehandling(sak)

        assertThat(funnet).isNotNull
        assertThat(funnet!!.behandlingsnummer).isEqualTo(sak.behandlingsnummer)
        assertThat(funnet.utbetalingStatus).isEqualTo(UtbetalingStatus.UTKAST)
    }

    @Test
    fun `findActiveByBehandling returnerer null for annet behandlingsnummer`() {
        val sak = withMockedUser { SakTestData.lagreNySak(sakRepository, SakTestData.nySakCompleteUtbetaling()) }
        utbetalingRepository.opprettUtbetaling(sak)

        // Simuler gjenåpnet sak med nytt behandlingsnummer
        val gjenapnetSak = withMockedUser { sakRepository.incrementBehandlingsNummer(sak.saksnummer) }
        assertThat(gjenapnetSak.behandlingsnummer).isEqualTo(Behandlingsnummer(2))

        val funnet = utbetalingRepository.findActiveByBehandling(gjenapnetSak)

        assertThat(funnet).isNull()
    }

    @Test
    fun `findActiveByBehandling foretrekker ikke-final utbetaling`() {
        val sak = withMockedUser { SakTestData.lagreNySak(sakRepository, SakTestData.nySakCompleteUtbetaling()) }
        val utbetaling1 = utbetalingRepository.opprettUtbetaling(sak)
        utbetalingRepository.setUtbetalingStatus(utbetaling1.uuid, UtbetalingStatus.UTBETALT)
        val utbetaling2 = utbetalingRepository.opprettUtbetaling(sak)

        val funnet = utbetalingRepository.findActiveByBehandling(sak)

        assertThat(funnet!!.uuid).isEqualTo(utbetaling2.uuid)
        assertThat(funnet.utbetalingStatus).isEqualTo(UtbetalingStatus.UTKAST)
    }

    @Test
    fun `findActiveByBehandling returnerer siste final utbetaling når alle er ferdige`() {
        val sak = withMockedUser { SakTestData.lagreNySak(sakRepository, SakTestData.nySakCompleteUtbetaling()) }
        val utbetaling = utbetalingRepository.opprettUtbetaling(sak)
        utbetalingRepository.setUtbetalingStatus(utbetaling.uuid, UtbetalingStatus.UTBETALT)

        val funnet = utbetalingRepository.findActiveByBehandling(sak)

        assertThat(funnet!!.uuid).isEqualTo(utbetaling.uuid)
    }
}
