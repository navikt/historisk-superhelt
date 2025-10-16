package no.nav.historisk.superhelt.auth.permission

import no.nav.historisk.superhelt.person.TilgangsmaskinTestData
import no.nav.historisk.superhelt.person.tilgangsmaskin.TilgangsmaskinService
import no.nav.tilgangsmaskin.TilgangsmaskinClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.stereotype.Service
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean

@ActiveProfiles("junit")
@SpringBootTest()
//@ExtendWith(SpringExtension::class)
//@Import(MethodSecurityTestConfig::class)
@Import(TilgangsmaskinAuthLogic2Test.TestService::class)
class TilgangsmaskinAuthLogic2Test {

    @MockitoBean
    private lateinit var tilgangsmaskinService: TilgangsmaskinService

    @Autowired
    private lateinit var testService: TestService


    @Test
    fun `PreAuthorize skal gi tilgang tilgangsmaskin godkjenner`() {
        val fnr = "12345678901"
        whenever(tilgangsmaskinService.sjekkKomplettTilgang(fnr))
            .thenReturn(TilgangsmaskinClient.TilgangResult(true))
        testService.testPreAuthorize(fnr)
        verify(tilgangsmaskinService).sjekkKomplettTilgang(fnr)
    }

    @Test
    fun `PreAuthorize skal gi exception om tilgangsmaskin gir feil`() {
        val fnr = "12345678901"
        whenever(tilgangsmaskinService.sjekkKomplettTilgang(fnr))
            .thenReturn(
                TilgangsmaskinClient.TilgangResult(
                    false, TilgangsmaskinTestData.problemDetailResponse
                        .copy(begrunnelse = "begrunnelse123")
                )
            )

        val exception = Assertions.assertThrows(AuthorizationDeniedException::class.java) {
            testService.testPreAuthorize(fnr)
        }
//        assertThat(exception.message).contains("Mangler tilgang til bruker")
        assertThat(exception.isGranted).isFalse
        assertThat(exception.authorizationResult.toString()).contains("begrunnelse123")

        verify(tilgangsmaskinService).sjekkKomplettTilgang(fnr)
    }

    @Test
    fun `PostAuthorize skal gi ok svar ved tilgang`() {
        val fnr = "12345678901"
        whenever(tilgangsmaskinService.sjekkKomplettTilgang(fnr.reversed()))
            .thenReturn(TilgangsmaskinClient.TilgangResult(true))

        testService.testPostAuthorize(fnr)

        verify(tilgangsmaskinService).sjekkKomplettTilgang(fnr.reversed())
    }

    @Test
    fun `PostAuthorize skal gi exception om tilgangsmaskin gir feil`() {
        val fnr = "12345678901"
        whenever(tilgangsmaskinService.sjekkKomplettTilgang(fnr.reversed()))
            .thenReturn(
                TilgangsmaskinClient.TilgangResult(
                    false, TilgangsmaskinTestData.problemDetailResponse.copy(
                        begrunnelse = "begrunnelse345"
                    )
                )
            )

        val exception = Assertions.assertThrows(AuthorizationDeniedException::class.java) {
            testService.testPostAuthorize(fnr)
        }
        assertThat(exception.isGranted).isFalse
        assertThat(exception.authorizationResult.toString()).contains("begrunnelse345")

        verify(tilgangsmaskinService).sjekkKomplettTilgang(fnr.reversed())
    }

    @Test
    fun `PostFilter skal kalle tilgangsmaskin for hvert element`() {
        val fnr = "12345678901"
        whenever(tilgangsmaskinService.sjekkKomplettTilgang(any()))
            .thenReturn(TilgangsmaskinClient.TilgangResult(true))

        testService.testPostFilter().also {
            assertThat(it).containsExactlyInAnyOrder("111", "222", "333")
        }

        verify(tilgangsmaskinService, times(3)).sjekkKomplettTilgang(any())
    }


    //TODO fiks slik at denne fungerer
    @Disabled("Virker ikke med throws")
    @Test
    fun `PostFilter skal  filtere ut elementer som ikke er lov`() {
        val fnr = "12345678901"
        whenever(tilgangsmaskinService.sjekkKomplettTilgang(any()))
            .thenReturn(TilgangsmaskinClient.TilgangResult(true))
        whenever(tilgangsmaskinService.sjekkKomplettTilgang("222"))
            .thenReturn(TilgangsmaskinClient.TilgangResult(false))

        testService.testPostFilter().also {
            assertThat(it).containsExactlyInAnyOrder("111",  "333")
        }

        verify(tilgangsmaskinService, times(3)).sjekkKomplettTilgang(any())
    }


    @Service
    open class TestService {

        @PreAuthorize("@tilgangsmaskin.harTilgang(#fnr)")
        fun testPreAuthorize(fnr: String): String {
            return fnr
        }

        @PostFilter("@tilgangsmaskin.harTilgang(filterObject)")
        fun testPostFilter(): List<String> {
            return listOf("111", "222", "333")
        }

        @PostAuthorize("@tilgangsmaskin.harTilgang(returnObject)")
        fun testPostAuthorize(fnr: String): String {
            return fnr.reversed()
        }
    }


}