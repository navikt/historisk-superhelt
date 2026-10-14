#let brevstil(data, body) = {
  set document(
    title: "Brev fra Nav",
    author: "Nav",
    description: "",
  )
  set text(
    font: "Source Sans Pro",
    size: 11pt,
    lang: "no",
    top-edge: 0.8em,
    bottom-edge: 0.2em,
  )
  // Typst måler linjeavstanden fra tekstens top-edge til neste baseline.
  set par(leading: 7.2pt, spacing: 23.2pt)
  set page(
    paper: "a4",
    margin: (top: 64pt, right: 64pt, bottom: 74pt, left: 64pt),
    footer-descent: 40.8pt,
    header: if data.at("forhandsvisning", default: false) {
      align(right, text(fill: red, weight: "bold", size: 13.5pt)[Forhåndsvisning])
    },
    footer: context {
      set text(size: 9pt)
      grid(
        columns: (1fr, auto),
        [Saksnummer: #data.behandlingsnummer],
        [side #counter(page).get().first() av #counter(page).final().first()],
      )
    },
  )
  set heading(numbering: none)
  // Depth styrer utseendet, mens level kan være normalisert for PDF/UA.
  show heading.where(depth: 1): set text(size: 16pt, weight: "bold", tracking: 0.3pt)
  show heading.where(depth: 1): set par(leading: 7.2pt)
  show heading.where(depth: 1): set block(above: 40pt, below: 26pt)
  show heading.where(depth: 2): set text(size: 13pt, weight: "bold", tracking: 0.25pt)
  show heading.where(depth: 2): set par(leading: 5.6pt)
  show heading.where(depth: 3): set text(size: 12pt, weight: "bold", tracking: 0.2pt)
  show heading.where(depth: 3): set par(leading: 6.4pt)
  show heading.where(depth: 4): set text(size: 11pt, weight: "bold", tracking: 0.1pt)
  show heading.where(depth: 4): set par(leading: 7.2pt)
  show heading.where(depth: 2): set block(above: 26pt, below: 6pt)
  show heading.where(depth: 3): set block(above: 26pt, below: 6pt)
  show heading.where(depth: 4): set block(above: 26pt, below: 6pt)
  set list(indent: 0pt, body-indent: 16pt, spacing: 7.2pt)
  set enum(indent: 0pt, body-indent: 16pt, spacing: 7.2pt)
  body
}
