/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.category

import android.content.Context
import tachiyomi.domain.library.model.Category
import tachiyomi.ui.R

fun Category.getVisibleName(context: Context): String {
  return when (id) {
    Category.ALL_ID -> context.getString(R.string.category_all_name)
    Category.UNCATEGORIZED_ID -> context.getString(R.string.category_uncategorized_name)
    else -> name
  }
}
