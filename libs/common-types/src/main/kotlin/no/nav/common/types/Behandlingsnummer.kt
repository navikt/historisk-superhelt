package no.nav.common.types

import io.swagger.v3.oas.annotations.media.Schema

/** Unik id på en behandling.
 *
 * Må maks være 30 tegn for å kunne brukes i mot oppdrag
 *
 */
@Schema(type = "number")
@JvmInline
value class Behandlingsnummer(val value: Int) {

    override fun toString(): String {
        return value.toString()
    }
}