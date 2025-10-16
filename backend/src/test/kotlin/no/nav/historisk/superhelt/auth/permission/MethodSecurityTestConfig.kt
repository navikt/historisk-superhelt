package no.nav.historisk.superhelt.auth.permission

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.stereotype.Service
import org.springframework.test.context.ContextConfiguration

@TestConfiguration
@EnableMethodSecurity(prePostEnabled = true)
@Import (TilgangsmaskinAuthLogic::class)
//@ContextConfiguration
class MethodSecurityTestConfig {

//    @Bean
//    fun testService(): TestService {
//        return TestService()
//    }

}

