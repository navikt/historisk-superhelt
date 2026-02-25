package no.nav.person

import no.nav.common.types.AktorId
import no.nav.common.types.FolkeregisterIdent
import no.nav.pdl.*
import org.slf4j.LoggerFactory
import org.springframework.web.client.HttpClientErrorException


class PdlPersondataParser {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun parsePdlResponse(response: HentPdlResponse): Persondata? {
        val errors = response.errors
        if (errors?.isNotEmpty() == true) {

            val feilmelding = errors
                .joinToString(separator = ", ") { "${it.extensions.code} \"${it.path}\" \"${it.message}\"" }
            logger.debug("Feilmeldinger fra PDL: $feilmelding")


            // Ikke tilgang av en eller annen grunn
            response.errors.finnFeil(PdlFeilkoder.UNAUTHORIZED)?.let {
                logger.warn("Ikke tilgang til person i PDL: ${it.message}")
                return parsePdlData(response.data).copy(harTilgang = false)
            }
            // Person ikke funnet
            errors.finnFeil(PdlFeilkoder.NOT_FOUND)?.let {
                logger.warn("Person ikke funnet i PDL: ${it.message}")
                return null
            }
            errors.finnFeil(PdlFeilkoder.SERVER_ERROR)?.let {
                throw HttpClientErrorException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Feil mot PDL: ${it.message}"
                )
            }
            throw RuntimeException("Uventet feil fra PDL: $feilmelding")
        }

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
            .let { AktorId(it.ident) }

        val hentPerson = data.hentPerson
        // Gir default navn om det ikke skulle være noe der. Betyr at personen ikke finnes eller det ikke er tilgan
        val navn = hentPerson?.navn?.firstOrNull() ?: Navn("***", "", "***")
        val adressebeskyttelse =
            hentPerson?.adressebeskyttelse?.firstOrNull()?.gradering
        val dodsDato = hentPerson?.doedsfall?.firstOrNull()?.doedsdato
        val fodselsDato = hentPerson?.foedselsdato?.firstOrNull()?.foedselsdato
        val verge = hentPerson?.vergemaalEllerFremtidsfullmakt?.firstOrNull()?.vergeEllerFullmektig

        return Persondata(
            navn = getSammensattNavnString(listOf(navn.fornavn, navn.mellomnavn, navn.etternavn)),
            fornavn = getSammensattNavnString(listOf(navn.fornavn, navn.mellomnavn)),
            etternavn = navn.etternavn,
            fnr = FolkeregisterIdent(aktivtFnr),
            aktorId = aktorId,
            alleFnr = alleFnr.map { FolkeregisterIdent(it) }.toSet(),
            doedsfall = dodsDato,
            foedselsdato = fodselsDato,
            adressebeskyttelseGradering = adressebeskyttelse,
            verge = verge,
            harTilgang = hentPerson != null
        )
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
