package tachiyomi.ui.catalogbrowse

import android.support.v7.util.DiffUtil
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.ViewGroup
import tachiyomi.app.R
import tachiyomi.domain.manga.model.Manga
import tachiyomi.ui.base.BaseListAdapter
import tachiyomi.widget.AutofitRecyclerView

class CatalogBrowseAdapter(
  val controller: CatalogBrowseController
) : BaseListAdapter<Manga, MangaHolder>(Diff()) {

//  init {
//    setHasStableIds(true)
//  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MangaHolder {
    val isListMode = (parent as? AutofitRecyclerView)?.layoutManager !is GridLayoutManager
    val inflater = LayoutInflater.from(parent.context)
    return if (!isListMode) {
      val view = inflater.inflate(R.layout.manga_grid_item, parent, false)
      MangaGridHolder(view, this)
    } else {
      val view = inflater.inflate(R.layout.manga_list_item, parent, false)
      MangaListHolder(view, this)
    }
  }

  override fun onBindViewHolder(holder: MangaHolder, position: Int) {
    onBindViewHolder(holder, position, emptyList())
  }

  override fun onBindViewHolder(holder: MangaHolder, position: Int, payloads: List<Any>) {
    val manga = getItem(position)
    if (payloads.isEmpty()) {
      holder.bind(manga)
    } else {
      for (payload in payloads) {
        if (payload == CoverChange) {
          holder.bindImage(manga)
        }
      }
    }
  }

//  override fun getItemId(position: Int): Long {
//    return getItem(position).id
//  }

  private class Diff : DiffUtil.ItemCallback<Manga>() {
    override fun areItemsTheSame(oldItem: Manga, newItem: Manga): Boolean {
      return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Manga, newItem: Manga): Boolean {
      return oldItem == newItem
    }

    override fun getChangePayload(oldItem: Manga, newItem: Manga): Any? {
      // TODO this method isn't called in 27.1.0. Will be fixed in the next release
      return if (oldItem.cover != newItem.cover) {
        CoverChange
      } else {
        null
      }
    }
  }

  private object CoverChange

}
