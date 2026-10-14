#import "/resources/lib/pdf-ua.typ": normaliser-overskrifter

#let kontroller(originale, forventede) = {
  let innhold = originale.map(nivå => heading(depth: nivå)[Overskrift]).join()
  let (normalisert, _) = normaliser-overskrifter(innhold)
  let overskrifter = if normalisert.func() == heading { (normalisert,) } else { normalisert.children }
  assert.eq(overskrifter.map(it => it.level), forventede)
  assert.eq(overskrifter.map(it => it.depth), originale)
}

#kontroller((2, 3, 2), (1, 2, 1))
#kontroller((1, 3, 4, 2), (1, 2, 3, 2))
#kontroller((2, 2, 2), (1, 1, 1))
#kontroller((1, 2, 3, 2), (1, 2, 3, 2))
#kontroller((4, 3, 2, 1), (1, 1, 1, 1))
#kontroller((3, 4, 4, 3, 4), (1, 2, 2, 1, 2))

#let (tomt, tomme-nivåer) = normaliser-overskrifter([])
#assert.eq(tomt, [])
#assert.eq(tomme-nivåer, ())

#let (tekst, tekst-nivåer) = normaliser-overskrifter([Vanlig tekst])
#assert.eq(tekst, [Vanlig tekst])
#assert.eq(tekst-nivåer, ())

#let (første, nivåer) = normaliser-overskrifter([= Tittel])
#let (standard, _) = normaliser-overskrifter([== Rettigheter], nivåer: nivåer)
#assert.eq(første.level, 1)
#assert.eq(standard.level, 2)
#assert.eq(standard.depth, 2)

#let (formatering, _) = normaliser-overskrifter([=== *Fet* _kursiv_ #highlight[markert] #link("https://www.nav.no")[lenke]])
#assert.eq(formatering.level, 1)
#assert.eq(formatering.depth, 3)
#assert.eq(formatering.body, [*Fet* _kursiv_ #highlight[markert] #link("https://www.nav.no")[lenke]])

#let (eksplisitt, _) = normaliser-overskrifter(heading(level: 4)[Eksplisitt nivå])
#assert.eq(eksplisitt.level, 1)
#assert.eq(eksplisitt.depth, 4)

#let (forskjøvet, _) = normaliser-overskrifter(heading(depth: 1, offset: 2)[Forskjøvet nivå])
#assert.eq(forskjøvet.level, 1)
#assert.eq(forskjøvet.depth, 3)
