package no.nav.historisk.pdfgen

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

fun htmlToTypst(html: String): String {
    val doc = Jsoup.parseBodyFragment(html)
    fun convertNode(element: Element): String = element.childNodes().joinToString("") { node ->
        when (node) {
            is TextNode -> escapeTypstMarkup(node.text())
            is Element -> {
                val content = convertNode(node)
                val listPrefix = if (element.tagName() == "li") "\n" else "\n\n"
                when (node.tagName()) {
                    "h1", "h2", "h3", "h4" -> "\n#heading(depth: ${node.tagName().last()})[$content]\n"
                    "p" -> if (node.childNodeSize() == 0) "" else "#par[$content]"
                    "br" -> "#linebreak()"
                    "mark" -> "#highlight[$content]"
                    "b", "strong" -> "#strong[$content]"
                    "i", "em" -> "#emph[$content]"
                    "ul" -> "${listPrefix}#list(tight: true,$content\n)\n"
                    "ol" -> "${listPrefix}#enum(tight: true,$content\n)\n"
                    "li" -> "\n[$content],"
                    "a" -> "#link(\"${escapeTypstString(node.attr("href"))}\")[$content]"
                    else -> content
                }
            }
            else -> ""
        }
    }

    return convertNode(doc.body()).trim()
}

private fun escapeTypstMarkup(text: String): String = buildString {
    for (char in text) {
        if (char in "\\#*_`$@<>[]{}=+-/~:.\"'") append('\\')
        append(char)
    }
}

private fun escapeTypstString(text: String): String = buildString {
    for (char in text) {
        when (char) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            in '\u0000'..'\u001f' -> append("\\u{${char.code.toString(16)}}")
            else -> append(char)
        }
    }
}
