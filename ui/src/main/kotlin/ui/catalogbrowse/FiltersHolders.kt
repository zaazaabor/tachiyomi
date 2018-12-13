/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogbrowse

import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import kotlinx.android.synthetic.main.filter_chip.*
import kotlinx.android.synthetic.main.filter_group.*
import kotlinx.android.synthetic.main.filter_text.*
import tachiyomi.source.model.Filter
import tachiyomi.ui.R
import tachiyomi.ui.base.BaseViewHolder
import tachiyomi.util.inflate

// TODO complete the remaining view holders.

/**
 * View holder for a [FilterWrapper.Text].
 */
class TextHolder(parent: ViewGroup) : BaseViewHolder(parent.inflate(R.layout.filter_text)) {

  private var wrapper: FilterWrapper.Text? = null

  init {
    filter_text.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(p0: Editable?) {}

      override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

      override fun onTextChanged(sequence: CharSequence, p1: Int, p2: Int, p3: Int) {
        wrapper?.value = sequence.toString()
      }
    })
  }

  fun bind(wrapper: FilterWrapper<*>) {
    wrapper as FilterWrapper.Text
    this.wrapper = wrapper
    filter_text_wrapper.hint = wrapper.filter.name
    filter_text.setText(wrapper.value)
  }

}

/**
 * View holder for a [FilterWrapper.Check].
 */
class ChipHolder(parent: ViewGroup) : BaseViewHolder(parent.inflate(R.layout.filter_chip)) {

  private val unsetColor = filter_chip.chipBackgroundColor
  private val includeColor = ColorStateList.valueOf(Color.parseColor("#4caf50"))
  private val excludeColor = ColorStateList.valueOf(Color.parseColor("#ff6f60"))

  fun bind(wrapper: FilterWrapper<*>) {
    wrapper as FilterWrapper.Check
    val filter = wrapper.filter as Filter.Check

    filter_chip.text = filter.name
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

/**
 * View holder for a [FilterWrapper.Group]
 */
class GroupHolder(parent: ViewGroup) : BaseViewHolder(parent.inflate(R.layout.filter_group)) {

  fun bind(wrapper: FilterWrapper<*>) {
    filter_group.text = wrapper.filter.name
  }

}
