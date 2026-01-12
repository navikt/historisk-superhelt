package no.nav.pdl

/** Domene for GraphQL-respons fra PDL  generert fra hentPersonOgIdenter.graphql*/

data class HentPdlResponse(
    val data: PdlData?,
    val errors: List<PdlError>?
)

data class PdlData(
    val hentPerson: Person?,
    val hentIdenter: Identliste?
)

data class Person(
    val navn: List<Navn>?,
    val doedsfall: List<Doedsfall>?,
    val foedselsdato: List<Foedselsdato>?,
    val adressebeskyttelse: List<Adressebeskyttelse>?,
    val vergemaalEllerFremtidsfullmakt: List<VergemaalEllerFremtidsfullmakt>?
)

data class Navn(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String
)

data class Doedsfall(
    val doedsdato: String?
)

data class Foedselsdato(
    val foedselsdato: String?
)

data class Adressebeskyttelse(
    val gradering: AdressebeskyttelseGradering
)

enum class AdressebeskyttelseGradering {
    FORTROLIG,
    STRENGT_FORTROLIG,
    STRENGT_FORTROLIG_UTLAND,
    UGRADERT
}

data class VergemaalEllerFremtidsfullmakt(
    val vergeEllerFullmektig: VergeEllerFullmektig
)

data class VergeEllerFullmektig(
    val motpartsPersonident: String?
)

data class Identliste(
    val identer: List<IdentInformasjon>
)

data class IdentInformasjon(
    val ident: String,
    val gruppe: IdentGruppe,
    val historisk: Boolean
)

enum class IdentGruppe {
    AKTORID,
    FOLKEREGISTERIDENT,
    NPID
}

data class PdlError(
    val message: String,
    val locations: List<PdlErrorLocation>,
    val path: List<String>?,
    val extensions: PdlErrorExtension
)

data class PdlErrorLocation(
    val line: Int?, val column: Int?
)

data class PdlErrorExtension(
    val code: String?, val classification: String
)
