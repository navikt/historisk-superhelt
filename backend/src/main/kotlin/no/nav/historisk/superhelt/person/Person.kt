package no.nav.historisk.superhelt.person

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.common.types.FolkeregisterIdent
import no.nav.pdl.AdressebeskyttelseGradering
import no.nav.person.Persondata
import no.nav.tilgangsmaskin.Avvisningskode
import no.nav.tilgangsmaskin.TilgangsmaskinClient
import java.time.LocalDate
import java.time.Period

data class PersonRequest(
    @field:Size(min = 11, max = 11, message = "Fødselsnummer må være 11 tegn")
    @field:Pattern(regexp = "[0-9]*", message = "Fødselsnummer må kun inneholde tall")
    val fnr: FolkeregisterIdent
)

data class Person(
    val navn: String,
    val fnr: FolkeregisterIdent,
    val maskertPersonident: MaskertPersonIdent,
    val doed: Boolean = false,
    val doedsfall: String? = null,
    val adressebeskyttelseGradering: AdressebeskyttelseGradering? = null,
    val harVerge: Boolean = false,
    val vergeInfo: VergeInfo? = null,
    val avvisningsKode: Avvisningskode? = null,
    val avvisningsBegrunnelse: String? = null,
    val foedselsdato: LocalDate? = null
) {
    val alder: Int?
        get() = foedselsdato?.let { Period.between(it, LocalDate.now()).years }
}

data class VergeInfo(
    val navn: String,
    val fnr: FolkeregisterIdent,
    val maskertPersonident: MaskertPersonIdent
)

fun Persondata.toDto(
    maskertPersonident: MaskertPersonIdent,
    tilgang: TilgangsmaskinClient.TilgangResult,
    vergeData: Persondata? = null
): Person {
    return Person(
        navn = this.navn,
        maskertPersonident = maskertPersonident,
        fnr = this.fnr,
        doed = this.doedsfall != null,
        doedsfall = this.doedsfall,
        adressebeskyttelseGradering = this.adressebeskyttelseGradering,
        harVerge = this.verge != null,
        vergeInfo = vergeData?.let {
            VergeInfo(
                navn = it.navn,
                fnr = it.fnr,
                maskertPersonident = it.fnr.toMaskertPersonIdent()
            )
        },
        avvisningsKode = tilgang.response?.title,
        avvisningsBegrunnelse = tilgang.response?.begrunnelse,
        foedselsdato = this.foedselsdato
    )
}
