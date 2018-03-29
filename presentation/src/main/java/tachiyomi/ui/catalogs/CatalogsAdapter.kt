package tachiyomi.ui.catalogs

import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import tachiyomi.app.R
import tachiyomi.domain.source.CatalogueSource
import tachiyomi.ui.base.BaseListAdapter

class CatalogsAdapter(
  controller: CatalogsController
) : BaseListAdapter<CatalogueSource, SourceHolder>(Diff()) {

  private val listener: Listener = controller

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SourceHolder {
    val inflater = LayoutInflater.from(parent.context)
    val view = inflater.inflate(R.layout.catalogue_card_item, parent, false)
    return SourceHolder(view, this)
  }

  override fun onBindViewHolder(holder: SourceHolder, position: Int) {
    holder.bind(getItem(position))
  }

  fun handleRowClick(position: Int) {
    val item = getItemOrNull(position) ?: return
    listener.onRowClick(item)
  }

  fun handleBrowseClick(position: Int) {
    val item = getItemOrNull(position) ?: return
    listener.onBrowseClick(item)
  }

  fun handleLatestClick(position: Int) {
    val item = getItemOrNull(position) ?: return
    listener.onLatestClick(item)
  }

  interface Listener {
    fun onRowClick(catalogue: CatalogueSource)
    fun onBrowseClick(catalogue: CatalogueSource)
    fun onLatestClick(catalogue: CatalogueSource)
  }

  private class Diff : DiffUtil.ItemCallback<CatalogueSource>() {
    override fun areItemsTheSame(oldItem: CatalogueSource, newItem: CatalogueSource): Boolean {
      return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CatalogueSource, newItem: CatalogueSource): Boolean {
      return true
    }
  }

}
