package no.nav.historisk.superhelt.sak.model

enum class SaksType(val navn: String, val beskrivelse: String) {
    PARYKK("Parykk", "Dekker kostnader til parykk"),
    ORTOSE("Ortose", "Dekker kostnader til ortopediske hjelpemidler"),
    PROTESE("Protese", "Dekker kostnader til proteser"),
    FOTTOY("Fottøy", "Dekker kostnader til ortopedisk fottøy"),
    REISEUTGIFTER("Reiseutgifter", "Dekker kostnader til reise i forbindelse med behandling")
}