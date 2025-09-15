package no.nav.historisk.superhelt.person

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.pdl.AdressebeskyttelseGradering
import no.nav.person.Fnr
import no.nav.person.Persondata

data class PersonRequest(
    @field:Size(min = 11, max = 11)
    @field:Pattern(regexp = "[0-9]*")
    val fnr: Fnr
)

data class Person(
    val navn: String,
    val fnr: Fnr,
    val maskertPersonident: String,
    val doed: Boolean = false,
    val adressebeskyttelseGradering: AdressebeskyttelseGradering = AdressebeskyttelseGradering.UGRADERT,
    val verge: Boolean = false,
)

fun Persondata.toDto(maskertPersonident: String) = Person(
    navn = this.navn,
    maskertPersonident = maskertPersonident,
    fnr = this.fnr,
    doed = this.doedsfall != null,
    adressebeskyttelseGradering = this.adressebeskyttelseGradering,
    verge = this.verge != null
)

