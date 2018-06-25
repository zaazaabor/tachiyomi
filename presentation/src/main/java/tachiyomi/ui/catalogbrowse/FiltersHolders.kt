package tachiyomi.ui.catalogbrowse

import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import kotlinx.android.synthetic.main.filter_chip.*
import kotlinx.android.synthetic.main.filter_group.*
import kotlinx.android.synthetic.main.filter_text.*
import tachiyomi.app.R
import tachiyomi.source.model.Filter
import tachiyomi.ui.base.BaseViewHolder
import tachiyomi.util.inflate

class TextHolder(parent: ViewGroup) : BaseViewHolder(parent.inflate(R.layout.filter_text)) {

  private var filter: Filter.Text? = null

  init {
    filter_text.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(p0: Editable?) {}

      override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

      override fun onTextChanged(sequence: CharSequence, p1: Int, p2: Int, p3: Int) {
        filter?.state = sequence.toString()
      }
    })
  }

  fun bind(item: Filter<*>) {
    item as Filter.Text
    filter = item
    filter_text_wrapper.hint = item.name
    filter_text.setText(item.state)
  }

}

class ChipHolder(parent: ViewGroup) : BaseViewHolder(parent.inflate(R.layout.filter_chip)) {

  private val unsetColor = filter_chip.chipBackgroundColor
  private val includeColor = ColorStateList.valueOf(Color.parseColor("#4caf50"))
  private val excludeColor = ColorStateList.valueOf(Color.parseColor("#ff6f60"))

  fun bind(filter: Filter<*>) {
    filter_chip.chipText = filter.name
    setBackground(filter)

    filter_chip.setOnClickListener {
      when (filter) {
        is Filter.CheckBox -> {
          filter.state = !filter.state
          setBackground(filter)
        }
        is Filter.TriState -> {
          filter.state = (filter.state + 1) % 3
          setBackground(filter)
        }
      }
    }
  }

  private fun setBackground(filter: Filter<*>) {
    when (filter) {
      is Filter.CheckBox -> {
        filter_chip.chipBackgroundColor = if (filter.state) {
          includeColor
        } else {
          unsetColor
        }
      }
      is Filter.TriState -> {
        filter_chip.chipBackgroundColor = when (filter.state) {
          Filter.TriState.STATE_INCLUDE -> includeColor
          Filter.TriState.STATE_EXCLUDE -> excludeColor
          else -> unsetColor
        }
      }
    }
  }

}

class GroupHolder(parent: ViewGroup) : BaseViewHolder(parent.inflate(R.layout.filter_group)) {
  fun bind(item: Filter<*>) {
    filter_group.text = item.name
  }
}
