package no.nav.historisk.mock

import java.io.InputStream

fun classpathAsString(path: String): String =
   X::class.java.getResource(path)?.readText()
      ?: throw kotlin.IllegalArgumentException("Classpath resource not found: $path")

fun classpathAsStream(path: String): InputStream =
   X::class.java.getResourceAsStream(path)
      ?: throw kotlin.IllegalArgumentException("Classpath resource not found: $path")


private object X
