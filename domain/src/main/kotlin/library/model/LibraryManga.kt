/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.model

data class LibraryManga(
  val mangaId: Long,
  val sourceId: Long,
  val key: String,
  val title: String,
  val status: Int,
  val cover: String,
  val lastUpdate: Long = 0,
  val unread: Int
)
