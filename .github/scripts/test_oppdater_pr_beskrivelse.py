#!/usr/bin/env python3
"""Tester for oppdater_pr_beskrivelse."""

import unittest
import io
import os
import tempfile
import unittest.mock

from oppdater_pr_beskrivelse import (
    MARKOR_SLUTT,
    MARKOR_START,
    OVERSKRIFT,
    bygg_blokk,
    flett_inn,
    hent_eksisterende_sammendrag,
    main,
)

TABELL = "| Modul | Filer | Linjer |\n| --- | ---: | --- |\n| `apps/bidrag-sak` | 2 | +10 / -3 |"
SAMMENDRAG = "Legger til validering av saksnummer."


class ByggBlokkTest(unittest.TestCase):
    def test_blokken_er_omsluttet_av_markorer(self):
        blokk = bygg_blokk(SAMMENDRAG, TABELL)
        self.assertTrue(blokk.startswith(MARKOR_START), blokk)
        self.assertTrue(blokk.endswith(MARKOR_SLUTT), blokk)

    def test_blokken_inneholder_sammendrag_og_tabell(self):
        blokk = bygg_blokk(SAMMENDRAG, TABELL)
        self.assertIn(SAMMENDRAG, blokk)
        self.assertIn("apps/bidrag-sak", blokk)
        self.assertIn(OVERSKRIFT, blokk)

    def test_tomt_sammendrag_gir_blokk_med_bare_tabell(self):
        blokk = bygg_blokk("", TABELL)
        self.assertIn("apps/bidrag-sak", blokk)
        self.assertIn("### Berørte moduler", blokk)
        self.assertTrue(blokk.startswith(MARKOR_START), blokk)
        self.assertTrue(blokk.endswith(MARKOR_SLUTT), blokk)
        # Ingen tom linje mellom overskrift og tabelloverskrift.
        self.assertNotIn("\n\n\n", blokk)

    def test_sammendrag_med_bare_blanktegn_behandles_som_tomt(self):
        self.assertEqual(bygg_blokk("   \n\n ", TABELL), bygg_blokk("", TABELL))


class FlettInnTest(unittest.TestCase):
    def test_tom_beskrivelse_gir_bare_blokken(self):
        blokk = bygg_blokk(SAMMENDRAG, TABELL)
        self.assertEqual(flett_inn("", blokk), blokk)

    def test_none_beskrivelse_gir_bare_blokken(self):
        blokk = bygg_blokk(SAMMENDRAG, TABELL)
        self.assertEqual(flett_inn(None, blokk), blokk)

    def test_blokken_legges_til_etter_eksisterende_tekst(self):
        blokk = bygg_blokk(SAMMENDRAG, TABELL)
        resultat = flett_inn("Min egen beskrivelse.", blokk)
        self.assertTrue(resultat.startswith("Min egen beskrivelse."), resultat)
        self.assertIn(blokk, resultat)

    def test_eksisterende_blokk_erstattes_uten_duplisering(self):
        gammel = bygg_blokk("Gammelt sammendrag", TABELL)
        ny = bygg_blokk("Nytt sammendrag", TABELL)
        beskrivelse = flett_inn("Min egen beskrivelse.", gammel)

        resultat = flett_inn(beskrivelse, ny)

        self.assertEqual(resultat.count(MARKOR_START), 1, resultat)
        self.assertEqual(resultat.count(MARKOR_SLUTT), 1, resultat)
        self.assertIn("Nytt sammendrag", resultat)
        self.assertNotIn("Gammelt sammendrag", resultat)

    def test_brukerens_egen_tekst_bevares_bade_for_og_etter_blokken(self):
        gammel = bygg_blokk("Gammelt sammendrag", TABELL)
        beskrivelse = f"Foran teksten.\n\n{gammel}\n\nBak teksten."

        resultat = flett_inn(beskrivelse, bygg_blokk("Nytt sammendrag", TABELL))

        self.assertIn("Foran teksten.", resultat)
        self.assertIn("Bak teksten.", resultat)
        self.assertIn("Nytt sammendrag", resultat)

    def test_kjoring_er_idempotent_med_samme_innhold(self):
        blokk = bygg_blokk(SAMMENDRAG, TABELL)
        forste = flett_inn("Min egen beskrivelse.", blokk)
        andre = flett_inn(forste, blokk)
        self.assertEqual(forste, andre)

    def test_ufullstendig_markor_behandles_som_vanlig_tekst(self):
        blokk = bygg_blokk(SAMMENDRAG, TABELL)
        resultat = flett_inn(f"Tekst med {MARKOR_START} men ingen slutt", blokk)
        self.assertIn("men ingen slutt", resultat)
        self.assertEqual(resultat.count(MARKOR_SLUTT), 1, resultat)

    def test_los_startmarkor_i_brukertekst_spiser_ikke_teksten(self):
        blokk = bygg_blokk(SAMMENDRAG, TABELL)
        forste = flett_inn(f"Tekst med {MARKOR_START} men ingen slutt", blokk)

        andre = flett_inn(forste, bygg_blokk("Nytt sammendrag", TABELL))

        self.assertIn("men ingen slutt", andre)
        self.assertIn("Nytt sammendrag", andre)
        self.assertEqual(andre.count(MARKOR_SLUTT), 1, andre)


