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

        assertThat(htmlToTypst("<p>$encoded</p>")).isEqualTo("\\$char")
    }

    @Test
    fun `Typst-kode i fritekst forblir tekst inne i HTML-formatering`() {
        val html = """<h2>= Tittel</h2><p><mark><strong>#panic("test")</strong></mark> ${'$'}x${'$'} [tekst]</p>"""
        val expected = """
            == \= Tittel

            #highlight[*\#panic(\"test\")*] \${'$'}x\${'$'} \[tekst\]
        """.trimIndent()

        assertThat(htmlToTypst(html)).isEqualTo(expected)
    }

    @Test
    fun `fritekst kan ikke avslutte formateringsblokker`() {
        val html = """<p><mark>]\#panic("test")[</mark><a href="https://nav.no">]#panic("test")[</a></p>"""

        assertThat(htmlToTypst(html))
            .isEqualTo("""#highlight[\]\\\#panic(\"test\")\[]#link("https://nav.no")[\]\#panic(\"test\")\[]""")
    }

    @Test
    fun `Typst-tegn fordelt over HTML-noder escapes hver for seg`() {
        assertThat(htmlToTypst("<p>1<span>.</span> Punkt <span>#</span>panic(&quot;test&quot;)</p>"))
            .isEqualTo("""1\. Punkt \#panic(\"test\")""")
    }

    @Test
    fun `kommentarer og formatering skrevet som tekst escapes`() {
        val html = """<p>// /* */ *fet* _kursiv_ `kode` -- --- ... \</p>"""

        assertThat(htmlToTypst(html))
            .isEqualTo("""\/\/ \/\* \*\/ \*fet\* \_kursiv\_ \`kode\` \-\- \-\-\- \.\.\. \\""")
    }

    @Test
    fun `liste og referanse skrevet som tekst blir ikke Typst-struktur`() {
        assertThat(htmlToTypst("<p>1. Punkt</p><p>- Punkt</p><p>@ref &lt;etikett&gt;</p>"))
            .isEqualTo("1\\. Punkt\n\n\\- Punkt\n\n\\@ref \\<etikett\\>")
    }

    @Test
    fun `lenkeadresse kan ikke avslutte Typst-strengen`() {
        val html = """<p><a href="https://example.com/&quot;); panic(&quot;test&quot;); //\sti">Test</a></p>"""

        assertThat(htmlToTypst(html))
            .isEqualTo("""#link("https://example.com/\"); panic(\"test\"); //\\sti")[Test]""")
    }

    @Test
    fun `kontrolltegn i lenkeadresse escapes som Typst-kodepunkter`() {
        val html = "<p><a href=\"https://example.com/\n\r\t\">Test</a></p>"

        assertThat(htmlToTypst(html))
            .isEqualTo("""#link("https://example.com/\u{a}\u{d}\u{9}")[Test]""")
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "<p></p>"])
    fun `tom editor gir tom Typst-tekst`(html: String) {
        assertThat(htmlToTypst(html)).isEmpty()
    }

    @Test
    fun `avsnitt skilles med en tom linje`() {
        val html = "<p>Vi viser til søknaden din.</p><p>Du får dekket utgiftene.</p>"

        assertThat(htmlToTypst(html))
            .isEqualTo("Vi viser til søknaden din\\.\n\nDu får dekket utgiftene\\.")
    }

    @ParameterizedTest
    @CsvSource("1, =", "2, ==", "3, ===", "4, ====")
    fun `overskrifter fra verktøylinjen beholder nivået`(level: Int, marker: String) {
        val html = "<h$level>Begrunnelse</h$level><p>Du oppfyller vilkårene.</p>"

        assertThat(htmlToTypst(html))
            .isEqualTo("$marker Begrunnelse\n\nDu oppfyller vilkårene\\.")
    }

    @Test
    fun `fet og kursiv tekst konverteres inne i et avsnitt`() {
        val html = "<p>Dette er <strong>viktig</strong> og <em>utdypende</em> tekst.</p>"

        assertThat(htmlToTypst(html))
            .isEqualTo("Dette er *viktig* og _utdypende_ tekst\\.")
    }

    @Test
    fun `tekst kan være både fet og kursiv`() {
        val html = "<p><strong><em>Viktig informasjon</em></strong></p>"

        assertThat(htmlToTypst(html)).isEqualTo("*_Viktig informasjon_*")
    }

    @Test
    fun `formatering i overskrifter beholdes`() {
        val html = "<h2>Din <em>rett til støtte</em></h2>"

        assertThat(htmlToTypst(html)).isEqualTo("== Din _rett til støtte_")
    }

    @Test
    fun `lenker med Tiptap-attributter konverteres til Typst-lenker`() {
        val html =
            """<p>Les mer på <a target="_blank" rel="noopener noreferrer nofollow" href="https://www.nav.no/helse">nav.no</a>.</p>"""

        assertThat(htmlToTypst(html))
            .isEqualTo("""Les mer på #link("https://www.nav.no/helse")[nav\.no]\.""")
    }

    @Test
    fun `formatering inne i lenker beholdes`() {
        val html = """<p><a href="https://www.nav.no"><strong>Les mer</strong></a></p>"""

        assertThat(htmlToTypst(html))
            .isEqualTo("""#link("https://www.nav.no")[*Les mer*]""")
    }

    @Test
    fun `HTML-entiteter dekodes i tekst og lenker`() {
        val html =
            """<p>Vilkår &amp; rettigheter: <a href="https://www.nav.no/sok?side=1&amp;antall=10">Søk &amp; finn</a></p>"""

        assertThat(htmlToTypst(html))
            .isEqualTo("""Vilkår & rettigheter\: #link("https://www.nav.no/sok?side=1&antall=10")[Søk & finn]""")
    }

    @Test
    fun `markering fra Highlight og WordHighlight beholdes med nestet formatering`() {
        val html = "<p>Dette er <mark><strong>viktig</strong> informasjon</mark> til deg.</p>"

        assertThat(htmlToTypst(html)).isEqualTo("Dette er #highlight[*viktig* informasjon] til deg\\.")
    }

    @Test
    fun `markering bruker standardfarge uten å ta med HTML-attributter`() {
        val html = """<p><mark data-color="#ff0000" style="background-color: red">Viktig</mark></p>"""

        assertThat(htmlToTypst(html)).isEqualTo("#highlight[Viktig]")
    }

    @Test
    fun `markering i listepunkter beholder lenker og linjeskift`() {
        val html =
            """<ul><li><p><mark><a href="https://www.nav.no">Les mer</a><br><em>Frist</em></mark></p></li></ul>"""

        assertThat(htmlToTypst(html))
            .isEqualTo("- #highlight[#link(\"https://www.nav.no\")[Les mer]\\\n  _Frist_]")
    }

    @ParameterizedTest
    @ValueSource(strings = ["span", "div", "custom-element"])
    fun `ukjente tagger beholder tekst og formatering uten å ta med attributter`(tag: String) {
        val html = """<$tag class="innhold" style="color: red">Dette er <strong>viktig</strong> og <em>nyttig</em>.</$tag>"""

        assertThat(htmlToTypst(html)).isEqualTo("Dette er *viktig* og _nyttig_\\.")
    }

    @Test
    fun `span inne i avsnitt beholder mellomrom rundt teksten`() {
        val html = "<p>Tekst før <span>innhold</span> og etter.</p>"

        assertThat(htmlToTypst(html)).isEqualTo("Tekst før innhold og etter\\.")
    }

    @Test
    fun `nestede div og span beholder avsnitt og lenker`() {
        val html =
            """<div><div><p><span>Første <strong>avsnitt</strong></span></p></div><p><span><a href="https://www.nav.no">Les mer</a></span></p></div>"""

        assertThat(htmlToTypst(html))
            .isEqualTo("Første *avsnitt*\n\n#link(\"https://www.nav.no\")[Les mer]")
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

        assertThat(htmlToTypst(html)).isEqualTo("Første linje\\\nAndre linje")
    }

    @Test
    fun `flere linjeskift etter hverandre beholdes`() {
        val html = "<p>Første linje<br><br>Etter tom linje</p>"

        assertThat(htmlToTypst(html)).isEqualTo("Første linje\\\n\\\nEtter tom linje")
    }

    @Test
    fun `punktliste med avsnitt i listepunktene gir sammenhengende Typst-liste`() {
        val html = "<ul><li><p>Første punkt</p></li><li><p>Andre punkt</p></li></ul>"

        assertThat(htmlToTypst(html)).isEqualTo("- Første punkt\n- Andre punkt")
    }

    @Test
    fun `nummerert liste med avsnitt i listepunktene gir sammenhengende Typst-liste`() {
        val html = "<ol><li><p>Første punkt</p></li><li><p>Andre punkt</p></li></ol>"

        assertThat(htmlToTypst(html)).isEqualTo("+ Første punkt\n+ Andre punkt")
    }

    @Test
    fun `formatering i listepunkter beholdes`() {
        val html = "<ul><li><p><strong>Viktig</strong> punkt</p></li><li><p><em>Mer informasjon</em></p></li></ul>"

        assertThat(htmlToTypst(html)).isEqualTo("- *Viktig* punkt\n- _Mer informasjon_")
    }

    @Test
    fun `nestede lister beholder hierarkiet`() {
        val html =
            "<ul><li><p>Hovedpunkt</p><ol><li><p>Underpunkt</p></li></ol></li><li><p>Neste hovedpunkt</p></li></ul>"

        assertThat(htmlToTypst(html))
            .isEqualTo("- Hovedpunkt\n  + Underpunkt\n- Neste hovedpunkt")
    }

    @Test
    fun `liste mellom avsnitt beholder skillet til teksten rundt`() {
        val html = "<p>Du må sende inn:</p><ul><li><p>Kvittering</p></li></ul><p>Fristen er fire uker.</p>"

        assertThat(htmlToTypst(html))
            .isEqualTo("Du må sende inn\\:\n\n- Kvittering\n\nFristen er fire uker\\.")
    }

    @Test
    fun `linjeskift i listepunkter beholder innrykket`() {
        val html = "<ul><li><p>Første linje<br>Andre linje</p></li><li><p>Neste punkt</p></li></ul>"

        assertThat(htmlToTypst(html))
            .isEqualTo("- Første linje\\\n  Andre linje\n- Neste punkt")
    }

    @Test
    fun `flere avsnitt i samme listepunkt beholder innrykket`() {
        val html = "<ul><li><p>Første avsnitt</p><p>Andre avsnitt</p></li><li><p>Neste punkt</p></li></ul>"

        assertThat(htmlToTypst(html))
            .isEqualTo("- Første avsnitt\n  \n  Andre avsnitt\n- Neste punkt")
    }

    @Test
    fun `tekst etter en nestet liste tilhører fortsatt hovedpunktet`() {
        val html = "<ul><li><p>Hovedpunkt</p><ul><li><p>Underpunkt</p></li></ul><p>Fortsettelse</p></li></ul>"

        assertThat(htmlToTypst(html))
            .isEqualTo("- Hovedpunkt\n  - Underpunkt\n  \n  Fortsettelse")
    }

    @Test
    fun `lister med tre nivåer beholder innrykket på hvert nivå`() {
        val html = "<ol><li><p>Nivå en</p><ul><li><p>Nivå to</p><ol><li><p>Nivå tre</p></li></ol></li></ul></li></ol>"

        assertThat(htmlToTypst(html))
            .isEqualTo("+ Nivå en\n  - Nivå to\n    + Nivå tre")
    }
}
