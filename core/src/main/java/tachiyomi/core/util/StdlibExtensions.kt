package tachiyomi.core.util

fun <T> List<T>.replace(position: Int, with: T): List<T> {
  val newList = toMutableList()
  newList[position] = with
  return newList
}

fun <T> List<T>.replaceFirst(predicate: (T) -> Boolean, with: T): List<T> {
  forEachIndexed { index, element ->
    if (predicate(element)) {
      return replace(index, with)
    }
  }
  return this
}
