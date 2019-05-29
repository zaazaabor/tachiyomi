/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.util

import android.content.ContentValues

fun ContentValues.optString(key: String, optional: Optional<String?>) {
  if (optional is Optional.Some) put(key, optional.value)
}

fun ContentValues.optInt(key: String, optional: Optional<Int>) {
  if (optional is Optional.Some) put(key, optional.value)
}

fun ContentValues.optLong(key: String, optional: Optional<Long>) {
  if (optional is Optional.Some) put(key, optional.value)
}

fun ContentValues.optFloat(key: String, optional: Optional<Float>) {
  if (optional is Optional.Some) put(key, optional.value)
}

fun ContentValues.optBoolean(key: String, optional: Optional<Boolean>) {
  if (optional is Optional.Some) put(key, if (optional.value) 1 else 0)
}
