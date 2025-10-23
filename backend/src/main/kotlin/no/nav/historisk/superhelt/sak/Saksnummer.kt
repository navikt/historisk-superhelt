package no.nav.historisk.superhelt.sak

@JvmInline
value class Saksnummer(val value: String) {
    constructor(id: Long) : this("SUPER-${id.toString().padStart(6, '0')}")

    val id: Long
        get() = value.split("-").getOrNull(1)?.toLongOrNull() ?: throw IllegalStateException("Ugyldig saksnummer: $value")

    override fun toString(): String {
        return value
    }
}

