package tachiyomi.domain.source.model

data class MangasPageMeta(
  val mangas: List<MangaMeta>,
  val hasNextPage: Boolean
)
