package tachiyomi.domain.source

/**
 * Model for a manga given by a source
 */
data class SManga(
  val url: String,
  val title: String,
  val artist: String,
  val author: String,
  val description: String,
  val genre: String,
  val status: Int,
  val cover: String,
  val initialized: Boolean
) {

  companion object {
    const val UNKNOWN = 0
    const val ONGOING = 1
    const val COMPLETED = 2
    const val LICENSED = 3
  }
}
