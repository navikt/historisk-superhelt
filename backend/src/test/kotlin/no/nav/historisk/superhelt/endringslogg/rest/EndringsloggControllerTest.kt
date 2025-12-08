package no.nav.historisk.superhelt.endringslogg.rest

import no.nav.common.types.Fnr
import no.nav.historisk.superhelt.endringslogg.EndringsloggLinje
import no.nav.historisk.superhelt.endringslogg.EndringsloggService
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.endringslogg.EndringsloggType.OPPRETTET_SAK
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.sak.Saksnummer
import no.nav.historisk.superhelt.sak.db.SakJpaEntity
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.historisk.superhelt.test.withMockedUser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.assertj.MockMvcTester

@MockedSpringBootTest
@AutoConfigureMockMvc
class EndringsloggControllerTest {
    @Autowired
    private lateinit var repository: SakRepository
    @Autowired
    private lateinit var endringsloggService: EndringsloggService
    @Autowired
    private lateinit var mockMvc: MockMvcTester


    @WithSaksbehandler(navIdent = "s12345")
    @Test
    fun `finn changelog `() {
        val fnr = Fnr("12345678901")
        val sak = lagreNySak(SakTestData.sakEntityMinimum(fnr))
        endringsloggService.logChange(
            saksnummer = sak.saksnummer,
            endringsType = OPPRETTET_SAK,
            endring = "Sak opprettet"
        )
        endringsloggService.logChange(
            saksnummer = sak.saksnummer,
            endringsType = EndringsloggType.FERDIGSTILT_SAK,
            endring = "Sak ferdigstilt"
        )


        assertThat(hentSakChangelog(sak.saksnummer))
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .convertTo(Array<EndringsloggLinje>::class.java)
            .satisfies({
                assertThat(it).hasSize(2)
                assertThat(it[0].type).isEqualTo(OPPRETTET_SAK)
                assertThat(it[0].endretAv.value).isEqualTo("s12345")
                assertThat(it[0].endretTidspunkt).isNotNull
                assertThat(it[0].endring).isNotNull
                assertThat(it[1].type).isEqualTo(EndringsloggType.FERDIGSTILT_SAK)
                assertThat(it[1].endretAv.value).isEqualTo("s12345")
            })

    }

    private fun hentSakChangelog(saksnummer: Saksnummer?): MockMvcTester.MockMvcRequestBuilder =
        mockMvc.get().uri("/api/sak/{saksnummer}/endringslogg", saksnummer)

    fun lagreNySak(sak: SakJpaEntity = SakTestData.sakEntityMinimum()): Sak {
        return withMockedUser {
            repository.save(sak)
        }
    }


}