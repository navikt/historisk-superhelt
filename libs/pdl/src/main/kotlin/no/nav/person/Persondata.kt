package no.nav.person

import no.nav.common.types.AktorId
import no.nav.common.types.FolkeregisterIdent
import no.nav.pdl.AdressebeskyttelseGradering
import no.nav.pdl.VergeEllerFullmektig
import java.time.LocalDate

data class Persondata(
    val navn: String,
    val fornavn: String,
    val etternavn: String,
    val fnr: FolkeregisterIdent,
    val aktorId: AktorId,
    val alleFnr: Set<FolkeregisterIdent>,
    val doedsfall: Doedsfall,
    val foedselsdato: LocalDate?,
    val adressebeskyttelseGradering: AdressebeskyttelseGradering? = null,
    val verge: VergeEllerFullmektig?,
    val harTilgang: Boolean
)

typealias Doedsfall = String?
