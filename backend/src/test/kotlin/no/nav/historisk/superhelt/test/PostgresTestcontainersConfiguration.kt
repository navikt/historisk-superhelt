package no.nav.historisk.superhelt.test

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.test.context.DynamicPropertyRegistrar
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.util.UUID

@TestConfiguration(proxyBeanMethods = false)
class PostgresTestcontainersConfiguration {

    @Bean
    @ServiceConnection
    @Profile("!sandkasse")
    fun postgresContainer(): PostgreSQLContainer {
        return PostgreSQLContainer(DockerImageName.parse("postgres:18-alpine"))
    }

    @Bean
    @Profile("sandkasse")
    fun sandkasseDatabaseProperties(): DynamicPropertyRegistrar {
        val schema = "test_${UUID.randomUUID().toString().replace("-", "")}"
        return DynamicPropertyRegistrar { registry ->
            registry.add("spring.flyway.default-schema") { schema }
            registry.add("spring.flyway.schemas") { schema }
            registry.add("spring.datasource.hikari.schema") { schema }
        }
    }
}
