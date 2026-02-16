package no.nav.historisk.superhelt.brev

import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(type = "string")
@JvmInline
value class BrevId(val value: UUID) {
    override fun toString(): String = value.toString()

    companion object {
        fun random(): BrevId = BrevId(UUID.randomUUID())
    }
}
