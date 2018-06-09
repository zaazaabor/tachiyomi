package tachiyomi.source.model

data class SearchQuery(
  val sort: Sorting?,
  val query: String,
  val filters: FilterList
)
