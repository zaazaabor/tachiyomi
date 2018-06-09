package tachiyomi.ui.catalogbrowse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tachiyomi.app.R
import tachiyomi.domain.manga.model.Manga
import tachiyomi.widget.AutofitRecyclerView

class CatalogBrowseAdapter(
  val controller: CatalogBrowseController
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val listener = controller as? Listener

  private val differ = AsyncListDiffer(AdapterListUpdateCallback(this),
    AsyncDifferConfig.Builder(ItemDiff()).build())

  override fun getItemCount(): Int {
    return differ.currentList.size
  }

  fun getItem(position: Int): Any {
    return differ.currentList[position]
  }

  fun getItemOrNull(position: Int): Any? {
    return differ.currentList.getOrNull(position)
  }

  fun submitList(mangas: List<Manga>, isLoading: Boolean, endReached: Boolean) {
    val items = mutableListOf<Any>()
    items.addAll(mangas)

    if (isLoading && mangas.isNotEmpty()) {
      items += LoadingMore
    } else if (endReached) {
      items += EndReached
    }

    differ.submitList(items)
  }

  override fun getItemViewType(position: Int): Int {
    return when (getItem(position)) {
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
        val manga = getItem(position) as Manga
        if (payloads.isEmpty()) {
          holder.bind(manga)
        } else if (CoverChange in payloads) {
          holder.bindImage(manga)
        }
      }
      is FooterHolder -> {
        val item = getItem(position)
        holder.bind(item === LoadingMore, item === EndReached)
      }
    }
  }

  fun handleClick(position: Int) {
    val manga = getItemOrNull(position) as? Manga ?: return
    listener?.onMangaClick(manga)
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

  private class ItemDiff : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
      return when {
        oldItem === newItem -> true
        oldItem is Manga && newItem is Manga -> oldItem.id == newItem.id
        else -> false
      }
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
      return oldItem == newItem
    }

    override fun getChangePayload(oldItem: Any, newItem: Any): Any? {
      return if (oldItem is Manga && newItem is Manga && oldItem.cover != newItem.cover) {
        CoverChange
      } else {
        null
      }
    }
  }

  private object LoadingMore
  private object EndReached

  private object CoverChange

  private companion object {
    const val MANGA_VIEWTYPE = 1
    const val FOOTER_VIEWTYPE = 2
  }

}
