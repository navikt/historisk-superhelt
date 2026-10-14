#let brevstil(data, body) = {
  set document(
    title: "Brev fra NAV",
    author: "Nav",
    description: "",
  )
  set text(
    font: "Source Sans Pro",
    size: 9pt,
    lang: "no",
  )
  set par(leading: 3pt, spacing: 9pt)
  set page(
    paper: "a4",
    margin: (top: 48pt, right: 48pt, bottom: 48pt, left: 55.5pt),
    header: if data.at("forhandsvisning", default: false) {
      align(right, text(fill: red, weight: "bold", size: 13.5pt)[Forhåndsvisning])
    },
    footer: context {
      set text(size: 6.75pt)
      grid(
        columns: (1fr, auto),
        [Behandlingsnummer: #data.behandlingsnummer],
        [side #counter(page).get().first() av #counter(page).final().first()],
      )
    },
  )
  set heading(numbering: none)
  show heading.where(level: 1): it => block(above: 30pt, below: 19.5pt)[
    #text(size: 12pt, weight: "bold", tracking: 0.225pt, it.body)
  ]
  show heading.where(level: 2): it => block(above: 19.5pt, below: 4.5pt)[
    #text(size: 9.75pt, weight: "bold", tracking: 0.1875pt, it.body)
  ]
  show heading.where(level: 3): it => block(above: 19.5pt, below: 4.5pt)[
    #text(size: 9pt, weight: "bold", tracking: 0.15pt, it.body)
  ]
  show heading.where(level: 4): it => block(above: 19.5pt, below: 4.5pt)[
    #text(size: 8.25pt, weight: "bold", tracking: 0.075pt, it.body)
  ]
  set list(indent: 0pt, body-indent: 12pt, spacing: 3pt)
  set enum(indent: 0pt, body-indent: 12pt, spacing: 3pt)
  body
}
