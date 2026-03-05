package no.nav.historisk.superhelt.vedtak

import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.withMockedUser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser

@MockedSpringBootTest
@WithMockUser(authorities = ["READ", "WRITE"])
class VedtakRepositoryTest {

    @Autowired
    private lateinit var vedtakRepository: VedtakRepository

    @Autowired
    private lateinit var sakRepository: SakRepository

    @Test
    fun `findBySak returnerer tomt resultat når ingen vedtak er lagret for saken`() {
        val sak = withMockedUser { SakTestData.lagreNySak(sakRepository) }

        val vedtak = vedtakRepository.findBySak(sak.saksnummer)

        assertThat(vedtak).isEmpty()
    }

    @Test
    fun `save og findBySak lagrer og henter korrekte felter`() {
        val sak = withMockedUser { SakTestData.lagreNySak(sakRepository, SakTestData.nySakCompleteUtbetaling()) }
        val vedtak = VedtakTestData.vedtakForSak(sak)

        withMockedUser { vedtakRepository.save(vedtak) }

        val lagretVedtak = vedtakRepository.findBySak(sak.saksnummer)
        assertThat(lagretVedtak).singleElement().satisfies({ v ->
            assertThat(v.saksnummer).isEqualTo(sak.saksnummer)
            assertThat(v.behandlingsnummer).isEqualTo(sak.behandlingsnummer)
            assertThat(v.fnr).isEqualTo(sak.fnr)
            assertThat(v.stonadstype).isEqualTo(sak.type)
            assertThat(v.resultat).isEqualTo(vedtak.resultat)
            assertThat(v.utbetalingsType).isEqualTo(sak.utbetalingsType)
            assertThat(v.belop).isEqualTo(sak.belop)
            assertThat(v.soknadsDato).isEqualTo(vedtak.soknadsDato)
            assertThat(v.saksbehandler).isEqualTo(sak.saksbehandler)
            assertThat(v.attestant).isEqualTo(vedtak.attestant)
        })
    }

    @Test
    fun `findBySak returnerer alle vedtak for saken ved flere behandlinger`() {
        val sak1 = withMockedUser { SakTestData.lagreNySak(sakRepository, SakTestData.nySakCompleteUtbetaling()) }
        val sak2 = withMockedUser { SakTestData.lagreNySak(sakRepository, SakTestData.nySakCompleteUtbetaling()) }
        withMockedUser { vedtakRepository.save(VedtakTestData.vedtakForSak(sak1)) }
        withMockedUser { vedtakRepository.save(VedtakTestData.vedtakForSak(sak2)) }

        assertThat(vedtakRepository.findBySak(sak1.saksnummer)).hasSize(1)
        assertThat(vedtakRepository.findBySak(sak2.saksnummer)).hasSize(1)
    }

    @Test
    fun `findBySak returnerer kun vedtak for riktig sak`() {
        val sak1 = withMockedUser { SakTestData.lagreNySak(sakRepository, SakTestData.nySakCompleteUtbetaling()) }
        val sak2 = withMockedUser { SakTestData.lagreNySak(sakRepository, SakTestData.nySakCompleteUtbetaling()) }
        withMockedUser { vedtakRepository.save(VedtakTestData.vedtakForSak(sak1)) }

        val vedtakSak2 = vedtakRepository.findBySak(sak2.saksnummer)

        assertThat(vedtakSak2).isEmpty()
    }
}

