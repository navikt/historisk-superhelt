package no.nav.common.types

/** AktørId fra PDL */
@JvmInline
value class AktorId(val value: String) {
    override fun toString(): String {
        return value
    }
}
