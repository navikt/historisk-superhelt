package no.nav.common.types

@JvmInline
value class NavIdent(val value: String) {
    override fun toString(): String {
        return value
    }
}