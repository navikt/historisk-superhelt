#import "style.typ": brevstil
#import "standardtekster.typ": standardtekster

#let seksjon(body, redigerbar: false) = context {
  if target() == "html" {
    html.elem("section")[
      #if redigerbar {
        html.elem("div", attrs: (class: "data-editable"), body)
      } else {
        body
      }
    ]
  } else {
    body
  }
}

#let personalia(data) = {
  context if target() == "html" {
    html.elem("div", attrs: (class: "brev-logo"))[
      #image("/resources/NAV_logo_digital_Red.svg", height: 16pt, alt: "Nav logo")
    ]
    html.elem("div", attrs: (class: "brev-personalia"))[
      #html.elem("dl")[
        #html.elem("dt")[Navn:]
        #html.elem("dd")[#data.personalia.fornavn #data.personalia.etternavn]
        #html.elem("dt")[Fødselsnummer:]
        #html.elem("dd")[#data.personalia.ident]
        #html.elem("dt")[Saksnummer:]
        #html.elem("dd")[#data.behandlingsnummer]
      ]
      #html.elem("p", attrs: (class: "brev-dato"))[#data.datoForUtsending]
    ]
  } else {
    block(below: 48pt)[
      #image("/resources/NAV_logo_digital_Red.svg", height: 16pt, alt: "Nav logo")
    ]
    grid(
      columns: (auto, auto, 1fr),
      column-gutter: 24pt,
      align: (left, left, right + bottom),
      [
        Navn: \
        Fødselsnummer: \
        Saksnummer:
      ],
      [
        #data.personalia.fornavn #data.personalia.etternavn \
        #data.personalia.ident \
        #data.behandlingsnummer
      ],
      [#data.datoForUtsending],
    )
  }
}

#let signaturnavn(data, dobbel) = context {
  if target() == "html" {
    html.elem("div", attrs: (class: if dobbel { "brev-signaturnavn brev-signaturnavn-dobbel" } else { "brev-signaturnavn" }))[
      #if dobbel {
        html.elem("p")[#data.beslutterNavn]
      }
      #html.elem("p")[#data.saksbehandlerNavn]
    ]
  } else if dobbel {
    grid(columns: (1fr, 1fr), [#data.beslutterNavn], [#data.saksbehandlerNavn])
  } else {
    [#data.saksbehandlerNavn]
  }
}

#let signatur(data, dobbel) = context if target() == "html" {
  html.elem("div", attrs: (class: "brev-signatur"))[
    #html.elem("p")[Med vennlig hilsen]
    #signaturnavn(data, dobbel)
    #html.elem("p", attrs: (class: "brev-kontor"))[#data.kontor]
  ]
} else {
  block(above: 32pt, breakable: false)[
    #set par(spacing: 0pt)
    Med vennlig hilsen
    #v(7.2pt)
    #signaturnavn(data, dobbel)
    #v(26pt)
    #data.kontor
  ]
}

#let brev(data) = {
  assert("innhold_typst" in data, message: "innhold_typst er påkrevd")
  assert(type(data.innhold_typst) == str, message: "innhold_typst må være en streng")
  let innhold = eval(data.innhold_typst, mode: "markup")
  let vedtak = data.at("brevtype", default: "") == "VEDTAKSBREV"
  let bruker = data.at("mottaker", default: "") == "BRUKER"
  let tekster = standardtekster(data.at("målform", default: "") == "NN")
  let beslutter = data.at("beslutterNavn", default: "")
  let dobbel-signatur = vedtak and beslutter != none and beslutter != ""

  brevstil(data)[
    #seksjon[
      #personalia(data)
    ]
    #seksjon(redigerbar: true)[
      #innhold
    ]

    #seksjon[
      #if bruker {
        if vedtak {
          tekster.klagerett
          tekster.innsynsrett
        }
        tekster.personopplysningsrett
        tekster.veiledningsrett
        tekster.sporsmal
      }

      #signatur(data, dobbel-signatur)
    ]
  ]
}
