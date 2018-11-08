package tachiyomi.ui.base

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class BaseListAdapter<T, VH : RecyclerView.ViewHolder>(
  callback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, VH>(callback) {

  fun getItemOrNull(position: Int): T? {
    return if (position < 0 || position > itemCount) {
      null
    } else {
      getItem(position)
    }
  }

}
