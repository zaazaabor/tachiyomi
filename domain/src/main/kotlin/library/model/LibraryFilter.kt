package tachiyomi.domain.library.model

enum class LibraryFilter {
  Downloaded,
  Unread,
  Completed;

  companion object
}

fun LibraryFilter.serialize(): String {
  return when (this) {
    LibraryFilter.Downloaded -> "Downloaded"
    LibraryFilter.Unread -> "Unread"
    LibraryFilter.Completed -> "Completed"
  }
}

fun LibraryFilter.Companion.deserialize(serialized: String): LibraryFilter? {
  return when (serialized) {
    "Downloaded" -> LibraryFilter.Downloaded
    "Unread" -> LibraryFilter.Unread
    "Completed" -> LibraryFilter.Completed
    else -> null
  }
}

fun List<LibraryFilter>.serialize(): String {
  return joinToString(";") { it.serialize() }
}

fun LibraryFilter.Companion.deserializeList(serialized: String): List<LibraryFilter> {
  return serialized.split(";").mapNotNull { LibraryFilter.deserialize(it) }
}
