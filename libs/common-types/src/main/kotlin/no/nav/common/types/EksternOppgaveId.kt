package no.nav.common.types

import io.swagger.v3.oas.annotations.media.Schema

/** Id på en oppgave i Navs oppgave system */
@Schema(type = "number")
@JvmInline
value class EksternOppgaveId(val value: Long) {
    override fun toString(): String {
        return value.toString()
    }
}