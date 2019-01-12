/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.manga.model

import tachiyomi.source.model.MangaInfo

data class Manga(
  val id: Long = -1,
  val source: Long,
  val key: String,
  val title: String,
  val artist: String = "",
  val author: String = "",
  val description: String = "",
  val genres: String = "",
  val status: Int = MangaInfo.UNKNOWN,
  val cover: String = "",
  val favorite: Boolean = false,
  val lastUpdate: Long = 0,
  val dateAdded: Long = 0,
  val initialized: Boolean = false,
  val viewer: Int = 0,
  val flags: Int = 0
)
