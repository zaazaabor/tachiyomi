package tachiyomi.ui.catalogs

import android.view.View
import kotlinx.android.synthetic.main.catalogue_card_item.*
import tachiyomi.source.CatalogSource
import tachiyomi.ui.base.BaseViewHolder

class SourceHolder(view: View, adapter: CatalogsAdapter) : BaseViewHolder(view) {

  init {
    view.setOnClickListener {
      adapter.handleRowClick(adapterPosition)
    }
    source_browse.setOnClickListener {
      adapter.handleBrowseClick(adapterPosition)
    }
    source_latest.setOnClickListener {
      adapter.handleLatestClick(adapterPosition)
    }
  }

  fun bind(item: CatalogSource) {
    title.text = item.name
  }

}
