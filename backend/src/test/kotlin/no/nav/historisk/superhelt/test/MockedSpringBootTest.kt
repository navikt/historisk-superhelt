package no.nav.historisk.superhelt.test

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("junit")
@SpringBootTest
@EmbeddedKafka(kraft = true, topics = ["\${app.utbetaling.status-topic}", "\${app.utbetaling.utbetaling-topic}"])
@Import(ExternalMockTestConfig::class, PostgresTestcontainersConfiguration::class)
annotation class MockedSpringBootTest()
