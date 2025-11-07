package no.nav.historisk.superhelt.config

import jakarta.persistence.EntityManagerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.orm.jpa.JpaTransactionManager


@Configuration
class TransactionConfig {
    // Må sette denne for å unngå å ha to med kafka og jpa i samme app
    @Bean("transactionManager")
    @Primary
    fun transactionManager(entityManagerFactory: EntityManagerFactory): JpaTransactionManager {
        return JpaTransactionManager(entityManagerFactory)
    }
}