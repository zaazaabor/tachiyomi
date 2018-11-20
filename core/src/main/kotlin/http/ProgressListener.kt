/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.http

/**
 * A listener who receives updates of the progress of network responses.
 */
interface ProgressListener {

  /**
   * Called every time a new chunk of data is read. [bytesRead] contains the currently read bytes,
   * [contentLength] shows the total size of the content if the server sent the corresponding
   * header, otherwise it will be -1, and [done] especify whether the source is already exhausted.
   *
   * This function should return quickly due to the amount of times it's called.
   */
  fun update(bytesRead: Long, contentLength: Long, done: Boolean)

}
