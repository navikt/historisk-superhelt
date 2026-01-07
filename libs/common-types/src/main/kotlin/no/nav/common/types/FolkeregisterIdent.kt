package no.nav.common.types

/**
 * Fødselsnummer eller D-nummer fra Folkeregisteret
 */
@JvmInline
value class FolkeregisterIdent(val value: String) {
    fun isValid(): Boolean {
        return value.length == 11 && value.all { it.isDigit() }
    }

    override fun toString(): String {
        return value
    }
}