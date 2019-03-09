/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.manga

import android.view.View
import kotlinx.android.synthetic.main.manga_header_item.*
import tachiyomi.ui.adapter.BaseViewHolder
import tachiyomi.ui.glide.GlideApp
import tachiyomi.ui.glide.MangaCover

class MangaHeaderHolder(private val view: View) : BaseViewHolder(view) {

  fun bind(header: MangaHeader?) {
    if (header == null) return
    val manga = header.manga

    manga_full_title.text = manga.title
    manga_author.text = manga.author
    manga_artist.text = manga.artist
    manga_status.text = manga.status.toString()
    manga_source.text = manga.source.toString()

    GlideApp.with(view.context)
      .load(MangaCover.from(manga))
      .centerCrop()
      .into(manga_cover)

  }

}
