package no.nav.historisk.pdfgen

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

fun htmlToTypst(html: String): String {
    val doc = Jsoup.parseBodyFragment(html)
    val builder = StringBuilder()

    fun convertNode(element: Element) {
        for (node in element.childNodes()) {
            when (node) {
                is TextNode -> builder.append(escapeTypstMarkup(node.text()))
                is Element -> {
                    when (node.tagName()) {
                        "h1" -> {
                            builder.append("\n= ")
                            convertNode(node)
                            builder.append("\n")
                        }

                        "h2" -> {
                            builder.append("\n== ")
                            convertNode(node)
                            builder.append("\n")
                        }

                        "h3" -> {
                            builder.append("\n=== ")
                            convertNode(node)
                            builder.append("\n")
                        }

                        "h4" -> {
                            builder.append("\n==== ")
                            convertNode(node)
                            builder.append("\n")
                        }

                        "p" -> {
                            if (node.parent()?.tagName() == "li") {
                                if (node.previousElementSibling() != null) builder.append("\n\n")
                                convertNode(node)
                            } else {
                                builder.append("\n")
                                convertNode(node)
                                builder.append("\n")
                            }
                        }

                        "br" -> builder.append("\\\n")

                        "mark" -> {
                            builder.append("#highlight[")
                            convertNode(node)
                            builder.append("]")
                        }

                        "b", "strong" -> {
                            builder.append("*")
                            convertNode(node)
                            builder.append("*")
                        }

                        "i", "em" -> {
                            builder.append("_")
                            convertNode(node)
                            builder.append("_")
                        }

                        "ul", "ol" -> {
                            convertNode(node)
                            if (node.parent()?.tagName() != "li") builder.append("\n")
                        }

                        "li" -> {
                            val marker = if (node.parent()?.tagName() == "ol") "+" else "-"
                            val start = builder.length
                            convertNode(node)
                            val content = builder.substring(start).trim()
                            builder.setLength(start)
                            builder.append("\n$marker ")
                            builder.append(content.replace("\n", "\n  "))
                        }

                        "a" -> {
                            val href = escapeTypstString(node.attr("href"))
                            builder.append("#link(\"$href\")[")
                            convertNode(node)
                            builder.append("]")
                        }

                        else -> convertNode(node) // Fallback for container elements like div/span
                    }
                }
            }
        }
    }

    convertNode(doc.body())
    return builder.toString().trim()
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
