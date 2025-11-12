package no.nav.historisk.mock.helved

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(HelvedConfigProperties::class)
class UtbetalingConfig(private val config: HelvedConfigProperties) {

    @Bean
    fun utbetalingTopic(): NewTopic {
        return NewTopic(config.utbetalingTopic, 1, 1)
    }

    @Bean
    fun statusTopic(): NewTopic {
        return NewTopic(config.statusTopic, 1, 1)
    }

}

@ConfigurationProperties(prefix = "app.helved")
class HelvedConfigProperties(val utbetalingTopic: String, val statusTopic: String) {}
