package no.nav.historisk.superhelt.infrastruktur.authentication

import no.nav.common.consts.FellesKodeverkTema
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@EnableConfigurationProperties(GruppeConfigProperties::class)
class GruppeRoleConfig {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun gruppeRoleMapping(props: GruppeConfigProperties): GruppeMapping {
        val gruppeRoller = mapOf(
            props.les to Role.LES,
            props.saksbehandler to Role.SAKSBEHANDLER,
            props.attestant to Role.ATTESTANT,
            props.drift to Role.DRIFT,
        )
        val gruppeTema = mapOf(
            props.temaHel to FellesKodeverkTema.HEL,
            props.temaHje to FellesKodeverkTema.HJE,
        )
        logger.debug("Mapping roller: {}, tema: {}", gruppeRoller, gruppeTema)
        return GruppeMapping(roller = gruppeRoller, tema = gruppeTema)
    }
}

@ConfigurationProperties(prefix = "app.gruppe")
class GruppeConfigProperties(
    val les: String,
    val saksbehandler: String,
    val attestant: String,
    val drift: String,
    val temaHel: String,
    val temaHje: String,
    )
