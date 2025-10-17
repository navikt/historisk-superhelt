package no.nav.person

import no.nav.pdl.AdressebeskyttelseGradering

data class Persondata(
    val navn: String,
    val fornavn: String,
    val etternavn: String,
    val fnr: Fnr,
    val aktorId: AktorId,
    val alleFnr: Set<Fnr>,
    val doedsfall: Doedsfall,
    val adressebeskyttelseGradering: AdressebeskyttelseGradering? = null,
    val verge: Fnr?,
    val harTilgang: Boolean
)
@JvmInline
value class Fnr(val value: String){
    fun isValid(): Boolean {
        return value.length == 11 && value.all { it.isDigit() }
    }
}
typealias AktorId = String
typealias Doedsfall = String?


