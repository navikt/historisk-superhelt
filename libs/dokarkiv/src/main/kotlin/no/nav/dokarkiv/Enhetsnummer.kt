package no.nav.dokarkiv

@JvmInline
value class Enhetsnummer(val value: String) {
    override fun toString(): String {
        return value
    }
}