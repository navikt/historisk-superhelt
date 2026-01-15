package no.nav.historisk.superhelt.test

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("junit")
@SpringBootTest
@Import(ExternalMockTestConfig::class, PostgresTestcontainersConfiguration::class, KafkaTestcontainersConfiguration::class)
annotation class MockedSpringBootTest()
