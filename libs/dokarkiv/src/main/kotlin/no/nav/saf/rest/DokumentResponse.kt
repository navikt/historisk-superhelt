package no.nav.saf.rest

import org.springframework.http.MediaType

data class DokumentResponse(
   val data: ByteArray,
   val contentType: MediaType,
   val fileName: String?,
   val contentLength: Long,
) {
   override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      other as DokumentResponse

      if (contentLength != other.contentLength) return false
      if (!data.contentEquals(other.data)) return false
      if (contentType != other.contentType) return false
      if (fileName != other.fileName) return false

      return true
   }

   override fun hashCode(): Int {
      var result = contentLength.hashCode()
      result = 31 * result + data.contentHashCode()
      result = 31 * result + (contentType?.hashCode() ?: 0)
      result = 31 * result + (fileName?.hashCode() ?: 0)
      return result
   }
}
