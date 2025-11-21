package no.nav.common.types

/** Unik id på en behandling.
 *
 * Må maks være 30 tegn for å kunne brukes i mot oppdrag
 *
 */
@JvmInline
value class Behandlingsnummer(val value: String) {
    constructor(prefix: String, id: Int) : this("${prefix}-${id}")

    val id: Int
        get() = value.split("-").getOrNull(1)?.toIntOrNull()
            ?: throw IllegalStateException("Ugyldig saksnummer: $value")

    override fun toString(): String {
        return value
    }
}