package tachiyomi.domain.library.model

data class LibraryManga(
  val mangaId: Long,
  val source: Long,
  val key: String,
  val title: String,
  val status: Int,
  val cover: String,
  val lastUpdate: Long = 0,
  val category: Long,
  val unread: Int
)
