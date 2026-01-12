package no.nav.person

import no.nav.common.types.FolkeregisterIdent
import no.nav.pdl.AdressebeskyttelseGradering

data class Persondata(
    val navn: String,
    val fornavn: String,
    val etternavn: String,
    val fnr: FolkeregisterIdent,
    val aktorId: AktorId,
    val alleFnr: Set<FolkeregisterIdent>,
    val doedsfall: Doedsfall,
    val foedselsdato: Foedselsdato,
    val adressebeskyttelseGradering: AdressebeskyttelseGradering? = null,
    val verge: FolkeregisterIdent?,
    val harTilgang: Boolean
)

typealias AktorId = String
typealias Doedsfall = String?
typealias Foedselsdato = String?
