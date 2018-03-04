package tachiyomi.ui.category

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.TextView
import org.jetbrains.anko.dip
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.verticalPadding
import org.jetbrains.anko.wrapContent
import tachiyomi.domain.category.Category

class CategoryAdapter : RecyclerView.Adapter<Holder>() {

  private var items: List<Category> = emptyList()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder? {
    return Holder(TextView(parent.context).apply {
      textSize = 20f
      verticalPadding = dip(8)
      isClickable = true
      layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
    })
  }

  override fun onBindViewHolder(holder: Holder, position: Int) {
    holder.textView.text = items[position].name
  }

  override fun getItemCount(): Int {
    return items.size
  }

  fun updateItems(items: List<Category>) {
    this.items = items
    notifyDataSetChanged()
  }
}

class Holder(val textView: TextView) : RecyclerView.ViewHolder(textView)
