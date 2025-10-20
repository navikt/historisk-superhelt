package no.nav.historisk.superhelt.infrastruktur.permission

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity

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

