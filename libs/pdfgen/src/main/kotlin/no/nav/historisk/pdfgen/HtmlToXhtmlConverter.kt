package no.nav.historisk.pdfgen

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Entities

fun htmlToXhtml(html: String): String {
    val document: Document = Jsoup.parse(html)
    document
        .outputSettings()
        .syntax(Document.OutputSettings.Syntax.xml)
        .escapeMode(Entities.EscapeMode.xhtml)
        .prettyPrint(false)
    return document.outerHtml()
}

