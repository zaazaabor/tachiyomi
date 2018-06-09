package tachiyomi.ui.catalogbrowse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.filter_checkbox.*
import tachiyomi.app.R
import tachiyomi.ui.base.BaseViewHolder

class FiltersAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val items = IntRange(0, 40).map { it }

  override fun getItemCount(): Int {
    return items.size
  }

  override fun getItemViewType(position: Int): Int {
    return when (position) {
      0 -> 1
      else -> 2
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return when (viewType) {
      1 -> {
        val view = inflater.inflate(R.layout.filter_text, parent, false)
        TextHolder(view)
      }
      2 -> {
        val view = inflater.inflate(R.layout.filter_checkbox, parent, false)
        CheckHolder(view)
      }
      else -> error("")
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder) {
      is CheckHolder -> holder.nav_view_item.text = "Filter $position"
    }
  }

}

class TextHolder(view: View) : BaseViewHolder(view)
class CheckHolder(view: View) : BaseViewHolder(view)
