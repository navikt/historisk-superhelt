package no.nav.common.types

import io.swagger.v3.oas.annotations.media.Schema

/** AktørId fra PDL */
@Schema(type = "string")
@JvmInline
value class AktorId(val value: String) {
    override fun toString(): String {
        return value
    }
}
