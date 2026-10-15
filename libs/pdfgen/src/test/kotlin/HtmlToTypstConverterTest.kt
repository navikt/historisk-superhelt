package no.nav.historisk.pdfgen

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class HtmlToTypstConverterTest {

    @ParameterizedTest
    @ValueSource(
        strings = ["\\", "#", "*", "_", "`", "$", "@", "<", ">", "[", "]", "{", "}", "=", "+", "-", "/", "~", ":", ".", "\"", "'"]
    )
    fun `Typst-tegn i fritekst escapes etter HTML-dekoding`(char: String) {
        val encoded = char.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

        assertThat(htmlToTypst("<p>$encoded</p>")).isEqualTo("#par[\\$char]")
    }

    @Test
    fun `Typst-kode i fritekst forblir tekst inne i HTML-formatering`() {
        val html = """<h2>= Tittel</h2><p><mark><strong>#panic("test")</strong></mark> ${'$'}x${'$'} [tekst]</p>"""
        val expected = """
            #heading(depth: 2)[\= Tittel]
            #par[#highlight[#strong[\#panic(\"test\")]] \${'$'}x\${'$'} \[tekst\]]
        """.trimIndent()

        assertThat(htmlToTypst(html)).isEqualTo(expected)
    }

    @Test
    fun `fritekst kan ikke avslutte formateringsblokker`() {
        val html = """<p><mark>]\#panic("test")[</mark><a href="https://nav.no">]#panic("test")[</a></p>"""

        assertThat(htmlToTypst(html))
            .isEqualTo("""#par[#highlight[\]\\\#panic(\"test\")\[]#link("https://nav.no")[\]\#panic(\"test\")\[]]""")
    }

    @Test
    fun `Typst-tegn fordelt over HTML-noder escapes hver for seg`() {
        assertThat(htmlToTypst("<p>1<span>.</span> Punkt <span>#</span>panic(&quot;test&quot;)</p>"))
            .isEqualTo("""#par[1\. Punkt \#panic(\"test\")]""")
    }

    @Test
    fun `kommentarer og formatering skrevet som tekst escapes`() {
        val html = """<p>// /* */ *fet* _kursiv_ `kode` -- --- ... \</p>"""

        assertThat(htmlToTypst(html))
            .isEqualTo("""#par[\/\/ \/\* \*\/ \*fet\* \_kursiv\_ \`kode\` \-\- \-\-\- \.\.\. \\]""")
    }

    @Test
    fun `liste og referanse skrevet som tekst blir ikke Typst-struktur`() {
        assertThat(htmlToTypst("<p>1. Punkt</p><p>- Punkt</p><p>@ref &lt;etikett&gt;</p>"))
            .isEqualTo("#par[1\\. Punkt]#par[\\- Punkt]#par[\\@ref \\<etikett\\>]")
    }

    @Test
    fun `lenkeadresse kan ikke avslutte Typst-strengen`() {
        val html = """<p><a href="https://example.com/&quot;); panic(&quot;test&quot;); //\sti">Test</a></p>"""

        assertThat(htmlToTypst(html))
            .isEqualTo("""#par[#link("https://example.com/\"); panic(\"test\"); //\\sti")[Test]]""")
    }

    @Test
    fun `kontrolltegn i lenkeadresse escapes som Typst-kodepunkter`() {
        val html = "<p><a href=\"https://example.com/\n\r\t\">Test</a></p>"

        assertThat(htmlToTypst(html))
            .isEqualTo("""#par[#link("https://example.com/\u{a}\u{d}\u{9}")[Test]]""")
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " \n "])
    fun `tom input gir tom Typst-tekst`(html: String) {
        assertThat(htmlToTypst(html)).isEmpty()
    }

    @ParameterizedTest
    @ValueSource(strings = ["<p></p>", " \n<p></p>\n "])
    fun `ett tomt avsnitt beholdes`(html: String) {
        assertThat(htmlToTypst(html)).isEqualTo("#par[]")
    }

    @ParameterizedTest
    @CsvSource(
        delimiter = '|',
        value = [
            "<p>A</p><p></p><p>B</p>|#par[A]#par[]#par[B]",
            "<p></p><p>A</p>|#par[]#par[A]",
            "<p>A</p><p></p>|#par[A]#par[]",
            "<p></p><p></p>|#par[]#par[]",
            "<p>A</p><p></p><p></p><p>B</p>|#par[A]#par[]#par[]#par[B]",
        ]
    )
    fun `tomme avsnitt beholdes sammen med andre avsnitt`(html: String, expected: String) {
        assertThat(htmlToTypst(html)).isEqualTo(expected)
    }

    @Test
    fun `tomme avsnitt i listepunkter beholdes`() {
        assertThat(htmlToTypst("<ul><li><p>A</p><p></p><p>B</p></li><li><p></p></li></ul>"))
            .isEqualTo("#list(tight: true,\n[#par[A]#par[]#par[B]],\n[#par[]],\n)")
    }

    @Test
    fun `avsnitt bruker eksplisitte par-kall uten tomme skillelinjer`() {
        val html = "<p>Vi viser til søknaden din.</p><p>Du får dekket utgiftene.</p>"

        assertThat(htmlToTypst(html))
            .isEqualTo("#par[Vi viser til søknaden din\\.]#par[Du får dekket utgiftene\\.]")
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3, 4])
    fun `overskrifter fra verktøylinjen beholder nivået`(level: Int) {
        val html = "<h$level>Begrunnelse</h$level><p>Du oppfyller vilkårene.</p>"

        assertThat(htmlToTypst(html))
            .isEqualTo("#heading(depth: $level)[Begrunnelse]\n#par[Du oppfyller vilkårene\\.]")
    }

    @Test
    fun `fet og kursiv tekst konverteres inne i et avsnitt`() {
        val html = "<p>Dette er <strong>viktig</strong> og <em>utdypende</em> tekst.</p>"

        assertThat(htmlToTypst(html))
            .isEqualTo("#par[Dette er #strong[viktig] og #emph[utdypende] tekst\\.]")
    }

    @Test
    fun `tekst kan være både fet og kursiv`() {
        val html = "<p><strong><em>Viktig informasjon</em></strong></p>"

        assertThat(htmlToTypst(html)).isEqualTo("#par[#strong[#emph[Viktig informasjon]]]")
    }

    @Test
    fun `formatering i overskrifter beholdes`() {
        val html = "<h2>Din <em>rett til støtte</em></h2>"

        assertThat(htmlToTypst(html)).isEqualTo("#heading(depth: 2)[Din #emph[rett til støtte]]")
    }

    @Test
    fun `lenker med Tiptap-attributter konverteres til Typst-lenker`() {
        val html =
            """<p>Les mer på <a target="_blank" rel="noopener noreferrer nofollow" href="https://www.nav.no/helse">nav.no</a>.</p>"""

        assertThat(htmlToTypst(html))
            .isEqualTo("""#par[Les mer på #link("https://www.nav.no/helse")[nav\.no]\.]""")
    }

    @Test
    fun `formatering inne i lenker beholdes`() {
        val html = """<p><a href="https://www.nav.no"><strong>Les mer</strong></a></p>"""

        assertThat(htmlToTypst(html))
            .isEqualTo("""#par[#link("https://www.nav.no")[#strong[Les mer]]]""")
    }

    @Test
    fun `HTML-entiteter dekodes i tekst og lenker`() {
        val html =
            """<p>Vilkår &amp; rettigheter: <a href="https://www.nav.no/sok?side=1&amp;antall=10">Søk &amp; finn</a></p>"""

        assertThat(htmlToTypst(html))
            .isEqualTo("""#par[Vilkår & rettigheter\: #link("https://www.nav.no/sok?side=1&antall=10")[Søk & finn]]""")
    }

    @Test
    fun `markering fra Highlight og WordHighlight beholdes med nestet formatering`() {
        val html = "<p>Dette er <mark><strong>viktig</strong> informasjon</mark> til deg.</p>"

        assertThat(htmlToTypst(html)).isEqualTo("#par[Dette er #highlight[#strong[viktig] informasjon] til deg\\.]")
    }

    @Test
    fun `markering bruker standardfarge uten å ta med HTML-attributter`() {
        val html = """<p><mark data-color="#ff0000" style="background-color: red">Viktig</mark></p>"""

        assertThat(htmlToTypst(html)).isEqualTo("#par[#highlight[Viktig]]")
    }

    @Test
    fun `markering i listepunkter beholder lenker og linjeskift`() {
        val html =
            """<ul><li><p><mark><a href="https://www.nav.no">Les mer</a><br><em>Frist</em></mark></p></li></ul>"""

        assertThat(htmlToTypst(html))
            .isEqualTo("#list(tight: true,\n[#par[#highlight[#link(\"https://www.nav.no\")[Les mer]#linebreak()#emph[Frist]]]],\n)")
    }

    @ParameterizedTest
    @ValueSource(strings = ["span", "div", "custom-element"])
    fun `ukjente tagger beholder tekst og formatering uten å ta med attributter`(tag: String) {
        val html = """<$tag class="innhold" style="color: red">Dette er <strong>viktig</strong> og <em>nyttig</em>.</$tag>"""

        assertThat(htmlToTypst(html)).isEqualTo("Dette er #strong[viktig] og #emph[nyttig]\\.")
    }

    @Test
    fun `span inne i avsnitt beholder mellomrom rundt teksten`() {
        val html = "<p>Tekst før <span>innhold</span> og etter.</p>"

        assertThat(htmlToTypst(html)).isEqualTo("#par[Tekst før innhold og etter\\.]")
    }

    @Test
    fun `nestede div og span beholder avsnitt og lenker`() {
        val html =
            """<div><div><p><span>Første <strong>avsnitt</strong></span></p></div><p><span><a href="https://www.nav.no">Les mer</a></span></p></div>"""

        assertThat(htmlToTypst(html))
            .isEqualTo("#par[Første #strong[avsnitt]]#par[#link(\"https://www.nav.no\")[Les mer]]")
    }

    @ParameterizedTest
    @ValueSource(strings = ["span", "div", "custom-element"])
    fun `tomme ukjente tagger gir ikke ekstra tekst eller linjeskift`(tag: String) {
        val html = "<div>Før<$tag></$tag> etter</div>"

        assertThat(htmlToTypst(html)).isEqualTo("Før etter")
    }

    @ParameterizedTest
    @ValueSource(strings = ["<br>", "<br/>", "<br />"])
    fun `linjeskift fra Shift Enter beholdes`(br: String) {
        val html = "<p>Første linje${br}Andre linje</p>"

        assertThat(htmlToTypst(html)).isEqualTo("#par[Første linje#linebreak()Andre linje]")
    }

    @Test
    fun `flere linjeskift etter hverandre beholdes`() {
        val html = "<p>Første linje<br><br>Etter tom linje</p>"

        assertThat(htmlToTypst(html)).isEqualTo("#par[Første linje#linebreak()#linebreak()Etter tom linje]")
    }

    @Test
    fun `punktliste med avsnitt i listepunktene gir sammenhengende Typst-liste`() {
        val html = "<ul><li><p>Første punkt</p></li><li><p>Andre punkt</p></li></ul>"

        assertThat(htmlToTypst(html)).isEqualTo("#list(tight: true,\n[#par[Første punkt]],\n[#par[Andre punkt]],\n)")
    }

    @Test
    fun `nummerert liste med avsnitt i listepunktene gir sammenhengende Typst-liste`() {
        val html = "<ol><li><p>Første punkt</p></li><li><p>Andre punkt</p></li></ol>"

        assertThat(htmlToTypst(html)).isEqualTo("#enum(tight: true,\n[#par[Første punkt]],\n[#par[Andre punkt]],\n)")
    }

    @Test
    fun `formatering i listepunkter beholdes`() {
        val html = "<ul><li><p><strong>Viktig</strong> punkt</p></li><li><p><em>Mer informasjon</em></p></li></ul>"

        assertThat(htmlToTypst(html)).isEqualTo("#list(tight: true,\n[#par[#strong[Viktig] punkt]],\n[#par[#emph[Mer informasjon]]],\n)")
    }

    @Test
    fun `nestede lister beholder hierarkiet`() {
        val html =
            "<ul><li><p>Hovedpunkt</p><ol><li><p>Underpunkt</p></li></ol></li><li><p>Neste hovedpunkt</p></li></ul>"

        assertThat(htmlToTypst(html))
            .isEqualTo("#list(tight: true,\n[#par[Hovedpunkt]\n#enum(tight: true,\n[#par[Underpunkt]],\n)\n],\n[#par[Neste hovedpunkt]],\n)")
    }

    @Test
    fun `liste mellom avsnitt beholder skillet til teksten rundt`() {
        val html = "<p>Du må sende inn:</p><ul><li><p>Kvittering</p></li></ul><p>Fristen er fire uker.</p>"

        assertThat(htmlToTypst(html))
            .isEqualTo("#par[Du må sende inn\\:]\n\n#list(tight: true,\n[#par[Kvittering]],\n)\n#par[Fristen er fire uker\\.]")
    }

    @Test
    fun `linjeskift beholdes inne i listepunktet`() {
        val html = "<ul><li><p>Første linje<br>Andre linje</p></li><li><p>Neste punkt</p></li></ul>"

        assertThat(htmlToTypst(html))
            .isEqualTo("#list(tight: true,\n[#par[Første linje#linebreak()Andre linje]],\n[#par[Neste punkt]],\n)")
    }

    @Test
    fun `flere avsnitt beholdes inne i samme listepunkt`() {
        val html = "<ul><li><p>Første avsnitt</p><p>Andre avsnitt</p></li><li><p>Neste punkt</p></li></ul>"

        assertThat(htmlToTypst(html))
            .isEqualTo("#list(tight: true,\n[#par[Første avsnitt]#par[Andre avsnitt]],\n[#par[Neste punkt]],\n)")
    }

    @Test
    fun `tekst etter en nestet liste tilhører fortsatt hovedpunktet`() {
        val html = "<ul><li><p>Hovedpunkt</p><ul><li><p>Underpunkt</p></li></ul><p>Fortsettelse</p></li></ul>"

        assertThat(htmlToTypst(html))
            .isEqualTo("#list(tight: true,\n[#par[Hovedpunkt]\n#list(tight: true,\n[#par[Underpunkt]],\n)\n#par[Fortsettelse]],\n)")
    }

    @Test
    fun `lister med tre nivåer beholder strukturen på hvert nivå`() {
        val html = "<ol><li><p>Nivå en</p><ul><li><p>Nivå to</p><ol><li><p>Nivå tre</p></li></ol></li></ul></li></ol>"

        assertThat(htmlToTypst(html))
            .isEqualTo("#enum(tight: true,\n[#par[Nivå en]\n#list(tight: true,\n[#par[Nivå to]\n#enum(tight: true,\n[#par[Nivå tre]],\n)\n],\n)\n],\n)")
    }

    @Test
    fun `separate lister forblir separate uten mellomliggende avsnitt`() {
        assertThat(htmlToTypst("<ul><li><p>A</p></li></ul><ul><li><p>B</p></li></ul>"))
            .isEqualTo("#list(tight: true,\n[#par[A]],\n)\n\n\n#list(tight: true,\n[#par[B]],\n)")
    }

    @Test
    fun `formatering midt i et ord bruker eksplisitte funksjoner`() {
        assertThat(htmlToTypst("<p>før<strong>fet</strong>etter<em>kursiv</em>slutt</p>"))
            .isEqualTo("#par[før#strong[fet]etter#emph[kursiv]slutt]")
    }
}
