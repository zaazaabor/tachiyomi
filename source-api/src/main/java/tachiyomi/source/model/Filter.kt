package tachiyomi.source.model

@Suppress("unused")
sealed class Filter<T>(val name: String, var state: T) {

  abstract fun isDefaultValue(): Boolean

  /**
   * Base filters.
   */

  class Message(name: String) : Filter<Any>(name, Unit) {

    override fun isDefaultValue() = true
  }

  abstract class Select<V>(
    name: String,
    val values: Array<V>,
    state: Int = 0
  ) : Filter<Int>(name, state) {

    override fun isDefaultValue() = state == 0
  }

  abstract class Text(name: String, state: String = "") : Filter<String>(name, state) {

    override fun isDefaultValue() = state.isBlank()
  }

  abstract class CheckBox(name: String, state: Boolean = false) : Filter<Boolean>(name, state) {

    override fun isDefaultValue() = !state
  }

  abstract class TriState(name: String, state: Int = STATE_IGNORE) : Filter<Int>(name, state) {
    fun isIgnored() = state == STATE_IGNORE
    fun isIncluded() = state == STATE_INCLUDE
    fun isExcluded() = state == STATE_EXCLUDE

    override fun isDefaultValue() = state == STATE_IGNORE

    companion object {
      const val STATE_IGNORE = 0
      const val STATE_INCLUDE = 1
      const val STATE_EXCLUDE = 2
    }
  }

  abstract class Group<V>(name: String, state: List<V>) : Filter<List<V>>(name, state) {

    override fun isDefaultValue() = true
  }

  abstract class Sort(
    name: String, val values: Array<String>, state: Selection? = null
  ) : Filter<Sort.Selection?>(name, state) {

    data class Selection(val index: Int, val ascending: Boolean)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Filter<*>

    return name == other.name && state == other.state
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + (state?.hashCode() ?: 0)
    return result
  }

  /**
   * Common filters.
   */

  class Title(name: String = "Title") : Text(name, "")

  class Author(name: String = "Author") : Text(name, "")

  class Artist(name: String = "Artist") : Text(name, "")

  interface Genre {
    val name: String
  }

  class GenreImpl(override val name: String) : Genre // Don't use this from extensions

  class GenreCheckBox(name: String) : CheckBox(name), Genre

  class GenreTriState(name: String) : TriState(name), Genre

}
