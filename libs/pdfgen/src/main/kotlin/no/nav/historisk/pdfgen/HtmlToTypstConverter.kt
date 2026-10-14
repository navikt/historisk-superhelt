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
                is TextNode -> builder.append(node.text())
                is Element -> {
                    when (node.tagName()) {
                        "h1" -> {
                            builder.append("\n= ")
                            convertNode(node)
                            builder.append("\n")
                        }

                        "h2" -> {
                            builder.append("\n== ");
                            convertNode(node)
                            builder.append("\n")
                        }

                        "h3" -> {
                            builder.append("\n=== ");
                            convertNode(node)
                            builder.append("\n")
                        }

                        "h4" -> {
                            builder.append("\n==== ");
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

                        "b", "strong" -> {
                            builder.append("*");
                            convertNode(node)
                            builder.append("*")
                        }

                        "i", "em" -> {
                            builder.append("_");
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
                            val href = node.attr("href")
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
