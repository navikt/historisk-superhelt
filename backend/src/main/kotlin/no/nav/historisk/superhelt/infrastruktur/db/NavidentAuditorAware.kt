package no.nav.historisk.superhelt.infrastruktur.db

import no.nav.historisk.superhelt.infrastruktur.getCurrentNavIdent
import org.springframework.data.domain.AuditorAware
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class NavidentAuditorAware: AuditorAware<String> {
    override fun getCurrentAuditor(): Optional<String> {
        val value = getCurrentNavIdent()
        return Optional.ofNullable(value)
    }
}