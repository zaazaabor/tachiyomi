package tachiyomi.domain.category

data class Category(
  val id: Long = -1,
  val name: String = "",
  val order: Int = 0,
  val flags: Int = 0
)
