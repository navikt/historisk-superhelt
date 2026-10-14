#let hoved(innhold_typst) = {
  assert(type(innhold_typst) == str, message: "innhold_typst må være en streng")
  eval(innhold_typst, mode: "markup")
}
