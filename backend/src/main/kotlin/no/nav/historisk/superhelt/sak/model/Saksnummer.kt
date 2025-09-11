package no.nav.historisk.superhelt.sak.model

import no.nav.historisk.superhelt.sak.SakDto

typealias Saksnummer = String

val SakEntity.saksnummer: Saksnummer
    get() = "SUPER-${this.id?: 0.toString().padStart(6, '0')}"

fun Saksnummer.toId(): Long {
    return this.split("-").getOrNull(1)?.toLongOrNull() ?: -999L
}