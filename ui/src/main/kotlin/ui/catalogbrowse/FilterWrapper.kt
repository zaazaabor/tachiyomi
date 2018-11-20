/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogbrowse

import tachiyomi.source.model.Filter

/**
 * A wrapper for a [Filter] that allows saving temporary values (in sync with the UI) while keeping
 * the original filters with their initial (or current query) values.
 *
 * Before making a request, [updateInnerValue] should be called on each filter to update the
 * value of the original filter.
 *
 * [reset] can also be used to set the values on the wrapper to their initial value.
 */
sealed class FilterWrapper<T>(val filter: Filter<T>) {

  /**
   * The value of this wrapped filter. It's initially set to the value of the original filter.
   */
  var value = filter.value

  /**
   * Resets the value on the wrapped filter.
   */
  fun reset() {
    value = filter.initialValue
  }

  /**
   * Updates the value of the original filter with the one from this wrapper.
   */
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
