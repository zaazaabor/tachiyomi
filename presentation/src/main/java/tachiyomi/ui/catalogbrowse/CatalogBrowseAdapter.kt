package tachiyomi.ui.catalogbrowse

import android.support.v7.recyclerview.extensions.AsyncDifferConfig
import android.support.v7.recyclerview.extensions.AsyncListDiffer
import android.support.v7.util.AdapterListUpdateCallback
import android.support.v7.util.DiffUtil
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import tachiyomi.app.R
import tachiyomi.domain.manga.model.Manga
import tachiyomi.widget.AutofitRecyclerView

class CatalogBrowseAdapter(
  val controller: CatalogBrowseController
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val listener = controller as? Listener

  private val differ = AsyncListDiffer(AdapterListUpdateCallback(this),
    AsyncDifferConfig.Builder(Diff()).build())

  private var showProgress = false

  private var showEndReached = false

  override fun getItemCount(): Int {
    return differ.currentList.size + if (showProgress || showEndReached) 1 else 0
  }

  fun getItem(position: Int): Manga {
    return differ.currentList[position]
  }

  fun getItemOrNull(position: Int): Manga? {
    return differ.currentList.getOrNull(position)
  }

  fun submitList(mangas: List<Manga>) {
    differ.submitList(mangas)
  }

  fun setLoading(visible: Boolean) {
    if (showProgress == visible) return

    showProgress = visible
    setFooterVisibility(visible)
  }

  fun setEndReached(endReached: Boolean) {
    if (showEndReached == endReached) return

    showEndReached = endReached
    setFooterVisibility(endReached)
  }

  private fun setFooterVisibility(visible: Boolean) {
    val footerPosition = differ.currentList.size
    if (visible) {
      notifyItemInserted(footerPosition)
    } else {
      notifyItemRemoved(footerPosition)
    }
  }

  override fun getItemViewType(position: Int): Int {
    return when (getItemOrNull(position)) {
      is Manga -> MANGA_VIEWTYPE
      else -> FOOTER_VIEWTYPE
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return when (viewType) {
      MANGA_VIEWTYPE -> {
        val isGridMode = (parent as? AutofitRecyclerView)?.layoutManager is GridLayoutManager
        if (isGridMode) {
          val view = inflater.inflate(R.layout.manga_grid_item, parent, false)
          MangaGridHolder(view, this)
        } else {
          val view = inflater.inflate(R.layout.manga_list_item, parent, false)
          MangaListHolder(view, this)
        }
      }
      FOOTER_VIEWTYPE -> {
        val view = inflater.inflate(R.layout.catalogbrowse_footer_item, parent, false)
        FooterHolder(view)
      }
      else -> error("$viewType is not a valid viewtype")
    }

  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    onBindViewHolder(holder, position, emptyList())
  }

  override fun onBindViewHolder(
    holder: RecyclerView.ViewHolder,
    position: Int,
    payloads: List<Any>
  ) {
    when (holder) {
      is MangaHolder -> {
        val manga = getItem(position)
        if (payloads.isEmpty()) {
          holder.bind(manga)
        } else if (CoverChange in payloads) {
          holder.bindImage(manga)
        }
      }
      is FooterHolder -> {
        holder.bind(showProgress, showEndReached)
      }
    }
  }

  fun handleClick(position: Int) {
    val item = getItemOrNull(position) ?: return
    listener?.onMangaClick(item)
  }

  fun getSpanSize(position: Int): Int? {
    return when (getItemViewType(position)) {
      MANGA_VIEWTYPE -> 1
      else -> null
    }
  }

  interface Listener {
    fun onMangaClick(manga: Manga)
  }

  private class Diff : DiffUtil.ItemCallback<Manga>() {
    override fun areItemsTheSame(oldItem: Manga, newItem: Manga): Boolean {
      return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Manga, newItem: Manga): Boolean {
      return oldItem == newItem
    }

    override fun getChangePayload(oldItem: Manga, newItem: Manga): Any? {
      return if (oldItem.cover != newItem.cover) {
        CoverChange
      } else {
        null
      }
    }
  }

  private object CoverChange

  private companion object {
    const val MANGA_VIEWTYPE = 1
    const val FOOTER_VIEWTYPE = 2
  }

}
