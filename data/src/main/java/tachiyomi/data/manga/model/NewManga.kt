package tachiyomi.data.manga.model

/**
 * Model for a manga that is about to be inserted in DB
 */
internal data class NewManga(
  val source: Long,
  val url: String,
  val title: String,
  val artist: String,
  val author: String,
  val description: String,
  val genre: String,
  val status: Int,
  val cover: String,
  val favorite: Boolean,
  val lastUpdate: Long,
  val initialized: Boolean,
  val viewer: Int,
  val flags: Int
)
