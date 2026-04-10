package no.nav.common.types

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Organisasjonsnummer fra Enhetsregisteret (9 siffer)
 */
@Schema(type = "string")
@JvmInline
value class Organisasjonsnummer(val value: String) {
    fun isValid(): Boolean {
        return value.length == 9 && value.all { it.isDigit() }
    }

    override fun toString(): String {
        return value
    }
}
