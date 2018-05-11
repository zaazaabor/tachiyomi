package tachiyomi.domain.chapter.model

data class Chapter(
  val id: Long = -1,
  val mangaId: Long = -1,
  val read: Boolean = false,
  val bookmark: Boolean = false,
  val progress: Int = 0,
  val dateFetch: Long = 0,
  val sourceOrder: Int = 0
)
