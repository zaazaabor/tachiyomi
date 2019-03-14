/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.chapter.model

import tachiyomi.core.stdlib.Optional

data class ChapterUpdate(
  val id: Long,
  val mangaId: Optional<Long> = Optional.None,
  val key: Optional<String> = Optional.None,
  val name: Optional<String> = Optional.None,
  val read: Optional<Boolean> = Optional.None,
  val bookmark: Optional<Boolean> = Optional.None,
  val progress: Optional<Int> = Optional.None,
  val dateUpload: Optional<Long> = Optional.None,
  val dateFetch: Optional<Long> = Optional.None,
  val sourceOrder: Optional<Int> = Optional.None,
  val number: Optional<Float> = Optional.None,
  val scanlator: Optional<String> = Optional.None
)
