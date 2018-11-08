package tachiyomi.ui.catalogbrowse

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter for the list of filters from a catalog source.
 */
class FiltersAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  /**
   * The list of filters currently set in the adapter.
   */
  var items = emptyList<FilterWrapper<*>>()
    private set

  /**
   * Updates the adapter with the given list of [filters] and notifies the change.
   */
  fun updateItems(filters: List<FilterWrapper<*>>) {
    items = filters
    notifyDataSetChanged()
  }

  /**
   * Returns the number of items in the adapter.
   */
  override fun getItemCount(): Int {
    return items.size
  }

  /**
   * Returns the view type for the item on this [position].
   */
  override fun getItemViewType(position: Int): Int {
    val filter = items[position]
    return when (filter) {
      is FilterWrapper.Text -> TEXT_HOLDER
      is FilterWrapper.Check -> CHECK_HOLDER
      is FilterWrapper.Group -> GROUP_HOLDER
      else -> 0 // TODO
    }
  }

  /**
   * Creates a new view holder for the given [viewType].
   */
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      TEXT_HOLDER -> TextHolder(parent)
      CHECK_HOLDER -> ChipHolder(parent)
      GROUP_HOLDER -> GroupHolder(parent)
      else -> object : RecyclerView.ViewHolder(View(parent.context)) {} // TODO all types
    }
  }

  /**
   * Binds this [holder] with the item on this [position].
   */
  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = items[position]
    when (holder) {
      is ChipHolder -> holder.bind(item)
      is GroupHolder -> holder.bind(item)
      is TextHolder -> holder.bind(item)
    }
  }

  private companion object {
    /**
     * View type for a [FilterWrapper.Text]
     */
    const val TEXT_HOLDER = 1

    /**
     * View type for a [FilterWrapper.Check]
     */
    const val CHECK_HOLDER = 2

    /**
     * View type for a [FilterWrapper.Group]
     */
    const val GROUP_HOLDER = 5
  }

}
