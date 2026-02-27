package no.nav.helved

import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

/**
 * Key for a transaction in the utbetaling system. This is not the same as the UtbetalingsId, which is the id of the utbetaling itself.
 */
@Schema(type = "string", format = "uuid")
@JvmInline
value class UtbetalingTransaksjonId(val value: UUID) {
    override fun toString(): String {
        return value.toString()
    }

    companion object {
        fun random(): UtbetalingTransaksjonId = UtbetalingTransaksjonId(UUID.randomUUID())
    }
}