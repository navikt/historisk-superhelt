import type { TravelCostResult } from "../services/travelCostService";
import styles from "./TravelCostResult.module.css";

interface TravelCostResultProps {
  result: TravelCostResult;
}

function TravelCostResultComponent({ result }: TravelCostResultProps) {
  return (
    <div className={styles.card} role="region" aria-label="Reisekostnadsresultat">
      <h2 className={styles.title}>Beregnet reisekostnad</h2>
      <div className={styles.route}>
        <span className={styles.routeLabel}>Reiserute:</span>
        <span className={styles.routeValue}>
          {result.fraStasjon} → {result.tilStasjon}
        </span>
      </div>
      <dl className={styles.details}>
        <div className={styles.detailRow}>
          <dt>Estimert avstand</dt>
          <dd>{result.distanceKm} km</dd>
        </div>
        <div className={styles.detailRow}>
          <dt>Estimert reisetid</dt>
          <dd>{result.reisetidMin} minutter</dd>
        </div>
        <div className={`${styles.detailRow} ${styles.totalRow}`}>
          <dt>Total kostnad</dt>
          <dd className={styles.totalAmount}>
            {result.kostnadKr.toLocaleString("nb-NO", {
              style: "currency",
              currency: "NOK",
              minimumFractionDigits: 2,
            })}
          </dd>
        </div>
      </dl>
      <p className={styles.disclaimer}>
        * Kostnad beregnes med en sats på 3,50 kr/km. Avstand er estimert.
      </p>
    </div>
  );
}

export default TravelCostResultComponent;
