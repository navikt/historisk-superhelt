#import "hoved.typ": hoved
#import "style.typ": brevstil
#import "klagerett.typ": klagerett
#import "klagerett_NN.typ": klagerett_NN
#import "innsynsrett.typ": innsynsrett
#import "innsynsrett_NN.typ": innsynsrett_NN
#import "personopplysningsrett.typ": personopplysningsrett
#import "personopplysningsrett_NN.typ": personopplysningsrett_NN
#import "veiledningsrett.typ": veiledningsrett
#import "veiledningsrett_NN.typ": veiledningsrett_NN
#import "sporsmal.typ": sporsmal
#import "sporsmal_NN.typ": sporsmal_NN

#let brev(data) = {
  assert("innhold_typst" in data, message: "innhold_typst er påkrevd")
  let innhold = hoved(data.innhold_typst)
  let nynorsk = data.at("målform", default: "") == "NN"
  let beslutter = data.at("beslutterNavn", default: "")
  let har-beslutter = beslutter != none and beslutter != ""

  brevstil(data)[
    #block(below: 36pt)[
      #image("/resources/NAV_logo_digital_Red.svg", height: 12pt, alt: "Nav logo")
    ]
    #grid(
      columns: (auto, auto, 1fr),
      column-gutter: 24pt,
      align: (left, left, right + bottom),
      [
        Navn: \
        Fødselsnummer: \
        Behandlingsnummer:
      ],
      [
        #data.personalia.fornavn #data.personalia.etternavn \
        #data.personalia.ident \
        #data.behandlingsnummer
      ],
      [#data.datoForUtsending],
    )

    #innhold

    #if data.at("mottaker", default: "") == "BRUKER" {
      if data.at("brevtype", default: "") == "VEDTAKSBREV" {
        if nynorsk {
          klagerett_NN
          innsynsrett_NN
        } else {
          klagerett
          innsynsrett
        }
      }
      if nynorsk {
        personopplysningsrett_NN
        veiledningsrett_NN
        sporsmal_NN
      } else {
        personopplysningsrett
        veiledningsrett
        sporsmal
      }
    }

    #block(above: 24pt, breakable: false)[
      #set par(spacing: 0pt)
      Med vennlig hilsen

      #grid(
        columns: (1fr, 1fr),
        [#if har-beslutter { beslutter } else { data.saksbehandlerNavn }],
        [#if har-beslutter { data.saksbehandlerNavn }],
      )
      #block(above: 19.5pt)[#data.kontor]
    ]
  ]
}
