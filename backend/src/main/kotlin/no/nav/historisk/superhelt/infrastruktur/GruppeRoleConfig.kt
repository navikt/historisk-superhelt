package no.nav.historisk.superhelt.infrastruktur

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
    fun gruppeRoleMapping(props: GruppeConfigProperties): Map<String, Role> {
        val gruppeRoller = mapOf(
            props.les to Role.LES,
            props.saksbehandler to Role.SAKSBEHANDLER,
            props.attestant to Role.ATTESTANT,
        )
        logger.debug("GruppeRoleMapping: {}", gruppeRoller)
        return gruppeRoller
    }
}

enum class Permission {
    READ,
    WRITE,
    DELETE,
}

enum class Role(private vararg val _permissions: Permission) {
    LES(Permission.READ),
    SAKSBEHANDLER(Permission.READ, Permission.WRITE),
    ATTESTANT(Permission.READ);

    val permissions: List<Permission>
        get() = _permissions.toList()
}

@ConfigurationProperties(prefix = "app.gruppe")
class GruppeConfigProperties(
    val les: String,
    val saksbehandler: String,
    val attestant: String,

) {}