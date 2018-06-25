package tachiyomi.ui.catalogbrowse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tachiyomi.source.model.Filter
import tachiyomi.source.model.FilterList
import tachiyomi.ui.base.BaseViewHolder

class FiltersAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  var items = emptyList<Filter<*>>()
    private set

  fun updateItems(filters: FilterList) {
    val flatFilters = mutableListOf<Filter<*>>()
    for (filter in filters) {
      flatFilters.add(filter)
      if (filter is Filter.Group<*>) {
        flatFilters.addAll(filter.state as List<Filter<*>>)
      }
    }

    items = flatFilters
    notifyDataSetChanged()
  }

  override fun getItemCount(): Int {
    return items.size
  }

  override fun getItemViewType(position: Int): Int {
    val filter = items[position]
    return when (filter) {
      is Filter.Text -> TEXT_HOLDER
      is Filter.CheckBox -> CHECK_HOLDER
      is Filter.TriState -> TRI_HOLDER
      is Filter.Group<*> -> GROUP_HOLDER
      else -> 1 // TODO
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return when (viewType) {
      TEXT_HOLDER -> TextHolder(parent)
      CHECK_HOLDER, TRI_HOLDER -> ChipHolder(parent)
      GROUP_HOLDER -> GroupHolder(parent)
      else -> {
        // TODO
        val view = View(parent.context)
        object : BaseViewHolder(view) {}
      }
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder) {
      is ChipHolder -> {
        val item = items[position]
        holder.bind(item)
      }
      is GroupHolder -> {
        val item = items[position]
        holder.bind(item)
      }
      is TextHolder -> {
        val item = items[position]
        holder.bind(item)
      }
    }
  }

  private companion object {
    const val TEXT_HOLDER = 1
    const val CHECK_HOLDER = 2
    const val RADIO_HOLDER = 3
    const val TRI_HOLDER = 4
    const val GROUP_HOLDER = 5
  }

}

