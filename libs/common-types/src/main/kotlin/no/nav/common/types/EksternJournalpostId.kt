package no.nav.common.types

/** Id på en journalpost i NAVs journalsystem */
@JvmInline
value class EksternJournalpostId(val value: String) {
    override fun toString(): String {
        return value
    }
}