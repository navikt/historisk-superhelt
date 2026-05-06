package no.nav.historisk.superhelt.dokarkiv

import no.nav.dokarkiv.AvsenderMottakerIdType
import no.nav.historisk.superhelt.brev.BrevMottaker
import no.nav.historisk.superhelt.person.PersonService
import no.nav.historisk.superhelt.person.PersonTestData
import no.nav.historisk.superhelt.sak.SakTestData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class AvsenderMottakerResolverTest {

    @Mock
    private lateinit var personService: PersonService

    @InjectMocks
    private lateinit var resolver: AvsenderMottakerResolver

    @Test
    fun `skal bruke verges FNR som mottaker når verge finnes`() {
        val verge = PersonTestData.testPersonMedAdressebeskyttelse
        val bruker = PersonTestData.testPersonMedVerge
        val sak = SakTestData.sakUtenUtbetaling().copy(fnr = bruker.fnr)

        whenever(personService.hentVerge(bruker.fnr)).thenReturn(verge)

        val result = resolver.resolve(BrevMottaker.BRUKER, sak)

        assertEquals(verge.fnr.value, result.id)
        assertEquals(AvsenderMottakerIdType.FNR, result.idType)
    }

    @Test
    fun `skal bruke brukers FNR som mottaker når verge mangler`() {
        val sak = SakTestData.sakUtenUtbetaling()

        whenever(personService.hentVerge(sak.fnr)).thenReturn(null)

        val result = resolver.resolve(BrevMottaker.BRUKER, sak)

        assertEquals(sak.fnr.value, result.id)
        assertEquals(AvsenderMottakerIdType.FNR, result.idType)
    }

    @Test
    fun `skal kaste UnsupportedOperationException for SAMHANDLER`() {
        val sak = SakTestData.sakUtenUtbetaling()

        assertThrows<UnsupportedOperationException> {
            resolver.resolve(BrevMottaker.SAMHANDLER, sak)
        }
    }
}
