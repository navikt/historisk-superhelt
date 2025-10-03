package no.nav.historisk.superhelt.auth

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(GruppeConfigProperties::class)
class GruppeConfig {
    @Bean
    fun gruppeMapping(props: GruppeConfigProperties): Map<String, Gruppe> {
        return mapOf(
            props.les to Gruppe.LES,
            props.saksbehandler to Gruppe.SAKSBEHANDLER,
            props.attestant to Gruppe.ATTESTANT,
        )
    }
}

enum class Gruppe {
    LES,
    SAKSBEHANDLER,
    ATTESTANT,
}

@ConfigurationProperties(prefix = "app.gruppe")
class GruppeConfigProperties(
    val les: String,
    val saksbehandler: String,
    val attestant: String,

) {}