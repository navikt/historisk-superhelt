package no.nav.oppgave

import tools.jackson.core.type.TypeReference
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.json.JsonMapper

/**
 * Validator for lovlige kombinasjoner av behandlingstema og behandlingstype i henhold til
 * oppgave-kodeverk for HEL og HJE.
 *
 * Kombinasjonene lastes fra kodeverk-filer i classpath (kodeverk/HEL_kodeverk.json og HJE_kodeverk.json).
 * Kilde: /api/v1/kodeverk/oppgavetype/{tema}
 */
object OppgaveKodeverkValidator {

    private data class GjelderverdiJson(
        val behandlingstema: String?,
        val behandlingstype: String?,
    )

    private data class KodeverkJson(
        val gjelderverdier: List<GjelderverdiJson>,
    )

    private val gyldigeKombinasjoner: Set<Pair<Behandlingstema?, Behandlingstype?>> by lazy {
        val mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build()

        val gjelderverdier = mutableListOf<GjelderverdiJson>()
        for (path in listOf("kodeverk/HEL_kodeverk.json", "kodeverk/HJE_kodeverk.json")) {
            val kodeverk: List<KodeverkJson> = OppgaveKodeverkValidator::class.java.classLoader
                .getResourceAsStream(path)
                ?.use { stream -> mapper.readValue(stream, object : TypeReference<List<KodeverkJson>>() {}) }
                ?: error("Fant ikke kodeverk-fil: $path")
            kodeverk.forEach { gjelderverdier.addAll(it.gjelderverdier) }
        }

        gjelderverdier.map { gjelderverdi ->
            val tema: Behandlingstema? = gjelderverdi.behandlingstema
                ?.let { kode -> Behandlingstema.entries.find { entry -> entry.kode == kode } }
            val type: Behandlingstype? = gjelderverdi.behandlingstype
                ?.let { kode -> Behandlingstype.entries.find { entry -> entry.kode == kode } }
            tema to type
        }.toSet()
    }

    fun erGyldig(tema: Behandlingstema?, type: Behandlingstype?): Boolean =
        (tema to type) in gyldigeKombinasjoner

    fun erGyldig(temaKode: String?, typeKode: String?): Boolean {
        val tema = temaKode?.let { kode -> Behandlingstema.entries.find { entry -> entry.kode == kode } }
        val type = typeKode?.let { kode -> Behandlingstype.entries.find { entry -> entry.kode == kode } }
        if (temaKode != null && tema == null) return false
        if (typeKode != null && type == null) return false
        return erGyldig(tema, type)
    }

    fun gyldigeTyperFor(tema: Behandlingstema): List<Behandlingstype?> =
        gyldigeKombinasjoner
            .filter { it.first == tema }
            .map { it.second }

    fun gyldigeTemaerFor(type: Behandlingstype): List<Behandlingstema?> =
        gyldigeKombinasjoner
            .filter { it.second == type }
            .map { it.first }
}
