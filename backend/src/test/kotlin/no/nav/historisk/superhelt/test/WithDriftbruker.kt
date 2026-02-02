package no.nav.historisk.superhelt.test

import no.nav.historisk.superhelt.infrastruktur.Role
import org.springframework.core.annotation.AliasFor

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@WithMockJwtAuth(
    roles = [Role.DRIFT],
    permissions = []
)
annotation class WithDriftbruker(
    @get:AliasFor(annotation = WithMockJwtAuth::class, attribute = "navIdent")
    val navIdent: String = "D999999",
    @get:AliasFor(annotation = WithMockJwtAuth::class, attribute = "name")
    val name: String = "Durek the Drifter"
)