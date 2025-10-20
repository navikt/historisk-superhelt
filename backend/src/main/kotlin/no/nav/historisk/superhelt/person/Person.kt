package no.nav.historisk.superhelt.person

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.pdl.AdressebeskyttelseGradering
import no.nav.person.Fnr
import no.nav.person.Persondata
import no.nav.tilgangsmaskin.Avvisningskode
import no.nav.tilgangsmaskin.TilgangsmaskinClient

data class PersonRequest(
    @field:Size(min = 11, max = 11)
    @field:Pattern(regexp = "[0-9]*")
    val fnr: Fnr
)

data class Person(
    val navn: String,
    val fnr: Fnr,
    val maskertPersonident: MaskertPersonIdent,
    val doed: Boolean = false,
    val adressebeskyttelseGradering: AdressebeskyttelseGradering? = null,
    val verge: Boolean = false,
    val avvisningsKode: Avvisningskode? = null,
    val avvisningsBegrunnelse: String? = null
)

fun Persondata.toDto(maskertPersonident: MaskertPersonIdent, tilgang: TilgangsmaskinClient.TilgangResult) = Person(
    navn = this.navn,
    maskertPersonident = maskertPersonident,
    fnr = this.fnr,
    doed = this.doedsfall != null,
    adressebeskyttelseGradering = this.adressebeskyttelseGradering,
    verge = this.verge != null,
    avvisningsKode = tilgang.response?.title,
    avvisningsBegrunnelse = tilgang.response?.begrunnelse

)

