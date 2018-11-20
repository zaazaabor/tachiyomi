/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.category.resolver

import android.content.ContentValues
import tachiyomi.data.category.table.CategoryTable
import tachiyomi.domain.category.Category

internal class ReorderCategoriesPutResolver : CategoryUpdateResolver() {

  override fun mapToContentValues(category: Category): ContentValues {
    return ContentValues(1).apply {
      put(CategoryTable.COL_NAME, category.name)
    }
  }
}
