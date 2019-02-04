/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.deeplink

import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.manga.model.Manga

data class ChapterDeepLinkViewState(
  val loading: Boolean = true,
  val manga: Manga? = null,
  val chapter: Chapter? = null,
  val error: Throwable? = null
)
