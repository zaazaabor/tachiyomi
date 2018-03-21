package tachiyomi.domain.manga.model

import tachiyomi.domain.source.model.SManga

data class Manga(
  val id: Long = -1,
  val source: Long,
  val key: String,
  val title: String,
  val artist: String = "",
  val author: String = "",
  val description: String = "",
  val genres: String = "",
  val status: Int = SManga.UNKNOWN,
  val cover: String = "",
  val favorite: Boolean = false,
  val lastUpdate: Long = 0,
  val initialized: Boolean = false,
  val viewer: Int = 0,
  val flags: Int = 0
)
