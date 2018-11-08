package tachiyomi.core.stdlib

/**
 * Returns a new list that replaces the item at the given [position] with [newItem].
 */
fun <T> List<T>.replace(position: Int, newItem: T): List<T> {
  val newList = toMutableList()
  newList[position] = newItem
  return newList
}

/**
 * Returns a new list that replaces the first occurrence that matches the given [predicate] with
 * [newItem]. If no item matches the predicate, the same list is returned (and unmodified).
 */
fun <T> List<T>.replaceFirst(predicate: (T) -> Boolean, newItem: T): List<T> {
  forEachIndexed { index, element ->
    if (predicate(element)) {
      return replace(index, newItem)
    }
  }
  return this
}