class HentEksisterendeSammendragTest(unittest.TestCase):
    def test_henter_prosa_fra_eksisterende_blokk(self):
        beskrivelse = flett_inn("Min egen tekst.", bygg_blokk(SAMMENDRAG, TABELL))
        self.assertEqual(hent_eksisterende_sammendrag(beskrivelse), SAMMENDRAG)

    def test_henter_flerlinjes_prosa(self):
        prosa = "Første linje.\n\n- punkt 1\n- punkt 2"
        beskrivelse = flett_inn("", bygg_blokk(prosa, TABELL))
        self.assertEqual(hent_eksisterende_sammendrag(beskrivelse), prosa)

    def test_tom_streng_uten_blokk(self):
        self.assertEqual(hent_eksisterende_sammendrag("Bare min tekst."), "")

    def test_tom_streng_nar_blokken_ikke_har_prosa(self):
        beskrivelse = flett_inn("", bygg_blokk("", TABELL))
        self.assertEqual(hent_eksisterende_sammendrag(beskrivelse), "")

    def test_tom_streng_ved_none(self):
        self.assertEqual(hent_eksisterende_sammendrag(None), "")

    def test_plukker_ikke_opp_tabellen(self):
        beskrivelse = flett_inn("", bygg_blokk(SAMMENDRAG, TABELL))
        self.assertNotIn("bidrag-sak", hent_eksisterende_sammendrag(beskrivelse))


class MainTest(unittest.TestCase):
    """Verifiserer CLI-en, som er det workflowen faktisk kaller."""

    def _skriv(self, katalog, navn, innhold):
        sti = os.path.join(katalog, navn)
        with open(sti, "w", encoding="utf-8") as fil:
            fil.write(innhold)
        return sti

    def _kjor(self, argv):
        stdout = io.StringIO()
        with unittest.mock.patch("sys.stdout", stdout):
            self.assertEqual(main(argv), 0)
        return stdout.getvalue()

    def test_gjenbruker_prosa_nar_sammendrag_mangler(self):
        """Dette er synchronize-scenarioet: tabellen oppdateres, prosaen beholdes."""
        with tempfile.TemporaryDirectory() as katalog:
            gammel_body = flett_inn("Min egen tekst.", bygg_blokk(SAMMENDRAG, TABELL))
            ny_tabell = TABELL.replace("+10 / -3", "+99 / -7")
            body_fil = self._skriv(katalog, "body.md", gammel_body)
            tabell_fil = self._skriv(katalog, "tabell.md", ny_tabell)

            resultat = self._kjor([
                "--beskrivelse-fil", body_fil,
                "--tabell-fil", tabell_fil,
            ])

            self.assertIn(SAMMENDRAG, resultat)
            self.assertIn("+99 / -7", resultat)
            self.assertNotIn("+10 / -3", resultat)
            self.assertIn("Min egen tekst.", resultat)
            self.assertEqual(resultat.count(MARKOR_START), 1, resultat)

    def test_nytt_sammendrag_overstyrer_det_gamle(self):
        with tempfile.TemporaryDirectory() as katalog:
            gammel_body = flett_inn("", bygg_blokk("Gammel prosa", TABELL))
            body_fil = self._skriv(katalog, "body.md", gammel_body)
            tabell_fil = self._skriv(katalog, "tabell.md", TABELL)
            sammendrag_fil = self._skriv(katalog, "sammendrag.md", "Ny prosa")

            resultat = self._kjor([
                "--beskrivelse-fil", body_fil,
                "--tabell-fil", tabell_fil,
                "--sammendrag-fil", sammendrag_fil,
            ])

            self.assertIn("Ny prosa", resultat)
            self.assertNotIn("Gammel prosa", resultat)

    def test_tomt_sammendrag_faller_tilbake_til_eksisterende(self):
        with tempfile.TemporaryDirectory() as katalog:
            gammel_body = flett_inn("", bygg_blokk("Gammel prosa", TABELL))
            body_fil = self._skriv(katalog, "body.md", gammel_body)
            tabell_fil = self._skriv(katalog, "tabell.md", TABELL)
            sammendrag_fil = self._skriv(katalog, "sammendrag.md", "  \n ")

            resultat = self._kjor([
                "--beskrivelse-fil", body_fil,
                "--tabell-fil", tabell_fil,
                "--sammendrag-fil", sammendrag_fil,
            ])

            self.assertIn("Gammel prosa", resultat)


if __name__ == "__main__":
    unittest.main(verbosity=2)