package no.nav.historisk.superhelt.vedtak.db

import jakarta.persistence.*
import no.nav.common.types.Aar
import no.nav.common.types.Behandlingsnummer
import no.nav.common.types.Belop
import no.nav.common.types.Fnr
import no.nav.historisk.superhelt.infrastruktur.NavUser
import no.nav.historisk.superhelt.sak.StonadsType
import no.nav.historisk.superhelt.sak.db.SakJpaEntity
import no.nav.historisk.superhelt.utbetaling.UtbetalingsType
import no.nav.historisk.superhelt.vedtak.Vedtak
import no.nav.historisk.superhelt.vedtak.VedtaksResultat
import org.hibernate.Hibernate
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "vedtak")
class VedtakJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sak_id", nullable = false)
    val sak: SakJpaEntity,

    @Column(nullable = false, unique = true)
    val behandlingsnummer: Behandlingsnummer,

    @Enumerated(EnumType.STRING)
    val type: StonadsType,

    val fnr: Fnr,
    val tittel: String,

    @Enumerated(EnumType.STRING)
    val resultat: VedtaksResultat,
    val begrunnelse: String?,

    @Enumerated(EnumType.STRING)
    val utbetalingsType: UtbetalingsType,
    val belop: Int?,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "navIdent", column = Column(name = "saksbehandler_navIdent")),
        AttributeOverride(name = "navn", column = Column(name = "saksbehandler_navn"))
    )
    var saksbehandler: NavUser,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "navIdent", column = Column(name = "attestant_navIdent")),
        AttributeOverride(name = "navn", column = Column(name = "attestant_navn"))
    )
    var attestant: NavUser,

    val soknadsDato: LocalDate,
    var tildelingsAar: Int?,
    val vedtaksTidspunkt: Instant,

    ) {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as VedtakJpaEntity

        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    internal fun toDomain(): Vedtak {

        return Vedtak(
            saksnummer = this.sak.saksnummer,
            behandlingsnummer = this.behandlingsnummer,
            stonadstype = this.type,
            fnr = this.fnr,
            tittel = this.tittel,
            begrunnelse = this.begrunnelse,
            resultat = this.resultat,
            saksbehandler = this.saksbehandler,
            attestant = this.attestant,
            soknadsDato = this.soknadsDato,
            tildelingsAar = this.tildelingsAar?.let { Aar(it) },
            vedtaksTidspunkt = vedtaksTidspunkt,
            utbetalingsType = this.utbetalingsType,
            belop = this.belop?.let { Belop(it) },
        )
    }
}
