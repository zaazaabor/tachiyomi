package tachiyomi.domain.chapter.model

data class Chapter(
  val id: Long = -1,
  val mangaId: Long = -1,
  val key: String,
  val name: String,
  val read: Boolean = false,
  val bookmark: Boolean = false,
  val progress: Int = 0,
  val dateUpload: Long = 0,
  val dateFetch: Long = 0,
  val sourceOrder: Int = 0,
  val number: Float = -1f,
  val scanlator: String = ""
) {

  val isRecognizedNumber get() = number >= 0

}
