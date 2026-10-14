// PDF/UA krever første overskrift på nivå 1 og tillater ikke hopp til dypere nivåer.
#let normaliser-overskrifter(body, nivåer: ()) = {
  if body.func() == heading {
    let original = body.at("level", default: auto)
    if original == auto {
      original = body.at("depth", default: 1) + body.at("offset", default: 0)
    }
    // Behold bare overordnede nivåer; stakkens dybde gir et sammenhengende hierarki.
    nivåer = nivåer.filter(nivå => nivå < original) + (original,)
    let felter = body.fields()
    let innhold = felter.remove("body")
    felter.level = nivåer.len()
    // Depth beholder originalnivået for de visuelle stilene.
    felter.depth = original
    felter.offset = 0
    (heading(innhold, ..felter), nivåer)
  } else if body.func() == [].func() {
    let innhold = []
    for child in body.children {
      let (normalisert, neste) = normaliser-overskrifter(child, nivåer: nivåer)
      innhold += normalisert
      nivåer = neste
    }
    (innhold, nivåer)
  } else {
    (body, nivåer)
  }
}
