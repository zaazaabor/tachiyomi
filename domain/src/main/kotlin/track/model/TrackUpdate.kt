/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.track.model

import tachiyomi.core.util.Optional

data class TrackUpdate(
  val id: Long,
  val entryId: Optional<Long> = Optional.None,
  val mediaId: Optional<Long> = Optional.None,
  val mediaUrl: Optional<String> = Optional.None,
  val title: Optional<String> = Optional.None,
  val lastChapterRead: Optional<Float> = Optional.None,
  val totalChapters: Optional<Int> = Optional.None,
  val score: Optional<Float> = Optional.None,
  val status: Optional<TrackStatus> = Optional.None
)
