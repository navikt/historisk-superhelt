package no.nav.common.types

@JvmInline
value class Fnr(val value: String) {
    fun isValid(): Boolean {
        return value.length == 11 && value.all { it.isDigit() }
    }
}