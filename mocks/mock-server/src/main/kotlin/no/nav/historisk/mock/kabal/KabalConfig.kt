package no.nav.historisk.mock.kabal

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(KabalConfigProperties::class)
class KabalConfig(private val properties: KabalConfigProperties) {

    @Bean
    fun kabalBehandlingEventTopic(): NewTopic {
        return NewTopic(properties.behandlingEventTopic, 1, 1)
    }
}

@ConfigurationProperties(prefix = "app.kabal")
class KabalConfigProperties(val behandlingEventTopic: String)

