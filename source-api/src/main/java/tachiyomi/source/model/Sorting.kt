package tachiyomi.source.model

interface Sorting {
  val name: String
  fun getFilters(): FilterList
}
