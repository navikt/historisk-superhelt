package no.nav.common.types

import io.swagger.v3.oas.annotations.media.Schema

/** Beløp i hele kroner */
@JvmInline
@Schema(type = "number")
value class Belop(val value: Int) {
    init {
        require(value >= 0) { "Beløpet kan ikke være negativt: $value" }
    }

    override fun toString(): String {
        return value.toString()
    }

}