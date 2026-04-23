package no.nav.common.consts

enum class EksternFellesKodeverkTema(val navn: String) {
    HEL("Helsetjenester"),
    HJE("Hjelpemidler");

    val kode: String
        get() = this.name
}
