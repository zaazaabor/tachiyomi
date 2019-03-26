/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogbrowse

import android.view.View
import kotlinx.android.synthetic.main.catalogbrowse_footer_item.*
import tachiyomi.ui.adapter.BaseViewHolder
import tachiyomi.ui.util.visibleIf

/**
 * Holder to use when displaying a loading more progress bar or an end reached message from a
 * [CatalogBrowseAdapter].
 */
class FooterHolder(view: View) : BaseViewHolder(view) {

  fun bind(showProgress: Boolean, showEndReached: Boolean) {
    catalogbrowse_footer_progress.visibleIf { showProgress }
    catalogbrowse_footer_message.visibleIf { showEndReached }
  }
}
