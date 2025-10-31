package no.nav.historisk.superhelt.sak.db

import jakarta.persistence.*
import no.nav.historisk.superhelt.sak.*
import no.nav.historisk.superhelt.sak.Forhandstilsagn
import no.nav.historisk.superhelt.sak.Utbetaling
import no.nav.person.Fnr
import org.hibernate.Hibernate
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "sak")
//@EntityListeners(AuditingEntityListener::class)
class SakJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Enumerated(EnumType.STRING)
    var type: StonadsType,
    var fnr: Fnr,
    var tittel: String? = null,

    @Enumerated(EnumType.STRING)
    var status: SakStatus= SakStatus.UNDER_BEHANDLING,

    @Enumerated(EnumType.STRING)
    var vedtak: VedtakType? = null,

    var begrunnelse: String? = null,

    var saksbehandler: String,
    var opprettet: LocalDateTime = LocalDateTime.now(),
    var soknadsDato: LocalDate? = null,

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    var utbetaling: UtbetalingJpaEntity? = null,

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    var forhandstilsagn: ForhandsTilsagnJpaEntity? = null,

//    @CreatedDate
//    var createdDate: Instant? = null,
//
//    @CreatedBy
//    var createdBy: String? = null,
//
//    @LastModifiedDate
//    var lastModifiedDate: Instant? = null,
//
//    @LastModifiedBy
//    var lastModifiedBy: String? = null

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as SakJpaEntity

        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    fun setOrUpdateUtbetaling(utbetalingDomain: Utbetaling?) {
        if (utbetalingDomain == null) {
            this.utbetaling = null
            return
        }
        val entity = this.utbetaling ?: UtbetalingJpaEntity(sak = this, belop = 0.0)
        entity.belop = utbetalingDomain.belop
        this.utbetaling = entity
    }

    fun setOrUpdateForhandsTilsagn(forhandsTilsagnDomain: Forhandstilsagn?) {
        if (forhandsTilsagnDomain == null) {
            this.forhandstilsagn = null
            return
        }
        val entity = this.forhandstilsagn ?: ForhandsTilsagnJpaEntity(sak = this)
        this.forhandstilsagn = entity
    }


    internal fun toDomain(): Sak {
        return Sak(
            saksnummer = this.id?.let { Saksnummer(it) },
            type = this.type,
            fnr = this.fnr,
            tittel = this.tittel,
            begrunnelse = this.begrunnelse,
            status = this.status,
            vedtak = this.vedtak,
            saksbehandler = this.saksbehandler,
            opprettetDato = this.opprettet.toLocalDate(),
            soknadsDato = this.soknadsDato,
            utbetaling = this.utbetaling?.toDomain(),
            forhandstilsagn = this.forhandstilsagn?.toDomain(),
        )
    }


}


