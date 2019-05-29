/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.manga.model

import tachiyomi.core.util.Optional

data class MangaUpdate(
  val id: Long,
  val source: Optional<Long> = Optional.None,
  val key: Optional<String> = Optional.None,
  val title: Optional<String> = Optional.None,
  val artist: Optional<String> = Optional.None,
  val author: Optional<String> = Optional.None,
  val description: Optional<String> = Optional.None,
  val genres: Optional<List<String>> = Optional.None,
  val status: Optional<Int> = Optional.None,
  val cover: Optional<String> = Optional.None,
  val favorite: Optional<Boolean> = Optional.None,
  val lastUpdate: Optional<Long> = Optional.None,
  val lastInit: Optional<Long> = Optional.None,
  val dateAdded: Optional<Long> = Optional.None,
  val viewer: Optional<Int> = Optional.None,
  val flags: Optional<Int> = Optional.None
)
