package no.nav.historisk.mock.pdl

fun classpathString(path: String): String {
    return X::class.java.getResource(path)?.readText()
        ?: throw IllegalArgumentException("Classpath resource not found: $path")
}

private object X