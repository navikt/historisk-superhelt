package no.nav.historisk.pdfgen

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class HtmlToXhtmlConverterTest {

    @Test
    fun `konverter HTML til XHTML`() {
        val html =
            "<p>Viser til Satser - nav.no.<br><br>Vedtaket er gjort etter folketrygdloven paragraf 10-7i med tilhørende rundskriv.<br><br>Kopi er sendt til samhandler</p>"
        val expectedXhtml =
            "<html><head></head><body><p>Viser til Satser - nav.no.<br /><br />Vedtaket er gjort etter folketrygdloven paragraf 10-7i med tilhørende rundskriv.<br /><br />Kopi er sendt til samhandler</p></body></html>"

        val xhtml = htmlToXhtml(html)
        assertEquals(expectedXhtml, xhtml)
    }

    @Test
    fun `konverter tom streng til XHTML`() {
        val html = ""
        val expectedXhtml = "<html><head></head><body></body></html>"

        val xhtml = htmlToXhtml(html)
        assertEquals(expectedXhtml, xhtml)
    }

    @Test
    fun `konverter whitespace-only streng til XHTML`() {
        val html = "   \n\t  "
        val expectedXhtml = "<html><head></head><body></body></html>"

        val xhtml = htmlToXhtml(html)
        assertEquals(expectedXhtml, xhtml)
    }

    @Test
    fun `konverter HTML med spesialtegn som må escapes`() {
        val html = "<p>Test med &, <, >, og \"quotes\"</p>"
        val expectedXhtml = "<html><head></head><body><p>Test med &amp;, &lt;, &gt;, og \"quotes\"</p></body></html>"

        val xhtml = htmlToXhtml(html)
        assertEquals(expectedXhtml, xhtml)
    }

    @Test
    fun `konverter HTML med eksisterende XHTML self-closing tags`() {
        val html = "<p>Line one<br />Line two<hr />End</p>"
        val expectedXhtml = "<html><head></head><body><p>Line one<br />Line two</p><hr />End<p></p></body></html>"

        val xhtml = htmlToXhtml(html)
        assertEquals(expectedXhtml, xhtml)
    }

    @Test
    fun `konverter malformed HTML med manglende closing tags`() {
        val html = "<p>Paragraph without closing<div>Div without closing"
        val expectedXhtml = "<html><head></head><body><p>Paragraph without closing</p><div>Div without closing</div></body></html>"

        val xhtml = htmlToXhtml(html)
        assertEquals(expectedXhtml, xhtml)
    }

    @Test
    fun `konverter HTML med nested tags i feil rekkefølge`() {
        val html = "<b><i>text</b></i>"
        val expectedXhtml = "<html><head></head><body><b><i>text</i></b></body></html>"

        val xhtml = htmlToXhtml(html)
        assertEquals(expectedXhtml, xhtml)
    }

    @Test
    fun `konverter HTML med HTML entities`() {
        val html = "<p>&nbsp;&copy;&reg;&euro;</p>"
        val expectedXhtml = "<html><head></head><body><p>&#xa0;©®€</p></body></html>"

        val xhtml = htmlToXhtml(html)
        assertEquals(expectedXhtml, xhtml)
    }

    @Test
    fun `konverter HTML med unicode tegn`() {
        val html = "<p>æøå ÆØÅ émoji: 😀</p>"
        val expectedXhtml = "<html><head></head><body><p>æøå ÆØÅ émoji: \uD83D\uDE00</p></body></html>"

        val xhtml = htmlToXhtml(html)
        assertEquals(expectedXhtml, xhtml)
    }

    @Test
    fun `konverter HTML med attributter som inneholder spesialtegn`() {
        val html = "<a href=\"http://example.com?foo=bar&baz=qux\" title=\"Link med &amp; og quotes\">Link</a>"
        val expectedXhtml = "<html><head></head><body><a href=\"http://example.com?foo=bar&amp;baz=qux\" title=\"Link med &amp; og quotes\">Link</a></body></html>"

        val xhtml = htmlToXhtml(html)
        assertEquals(expectedXhtml, xhtml)
    }


    @Test
    fun `konverter HTML med kommentarer`() {
        val html = "<p>Text<!-- This is a comment -->More text</p>"
        val expectedXhtml = "<html><head></head><body><p>Text<!-- This is a comment -->More text</p></body></html>"

        val xhtml = htmlToXhtml(html)
        assertEquals(expectedXhtml, xhtml)
    }

    @Test
    fun `konverter kun tekst uten HTML tags`() {
        val html = "Just plain text with no tags"
        val expectedXhtml = "<html><head></head><body>Just plain text with no tags</body></html>"

        val xhtml = htmlToXhtml(html)
        assertEquals(expectedXhtml, xhtml)
    }

    @Test
    fun `konverter HTML med multiple br tags`() {
        val html = "<p>Line 1<br><br><br>Line 2</p>"
        val expectedXhtml = "<html><head></head><body><p>Line 1<br /><br /><br />Line 2</p></body></html>"

        val xhtml = htmlToXhtml(html)
        assertEquals(expectedXhtml, xhtml)
    }

    @Test
    fun `konverter HTML med img tag`() {
        val html = "<img src=\"image.jpg\" alt=\"Description\">"
        val expectedXhtml = "<html><head></head><body><img src=\"image.jpg\" alt=\"Description\" /></body></html>"

        val xhtml = htmlToXhtml(html)
        assertEquals(expectedXhtml, xhtml)
    }
}
