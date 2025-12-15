package no.nav.historisk.superhelt.dokarkiv

import no.nav.dokarkiv.DokarkivClient
import no.nav.pdl.SafGraphqlClient
import no.nav.saf.rest.SafRestClient
import org.springframework.stereotype.Service

@Service
class DokarkivService (
    private val dokarkivClient: DokarkivClient,
    private val safGqlClient: SafGraphqlClient,
    private val safRestClient: SafRestClient

    ){
}