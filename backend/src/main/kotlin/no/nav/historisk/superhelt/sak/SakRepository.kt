package no.nav.historisk.superhelt.sak

import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.person.Fnr
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PreAuthorize

interface SakRepository {
    @PreAuthorize("hasAuthority('WRITE')")
    fun save(sak: Sak): Sak

    @PreAuthorize("hasAuthority('READ')")
    @PostAuthorize("@tilgangsmaskin.harTilgang(returnObject.fnr)")
    fun getSak(saksnummer: Saksnummer): Sak?

    @PreAuthorize("hasAuthority('READ') and @tilgangsmaskin.harTilgang(#fnr)")
    fun findSaker(fnr: Fnr): List<Sak>

    fun getSakOrThrow(saksnummer: Saksnummer): Sak {
        return getSak(saksnummer)
            ?: throw IkkeFunnetException("Sak med saksnummer $saksnummer ikke funnet")
    }
}