package tachiyomi.ui.catalogbrowse

import tachiyomi.source.model.Filter

sealed class FilterWrapper<T>(val filter: Filter<T>) {

  var value = filter.value

  fun reset() {
    value = filter.initialValue
  }

  fun updateInnerValue() {
    filter.value = value
  }

  class Note(filter: Filter.Note) : FilterWrapper<Unit>(filter)

  open class Text(filter: Filter.Text) : FilterWrapper<String>(filter)

  open class Check(
    filter: Filter.Check
  ) : FilterWrapper<Boolean?>(filter)

  open class Select(
    filter: Filter.Select
  ) : FilterWrapper<Int>(filter)

  open class Group(filter: Filter.Group) : FilterWrapper<Unit>(filter)

  open class Sort(
    filter: Filter.Sort
  ) : FilterWrapper<Filter.Sort.Selection?>(filter)

  companion object {
    fun from(filter: Filter<*>): FilterWrapper<*> {
      return when (filter) {
        is Filter.Note -> FilterWrapper.Note(filter)
        is Filter.Text -> FilterWrapper.Text(filter)
        is Filter.Check -> FilterWrapper.Check(filter)
        is Filter.Select -> FilterWrapper.Select(filter)
        is Filter.Group -> FilterWrapper.Group(filter)
        is Filter.Sort -> FilterWrapper.Sort(filter)
      }
    }
  }
}
