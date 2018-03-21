package tachiyomi.domain.source.model

data class SMangasPage(
  val mangas: List<SManga>,
  val hasNextPage: Boolean
)
