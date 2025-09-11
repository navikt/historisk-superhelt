package no.nav.historisk.superhelt.sak.model

import jakarta.persistence.*

@Entity
@Table(name = "sak")
class SakEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?=null,

    @Enumerated(EnumType.STRING)
    var type: StonadsType,
    var person: Personident,
    var tittel: String? = null,

    @Enumerated(EnumType.STRING)
    var status: SakStatus= SakStatus.UNDER_BEHANDLING,

    // vedtak
    @Enumerated(EnumType.STRING)
    var vedtak: VedtakType? =null ,
    var begrunnelse: String?= null,
    // Utbetaling
    // Oppgaver
    // Brev
) {

}
