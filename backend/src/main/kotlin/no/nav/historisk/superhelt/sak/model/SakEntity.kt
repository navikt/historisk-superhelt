package no.nav.historisk.superhelt.sak.model

import jakarta.persistence.*

@Entity
@Table(name = "sak")
class SakEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var id: Long?=null,
    var saksnummer: Saksnummer,
    @Enumerated(EnumType.STRING)
    var type: StonadsType,
    var person: Personident,
    var tittel: String?= null,
    var begrunnelse: String?=null,
    @Enumerated(EnumType.STRING)
    var status: SakStatus= SakStatus.UNDER_BEHANDLING,
    @Enumerated(EnumType.STRING)
    var vedtak: VedtakType? = null,
    // Utbetaling
    // Oppgaver
    // Brev
) {
    fun getId(): Long? = id
}
