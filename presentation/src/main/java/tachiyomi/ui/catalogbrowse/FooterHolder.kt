package tachiyomi.ui.catalogbrowse

import android.view.View
import kotlinx.android.synthetic.main.catalogbrowse_footer_item.*
import tachiyomi.ui.base.BaseViewHolder
import tachiyomi.util.visibleIf

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
