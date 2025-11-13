package no.nav.historisk.superhelt.test

import no.nav.historisk.superhelt.infrastruktur.Permission
import no.nav.historisk.superhelt.infrastruktur.Role
import org.springframework.core.annotation.AliasFor

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@WithMockJwtAuth(
    roles = [Role.SAKSBEHANDLER],
    permissions = [Permission.READ, Permission.WRITE]
)
annotation class WithSaksbehandler(
    @get:AliasFor(annotation = WithMockJwtAuth::class, attribute = "navIdent")
    val navIdent: String = "Z999999",
    @get:AliasFor(annotation = WithMockJwtAuth::class, attribute = "name")
    val name: String = "Test Saksbehandler"
)