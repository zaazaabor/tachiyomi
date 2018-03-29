package tachiyomi.core.rx

sealed class RxOptional<out T> {
  class Some<out T>(val value: T) : RxOptional<T>()
  object None : RxOptional<Nothing>()

  companion object {
    fun <T> of(value: T?): RxOptional<T> {
      return if (value != null) Some(value) else None
    }

    fun empty(): RxOptional<Nothing> {
      return None
    }
  }
}
