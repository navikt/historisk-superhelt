// JSON data is injected by the server as a virtual file at /data/nav-brev/brev.json.
#let data = json("/data/nav-brev/brev.json")

#let recipient = data.at("mottaker", default: (:))
#let sender = data.at("avsender", default: (:))
#let contact = data.at("kontakt", default: (:))
#let paragraphs = data.at("innhold", default: ())
#let references = data.at("referanser", default: ())

#set document(title: data.at("tittel", default: "Brev fra Nav"))
#set text(font: "Source Sans 3", lang: "nb", size: 11pt)
#set page(
  paper: "a4",
  margin: (top: 16.9mm, bottom: 19.6mm, left: 16.9mm, right: 16.9mm),
  footer: context align(right)[
    #text(size: 9pt)[Side #counter(page).display() av #counter(page).final().at(0)]
  ],
)

#let address-block(person) = [
  #person.at("navn", default: "")
  #if person.at("adresse", default: "") != "" [
    #linebreak()
    #person.at("adresse")
  ]
  #if person.at("postnummer", default: "") != "" or person.at("poststed", default: "") != "" [
    #linebreak()
    #person.at("postnummer", default: "") #person.at("poststed", default: "")
  ]
]

#grid(
  columns: (1fr, auto),
  align: (left, top),
  image("/resources/NAVLogoRed.png", width: 22mm, alt: "Nav"),
  align(right)[
    #text(size: 9pt)[
      #sender.at("navn", default: "Nav")
      #if sender.at("adresse", default: "") != "" [#linebreak() #sender.at("adresse")]
      #if sender.at("postnummer", default: "") != "" or sender.at("poststed", default: "") != "" [
        #linebreak()
        #sender.at("postnummer", default: "") #sender.at("poststed", default: "")
      ]
    ]
  ],
)

#v(16pt)

#grid(
  columns: (1fr, auto),
  align: (left, top),
  [#address-block(recipient)],
  align(right)[
    #text(size: 10pt)[Dato: #data.at("dato", default: "")]
  ],
)

#v(32pt)

= #data.at("tittel", default: "Brev fra Nav")

#if data.at("saksnummer", default: "") != "" [
  #text(size: 10pt)[Saksnummer: #data.at("saksnummer")]
  #v(16pt)
]

#for paragraph in paragraphs [
  #paragraph
  #v(16pt)
]

#if references.len() > 0 [
  == Dette bygger vi på
  #list(..references.map(reference => [#reference]))
]

#v(16pt)

Vennlig hilsen

#v(16pt)

#sender.at("saksbehandler", default: "")
#if sender.at("enhet", default: "") != "" [
  #linebreak()
  #sender.at("enhet")
]

#if contact.len() > 0 [
  #v(32pt)
  == Kontakt oss
  #if contact.at("telefon", default: "") != "" [
    Telefon: #contact.at("telefon")
    #linebreak()
  ]
  #if contact.at("aapningstid", default: "") != "" [
    Åpningstid: #contact.at("aapningstid")
    #linebreak()
  ]
  #if contact.at("nettside", default: "") != "" [
    #contact.at("nettside")
  ]
]