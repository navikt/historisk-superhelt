package no.nav.oppgave.infrastructure


import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper

object Serializer {
    @JvmStatic
    val jacksonObjectMapper: ObjectMapper =   JsonMapper.builder()
        .findAndAddModules()
//        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .build()
}
