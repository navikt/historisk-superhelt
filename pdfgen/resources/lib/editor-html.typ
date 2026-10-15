// Breveditoren bruker section og data-editable til å skille fast og redigerbart innhold.
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

#let editor-html(body) = {
  show heading: it => context {
    if target() == "html" {
      // Typsts standardeksport forskyver nivåene med én og endrer editorinnholdet ved retur.
      html.elem("h" + str(it.level), it.body)
    } else {
      it
    }
  }
  body
}
