package no.nav.person

import no.nav.common.types.Fnr
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

typealias AktorId = String
typealias Doedsfall = String?


