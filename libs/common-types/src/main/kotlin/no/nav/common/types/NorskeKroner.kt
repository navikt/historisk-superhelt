package no.nav.common.types

/** Beløp i hele kroner */
@JvmInline
value class NorskeKroner(val value: Int) {
//    init {
//        require(value >= 0) { "Beløp kan ikke være negativt: $value" }
//    }
}