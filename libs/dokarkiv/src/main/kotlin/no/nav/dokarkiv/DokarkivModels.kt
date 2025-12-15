package no.nav.dokarkiv

import no.nav.common.types.Saksnummer

data class Bruker(
    val id: String,
    val idType: BrukerIdType,
)

data class Sak(
    val sakstype: Sakstype = Sakstype.FAGSAK,
    val fagsakId: Saksnummer,
    val fagsaksystem: String = "SUPERHELT"
)

data class AvsenderMottaker(
    val id: String? = null,
    val idType: AvsenderMottakerIdType? = null,
    val navn: String? = null,
)

data class Dokument(
    val tittel: String,
    val brevkode: String,
    val dokumentvarianter: List<DokumentVariant>,
)

data class DokumentVariant(
    val filtype: Filtype,
    val fysiskDokument: ByteArray,
    val variantformat: Variantformat,
)


data class DokumentInfo(
    val brevkode: String? = null,
    val dokumentInfoId: String? = null,
    val tittel: String? = null,
)
