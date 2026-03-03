package no.nav.helved

import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(type = "string", format = "uuid")
@JvmInline
value class UtbetalingUuid(val value: UUID) {
    override fun toString(): String {
        return value.toString()
    }
    companion object {
        fun random(): UtbetalingUuid = UtbetalingUuid(UUID.randomUUID())
    }
}