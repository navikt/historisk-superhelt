package no.nav.historisk.superhelt.infrastruktur.authentication

enum class Role(private vararg val _permissions: Permission) {
    LES(Permission.READ),
    SAKSBEHANDLER(Permission.READ, Permission.WRITE),
    ATTESTANT(Permission.READ, Permission.WRITE),
    DRIFT()
    ;

    val permissions: List<Permission>
        get() = _permissions.toList()
}
