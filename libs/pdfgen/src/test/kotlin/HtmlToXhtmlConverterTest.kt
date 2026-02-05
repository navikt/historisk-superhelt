import no.nav.historisk.pdfgen.htmlToXhtml
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

}
