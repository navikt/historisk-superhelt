#!/usr/bin/env python3
"""Fletter en generert endringsbeskrivelse inn i en pull request-beskrivelse.

Teksten legges i en markert blokk slik at kjøringen er idempotent og aldri
overskriver det utvikleren selv har skrevet i beskrivelsen.
"""

from __future__ import annotations

import argparse
import sys

MARKOR_START = "<!-- pr-beskrivelse:start -->"
MARKOR_SLUTT = "<!-- pr-beskrivelse:slutt -->"
OVERSKRIFT = "## Automatisk endringsoversikt"
UNDEROVERSKRIFT_TABELL = "### Berørte moduler"
FOTNOTE = (
    "_Generert automatisk. Rediger gjerne teksten utenfor blokken - "
    "innholdet i blokken blir overskrevet ved neste push._"
)


def bygg_blokk(sammendrag: str, tabell: str) -> str:
    """Setter sammen den markerte blokken av sammendrag og modultabell.

    Sammendraget er valgfritt: mangler det (typisk fordi AI-steget ble hoppet
    over), tas kun modultabellen med.
    """
    deler = [MARKOR_START, OVERSKRIFT]

    if sammendrag and sammendrag.strip():
        deler += [sammendrag.strip(), ""]

    deler += [
        UNDEROVERSKRIFT_TABELL,
        tabell.strip(),
        "",
        FOTNOTE,
        MARKOR_SLUTT,
    ]
    return "\n".join(deler)


def _finn_blokk(beskrivelse: str) -> tuple[int, int]:
    """Returnerer (start, slutt) for den markerte blokken, eller (-1, -1).

    Leter etter den siste startmarkøren som har en tilhørende sluttmarkør, slik
    at en løs markør i utviklerens egen tekst ikke fører til at tekst mellom
    markørene blir spist opp.
    """
    start = beskrivelse.rfind(MARKOR_START)
    if start == -1:
        return -1, -1
    slutt = beskrivelse.find(MARKOR_SLUTT, start)
    if slutt == -1:
        return -1, -1
    return start, slutt


def hent_eksisterende_sammendrag(beskrivelse: str) -> str:
    """Henter prosa-delen ut av en blokk som allerede står i beskrivelsen.

    Brukes når kjøringen ikke har generert et nytt sammendrag, typisk ved push
    til en åpen PR. Da beholdes prosaen fra forrige kjøring i stedet for at den
    forsvinner når tabellen oppdateres.
    """
    beskrivelse = beskrivelse or ""
    start, slutt = _finn_blokk(beskrivelse)
    if start == -1:
        return ""

    blokk = beskrivelse[start:slutt]
    overskrift_slutt = blokk.find(OVERSKRIFT)
    if overskrift_slutt == -1:
        return ""
    overskrift_slutt += len(OVERSKRIFT)

    tabell_start = blokk.find(UNDEROVERSKRIFT_TABELL, overskrift_slutt)
    if tabell_start == -1:
        return ""

    return blokk[overskrift_slutt:tabell_start].strip()


def flett_inn(beskrivelse: str, blokk: str) -> str:
    """Erstatter en eksisterende markert blokk, eller legger blokken til slutt.

    Beholder posisjonen til en eksisterende blokk slik at beskrivelsen ikke
    stokkes om ved hver push.
    """
    beskrivelse = beskrivelse or ""
    start, slutt = _finn_blokk(beskrivelse)

    if start != -1:
        foran = beskrivelse[:start]
        bak = beskrivelse[slutt + len(MARKOR_SLUTT):]
        return f"{foran}{blokk}{bak}"

    if not beskrivelse.strip():
        return blokk

    return f"{beskrivelse.rstrip()}\n\n{blokk}"


def _les(sti: str) -> str:
    if not sti:
        return ""
    if sti == "-":
        return sys.stdin.read()
    with open(sti, encoding="utf-8") as fil:
        return fil.read()


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--beskrivelse-fil", required=True,
                        help="Fil med nåværende PR-beskrivelse")
    parser.add_argument("--sammendrag-fil", default="",
                        help="Fil med generert prosa-sammendrag. Utelates når "
                             "sammendrag ikke er generert; da gjenbrukes prosaen "
                             "fra en eventuell eksisterende blokk.")
    parser.add_argument("--tabell-fil", required=True,
                        help="Fil med markdown-tabell over berørte moduler")
    args = parser.parse_args(argv)

    beskrivelse = _les(args.beskrivelse_fil)
    sammendrag = _les(args.sammendrag_fil)
    if not sammendrag.strip():
        sammendrag = hent_eksisterende_sammendrag(beskrivelse)

    blokk = bygg_blokk(sammendrag, _les(args.tabell_fil))
    sys.stdout.write(flett_inn(beskrivelse, blokk))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
