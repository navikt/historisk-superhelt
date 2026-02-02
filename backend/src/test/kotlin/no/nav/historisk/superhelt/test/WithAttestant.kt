package no.nav.historisk.superhelt.test

import no.nav.historisk.superhelt.infrastruktur.authentication.Permission
import no.nav.historisk.superhelt.infrastruktur.authentication.Role
import org.springframework.core.annotation.AliasFor

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@WithMockJwtAuth(
    roles = [Role.ATTESTANT],
    permissions = [Permission.READ, Permission.WRITE]
)
annotation class WithAttestant(
    @get:AliasFor(annotation = WithMockJwtAuth::class, attribute = "navIdent")
    val navIdent: String = "A999999",
    @get:AliasFor(annotation = WithMockJwtAuth::class, attribute = "name")
    val name: String = "Test Attestant"
)