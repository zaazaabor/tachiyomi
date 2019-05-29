/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.util

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
