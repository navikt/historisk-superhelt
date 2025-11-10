package no.nav.historisk.superhelt.test

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("junit")
@SpringBootTest
@EmbeddedKafka
@Import(ExternalMockTestConfig::class)
annotation class MockedSpringBootTest()
