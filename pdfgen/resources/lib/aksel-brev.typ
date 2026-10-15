#import "style.typ": brevstil
#import "pdf-ua.typ": normaliser-overskrifter
#import "standardtekster.typ": standardtekster
#import "brev-elementer.typ": personalia, signatur
#import "editor-html.typ": seksjon, editor-html

#let brev(data) = {
  assert("innhold_typst" in data, message: "innhold_typst er påkrevd")
  assert(type(data.innhold_typst) == str, message: "innhold_typst må være en streng")
  let innhold = eval(data.innhold_typst, mode: "markup")
  let vedtak = data.at("brevtype", default: "") == "VEDTAKSBREV"
  let bruker = data.at("mottaker", default: "") == "BRUKER"
  let tekster = standardtekster(data.at("målform", default: "") == "NN")
  let beslutter = data.at("beslutterNavn", default: "")
  let dobbel-signatur = vedtak and beslutter != none and beslutter != ""

  let standardtekster = if bruker {
    if vedtak {
      tekster.klagerett
      tekster.innsynsrett
    }
    tekster.personopplysningsrett
    tekster.veiledningsrett
    tekster.sporsmal
  } else { [] }

  show: editor-html
  brevstil(data)[
    #context {
      let redigerbart = innhold
      let standardinnhold = standardtekster
      if target() != "html" {
        // Bare PDF-hierarkiet normaliseres; HTML beholder editorens valgte nivåer.
        let (normalisert, nivåer) = normaliser-overskrifter(innhold)
        redigerbart = normalisert
        // Standardtekstene fortsetter samme hierarki, også når innholdet mangler H1.
        let (normalisert-standard, _) = normaliser-overskrifter(standardtekster, nivåer: nivåer)
        standardinnhold = normalisert-standard
      }

      [
        #seksjon[
          #personalia(data)
        ]
        #seksjon(redigerbar: true)[
          #redigerbart
        ]
        #seksjon[
          #standardinnhold
          #signatur(data, dobbel-signatur)
        ]
      ]
    }
  ]
}
