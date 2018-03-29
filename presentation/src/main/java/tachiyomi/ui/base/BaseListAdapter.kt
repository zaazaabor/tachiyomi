package tachiyomi.ui.base

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView

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
