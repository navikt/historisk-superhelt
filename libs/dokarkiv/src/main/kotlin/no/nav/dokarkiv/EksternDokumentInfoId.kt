package no.nav.dokarkiv


/** Id på ett dokument i en  journalpost i NAVs journalsystem */
@JvmInline
value class EksternDokumentInfoId(val value: String) {
    override fun toString(): String {
        return value
    }
}