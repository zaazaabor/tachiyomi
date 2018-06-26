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

  private var filter: FilterWrapper.Text? = null

  init {
    filter_text.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(p0: Editable?) {}

      override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

      override fun onTextChanged(sequence: CharSequence, p1: Int, p2: Int, p3: Int) {
        filter?.value = sequence.toString()
      }
    })
  }

  fun bind(wrapper: FilterWrapper<*>) {
    wrapper as FilterWrapper.Text
    filter = wrapper
    filter_text_wrapper.hint = wrapper.filter.name
    filter_text.setText(wrapper.value)
  }

}

class ChipHolder(parent: ViewGroup) : BaseViewHolder(parent.inflate(R.layout.filter_chip)) {

  private val unsetColor = filter_chip.chipBackgroundColor
  private val includeColor = ColorStateList.valueOf(Color.parseColor("#4caf50"))
  private val excludeColor = ColorStateList.valueOf(Color.parseColor("#ff6f60"))

  fun bind(wrapper: FilterWrapper<*>) {
    wrapper as FilterWrapper.Check
    val filter = wrapper.filter as Filter.Check

    filter_chip.chipText = filter.name
    setBackground(wrapper)

    filter_chip.setOnClickListener {
      wrapper.value = if (filter.allowsExclusion) {
        when (wrapper.value) {
          true -> false
          false -> null
          null -> true
        }
      } else {
        if (wrapper.value == null) {
          true
        } else {
          null
        }
      }
      setBackground(wrapper)
    }
  }

  private fun setBackground(filter: FilterWrapper.Check) {
    filter_chip.chipBackgroundColor = when (filter.value) {
      true -> includeColor
      false -> excludeColor
      null -> unsetColor
    }
  }

}

class GroupHolder(parent: ViewGroup) : BaseViewHolder(parent.inflate(R.layout.filter_group)) {
  fun bind(wrapper: FilterWrapper<*>) {
    filter_group.text = wrapper.filter.name
  }
}
