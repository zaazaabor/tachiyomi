/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.interactor

import io.reactivex.Flowable
import tachiyomi.domain.category.Category
import tachiyomi.domain.category.interactor.SubscribeCategoriesWithCount
import javax.inject.Inject

class SubscribeUserCategories @Inject constructor(
  private val subscribeCategoriesWithCount: SubscribeCategoriesWithCount
) {

  fun interact(withAllCategory: Boolean): Flowable<List<Category>> {
    return subscribeCategoriesWithCount.interact()
      .map { categories ->
        categories.mapNotNull { (category, count) ->
          when (category.id) {
            // All category only shown when requested
            Category.ALL_ID -> if (withAllCategory) category else null

            // Uncategorized category only shown if there are entries and user categories exist
            Category.UNCATEGORIZED_ID -> {
              if (count > 0 && categories.any { !it.category.isSystemCategory }) {
                category
              } else {
                null
              }
            }

            // User created category, always show
            else -> category
          }
        }
      }
      .distinctUntilChanged()
  }

}
