@file:Suppress("NOTHING_TO_INLINE")

package tachiyomi.util

import android.view.View

inline fun View.setVisible() {
  visibility = View.VISIBLE
}

inline fun View.setInvisible() {
  visibility = View.INVISIBLE
}

inline fun View.setGone() {
  visibility = View.GONE
}

inline fun View.visibleIf(block: () -> Boolean) {
  visibility = if (block()) View.VISIBLE else View.GONE
}
