package no.nav.saf.graphql

import com.fasterxml.jackson.annotation.JsonIgnoreProperties


data class GraphqlQuery<T>(
   val query: String,
   val variables: T,
)

fun <T> createGraphqlQuery(
   gqlFile: String,
   variables: T,
): GraphqlQuery<T> {
   val query =
      GraphqlQuery::class.java
         .getResource(gqlFile)!!
         .readText()
         .replace("[\n\r]", "")
   return GraphqlQuery<T>(query, variables)
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class GraphqlError(
   val message: String,
   val extensions: ErrorExtensions?,
)

data class ErrorExtensions(
   val code: String?,
   val classification: String?,
)

enum class ErrorCode {
   NOT_FOUND,
   FORBIDDEN,
   BAD_REQUEST,
   SERVER_ERROR,
}


