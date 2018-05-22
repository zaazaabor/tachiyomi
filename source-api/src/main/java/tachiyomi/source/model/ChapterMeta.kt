package tachiyomi.source.model

data class ChapterMeta(
  var key: String,
  var name: String,
  var dateUpload: Long,
  var number: Float = -1f,
  var scanlator: String = ""
)
