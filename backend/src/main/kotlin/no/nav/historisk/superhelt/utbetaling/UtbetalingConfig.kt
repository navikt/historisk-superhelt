package no.nav.historisk.superhelt.utbetaling

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(UtbetalingConfigProperties::class)
class UtbetalingConfig {}


@ConfigurationProperties(prefix = "app.utbetaling")
class UtbetalingConfigProperties(
    val utbetalingTopic: String,
    val statusTopic: String,
    ) {}