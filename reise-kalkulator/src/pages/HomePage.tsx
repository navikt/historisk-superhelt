import { useState, type FormEvent } from "react";
import TravelCostResultComponent from "../components/TravelCostResult";
import type { TravelCostResult } from "../services/travelCostService";
import { beregnKostnad, beregnReisetid, simulerAvstand } from "../utils/calculateCost";
import styles from "./HomePage.module.css";

const HELSESTASJONER = [
  "Alna helsestasjon",
  "Bjerke helsestasjon",
  "Frogner helsestasjon",
  "Gamle Oslo helsestasjon",
  "Grünerløkka helsestasjon",
  "Nordre Aker helsestasjon",
  "Nordstrand helsestasjon",
  "St. Hanshaugen helsestasjon",
  "Sagene helsestasjon",
  "Stovner helsestasjon",
  "Søndre Nordstrand helsestasjon",
  "Ullern helsestasjon",
  "Vestre Aker helsestasjon",
  "Østensjø helsestasjon",
  "Østre Aker helsestasjon",
];

function HomePage() {
  const [fraStasjon, setFraStasjon] = useState("");
  const [tilStasjon, setTilStasjon] = useState("");
  const [result, setResult] = useState<TravelCostResult | null>(null);
  const [error, setError] = useState<string | null>(null);

  function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);

    if (!fraStasjon || !tilStasjon) {
      setError("Du må velge begge helsestasjoner.");
      return;
    }

    if (fraStasjon === tilStasjon) {
      setError("Fra- og til-stasjon kan ikke være den samme.");
      return;
    }

    const distanceKm = simulerAvstand(fraStasjon, tilStasjon);
    const kostnadKr = beregnKostnad(distanceKm);
    const reisetidMin = beregnReisetid(distanceKm);

    setResult({ fraStasjon, tilStasjon, distanceKm, kostnadKr, reisetidMin });
  }

  function handleReset() {
    setFraStasjon("");
    setTilStasjon("");
    setResult(null);
    setError(null);
  }

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <div className={styles.headerContent}>
          <h1 className={styles.appTitle}>Reisekostnadskalkulator</h1>
          <p className={styles.appSubtitle}>Beregn reisekostnad mellom to helsestasjoner</p>
        </div>
      </header>

      <main className={styles.main}>
        <div className={styles.container}>
          <section className={styles.formSection}>
            <h2 className={styles.formTitle}>Velg helsestasjoner</h2>
            <form onSubmit={handleSubmit} noValidate aria-label="Reisekostnadsberegner">
              <div className={styles.formGrid}>
                <div className={styles.fieldGroup}>
                  <label htmlFor="fraStasjon" className={styles.label}>
                    Fra helsestasjon
                    <span className={styles.required} aria-hidden="true">
                      {" "}
                      *
                    </span>
                  </label>
                  <select
                    id="fraStasjon"
                    className={styles.select}
                    value={fraStasjon}
                    onChange={(e) => setFraStasjon(e.target.value)}
                    aria-required="true"
                  >
                    <option value="">Velg helsestasjon...</option>
                    {HELSESTASJONER.map((stasjon) => (
                      <option key={stasjon} value={stasjon}>
                        {stasjon}
                      </option>
                    ))}
                  </select>
                </div>

                <div className={styles.fieldGroup}>
                  <label htmlFor="tilStasjon" className={styles.label}>
                    Til helsestasjon
                    <span className={styles.required} aria-hidden="true">
                      {" "}
                      *
                    </span>
                  </label>
                  <select
                    id="tilStasjon"
                    className={styles.select}
                    value={tilStasjon}
                    onChange={(e) => setTilStasjon(e.target.value)}
                    aria-required="true"
                  >
                    <option value="">Velg helsestasjon...</option>
                    {HELSESTASJONER.map((stasjon) => (
                      <option key={stasjon} value={stasjon}>
                        {stasjon}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {error && (
                <div className={styles.errorMessage} role="alert" aria-live="polite">
                  {error}
                </div>
              )}

              <div className={styles.formActions}>
                <button type="submit" className={styles.primaryButton}>
                  Beregn reisekostnad
                </button>
                {result && (
                  <button type="button" className={styles.secondaryButton} onClick={handleReset}>
                    Nullstill
                  </button>
                )}
              </div>
            </form>
          </section>

          {result && <TravelCostResultComponent result={result} />}

          <section className={styles.infoSection}>
            <h2 className={styles.infoTitle}>Om kalkulatoren</h2>
            <p className={styles.infoText}>
              Denne kalkulatoren beregner estimert reisekostnad mellom to helsestasjoner basert på
              kjøreavstand. Kostnaden beregnes etter statens satser for reisegodtgjørelse.
            </p>
            <ul className={styles.infoList}>
              <li>Sats: 3,50 kr per km</li>
              <li>Avstandene er estimerte og kan avvike fra faktisk kjørerute</li>
              <li>For nøyaktige beregninger, bruk faktisk kilometertelling</li>
            </ul>
          </section>
        </div>
      </main>

      <footer className={styles.footer}>
        <p>© {new Date().getFullYear()} NAV – Team Historisk</p>
      </footer>
    </div>
  );
}

export default HomePage;
