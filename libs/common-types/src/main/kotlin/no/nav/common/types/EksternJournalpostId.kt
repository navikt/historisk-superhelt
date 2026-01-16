package no.nav.common.types

import io.swagger.v3.oas.annotations.media.Schema

/** Id på en journalpost i NAVs journalsystem */
@Schema(type = "string")
@JvmInline
value class EksternJournalpostId(val value: String) {
    override fun toString(): String {
        return value
    }
}