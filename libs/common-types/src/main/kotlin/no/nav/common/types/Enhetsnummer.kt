package no.nav.common.types

import io.swagger.v3.oas.annotations.media.Schema

@Schema(type = "string")
@JvmInline
value class Enhetsnummer(val value: String) {
    override fun toString(): String {
        return value
    }
}
