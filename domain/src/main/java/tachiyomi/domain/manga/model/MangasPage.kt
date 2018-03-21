package tachiyomi.domain.manga.model

data class MangasPage(
  val mangas: List<Manga>,
  val hasNextPage: Boolean
)
