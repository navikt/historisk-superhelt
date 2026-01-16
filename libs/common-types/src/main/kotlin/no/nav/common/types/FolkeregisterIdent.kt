package no.nav.common.types

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Fødselsnummer eller D-nummer fra Folkeregisteret
 */
@Schema(type = "string")
@JvmInline
value class FolkeregisterIdent(val value: String) {
    fun isValid(): Boolean {
        return value.length == 11 && value.all { it.isDigit() }
    }

    override fun toString(): String {
        return value
    }
}