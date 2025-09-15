package no.nav.person

import no.nav.pdl.AdressebeskyttelseGradering
import no.nav.pdl.HentPdlResponse
import no.nav.pdl.IdentGruppe
import no.nav.pdl.PdlData
import no.nav.pdl.PdlError
import no.nav.pdl.PdlErrorList
import no.nav.pdl.PdlFeilkoder
import org.slf4j.LoggerFactory
import org.springframework.web.client.HttpClientErrorException
import kotlin.collections.joinToString
import kotlin.jvm.java


class PdlPersondataParser {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun parsePdlResponse(response: HentPdlResponse): Persondata {
        handlePdlErrors(response.errors)
        return parsePdlData(response.data)
    }

    private fun parsePdlData(data: PdlData?): Persondata {
        val identer = data?.hentIdenter?.identer
        require(!identer.isNullOrEmpty()) { "Fikk ingen identer fra PDL" }


        val alleFnrIdenter = identer.filter { it.gruppe == IdentGruppe.FOLKEREGISTERIDENT }
        val aktivtFnr = alleFnrIdenter.first { !it.historisk }.ident
        val alleFnr = alleFnrIdenter.map { it.ident }

        val aktorId = identer.filter { it.gruppe == IdentGruppe.AKTORID }
            .first { !it.historisk }
            .ident

        val hentPerson = data.hentPerson
        require(hentPerson != null) { "Forventet å finne persondata" }

        val navn = hentPerson.navn?.firstOrNull()
        require(navn != null) { "Forventet å finne navn på person" }

        val adressebeskyttelse =
            hentPerson.adressebeskyttelse?.firstOrNull()?.gradering ?: AdressebeskyttelseGradering.UGRADERT
        val dodsDato = hentPerson.doedsfall?.firstOrNull()?.doedsdato
        val verge = hentPerson.vergemaalEllerFremtidsfullmakt?.firstOrNull()?.vergeEllerFullmektig?.motpartsPersonident

        return Persondata(
            navn = getSammensattNavnString(listOf(navn.fornavn, navn.mellomnavn, navn.etternavn)),
            fornavn = getSammensattNavnString(listOf(navn.fornavn, navn.mellomnavn)),
            etternavn = navn.etternavn,
            fnr = aktivtFnr,
            aktorId = aktorId,
            alleFnr = alleFnr.toSet(),
            doedsfall = dodsDato,
            adressebeskyttelseGradering = adressebeskyttelse,
            verge = verge
        )
    }


    /** Oversetter PDL-feilkoder til rest api med passende HTTP-status. */
    private fun handlePdlErrors(errors: PdlErrorList?) {
        if (errors?.isNotEmpty() == true) {

            val feilmelding = errors
                .joinToString(separator = ", ") { "${it.extensions.code} \"${it.path}\" \"${it.message}\"" }
            logger.info("Feilmeldinger fra PDL: $feilmelding")

            errors.finnFeil(PdlFeilkoder.UNAUTHORIZED)?.let {
                throw HttpClientErrorException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "Ikke tilgang til person i PDL: ${it.message}"
                )
            }
            errors.finnFeil(PdlFeilkoder.NOT_FOUND)?.let {
                throw HttpClientErrorException(
                    org.springframework.http.HttpStatus.NOT_FOUND,
                    "Person ikke funnet: ${it.message}"
                )
            }
            errors.finnFeil(PdlFeilkoder.SERVER_ERROR)?.let {
                throw HttpClientErrorException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Feil mot PDL: ${it.message}"
                )
            }
            require(errors.isEmpty()) { "Fikk feilmeldinger fra PDL: $feilmelding" }
        }
    }

    private fun PdlErrorList.finnFeil(pdlFeilkode: String): PdlError? {
        return this.find { it.extensions.code == pdlFeilkode }
    }


    private fun getSammensattNavnString(navneliste: List<String?>): String {
        return navneliste
            .filterNotNull()
            .joinToString(separator = " ")
    }
}