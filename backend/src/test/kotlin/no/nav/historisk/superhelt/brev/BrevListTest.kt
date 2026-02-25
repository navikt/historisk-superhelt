package no.nav.historisk.superhelt.brev

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class BrevListTest {

    @Test
    fun `skal finne gjeldende brev for sak`() {
        val now = Instant.now()
        val brevList = listOf(
            BrevTestdata.vedtaksbrevBruker().copy(tittel = "Brev1", opprettetTidspunkt = now),
            BrevTestdata.vedtaksbrevBruker().copy(tittel = "Brev2", opprettetTidspunkt = now.minusSeconds(10)),
            BrevTestdata.vedtaksbrevBruker().copy(tittel = "Brev3", opprettetTidspunkt = now.plusSeconds(20)),
        )

        val gjeldendeBrev = brevList.finnGjeldendeBrev(BrevType.VEDTAKSBREV, BrevMottaker.BRUKER)

        assertThat(gjeldendeBrev).isNotNull
        assertThat(gjeldendeBrev?.tittel).isEqualTo("Brev3")
    }

}