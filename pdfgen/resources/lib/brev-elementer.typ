#let personalia(data) = {
  // HTML-eksporten utelater grid, så personalia får egen HTML-struktur.
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
  // HTML og PDF deler navnerekkefølge, men trenger ulikt kolonneoppsett.
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
