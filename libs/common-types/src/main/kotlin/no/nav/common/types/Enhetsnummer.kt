package no.nav.common.types

@JvmInline
value class Enhetsnummer(val value: String) {
    override fun toString(): String {
        return value
    }
}
