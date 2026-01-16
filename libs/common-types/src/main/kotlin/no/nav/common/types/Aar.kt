package no.nav.common.types

import io.swagger.v3.oas.annotations.media.Schema

@Schema(type = "number")
@JvmInline
value class Aar(val value: Int) {
    fun isValid(): Boolean {
        return value in 1900..<3000
    }

    override fun toString(): String {
        return value.toString()
    }
}