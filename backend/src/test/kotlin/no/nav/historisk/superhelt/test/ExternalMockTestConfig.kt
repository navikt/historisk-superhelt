package no.nav.historisk.superhelt.test

import no.nav.historisk.superhelt.person.tilgangsmaskin.TilgangsmaskinService
import no.nav.pdl.PdlClient
import no.nav.tilgangsmaskin.TilgangsmaskinClient
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class ExternalMockTestConfig {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @Primary
    @Bean
    fun tilgangsmaskinServiceMock(): TilgangsmaskinService {
        val mock = mock<TilgangsmaskinService>()
        logger.warn("Bruker mock av tilgangsmaskin")
        mock.stub {
            on { sjekkKomplettTilgang(any()) } doReturn TilgangsmaskinClient.TilgangResult(
                harTilgang = true,
                response = null
            )
        }
        return mock
    }

    @Primary
    @Bean
    fun pdlClientMock(): PdlClient {
        return mock()
    }
}