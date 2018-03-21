package tachiyomi.core.util

sealed class Optional<out T> {
  class Some<out T>(val value: T) : Optional<T>()
  object None : Optional<Nothing>()

  companion object {
    fun <T> of(value: T?): Optional<T> {
      return if (value != null) Some(value) else None
    }

    fun empty(): Optional<Nothing> {
      return None
    }
  }
}
