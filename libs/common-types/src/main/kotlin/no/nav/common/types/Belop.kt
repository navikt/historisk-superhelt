package no.nav.common.types

/** Beløp i hele kroner */
@JvmInline
value class Belop(val value: Int) {
    init {
        require(value >= 0) { "Beløpet kan ikke være negativt: $value" }
    }

    override fun toString(): String {
        return value.toString()
    }

}