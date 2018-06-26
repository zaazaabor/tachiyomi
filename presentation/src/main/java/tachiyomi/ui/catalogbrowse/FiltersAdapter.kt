package tachiyomi.ui.catalogbrowse

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class FiltersAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  var items = emptyList<FilterWrapper<*>>()
    private set

  fun updateItems(filters: List<FilterWrapper<*>>) {
    items = filters
    notifyDataSetChanged()
  }

  override fun getItemCount(): Int {
    return items.size
  }

  override fun getItemViewType(position: Int): Int {
    val filter = items[position]
    return when (filter) {
      is FilterWrapper.Text -> TEXT_HOLDER
      is FilterWrapper.Check -> CHECK_HOLDER
      is FilterWrapper.Group -> GROUP_HOLDER
      else -> 0 // TODO
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      TEXT_HOLDER -> TextHolder(parent)
      CHECK_HOLDER -> ChipHolder(parent)
      GROUP_HOLDER -> GroupHolder(parent)
      else -> object : RecyclerView.ViewHolder(View(parent.context)) {} // TODO all types
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = items[position]
    when (holder) {
      is ChipHolder -> holder.bind(item)
      is GroupHolder -> holder.bind(item)
      is TextHolder -> holder.bind(item)
    }
  }

  private companion object {
    const val TEXT_HOLDER = 1
    const val CHECK_HOLDER = 2
    const val RADIO_HOLDER = 3
    const val GROUP_HOLDER = 5
  }

}
