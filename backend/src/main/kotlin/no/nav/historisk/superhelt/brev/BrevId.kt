package no.nav.historisk.superhelt.brev

import java.util.*

@JvmInline
value class BrevId(val value: UUID) {
    override fun toString(): String = value.toString()

    companion object {
        fun random(): BrevId = BrevId(UUID.randomUUID())
    }
}
