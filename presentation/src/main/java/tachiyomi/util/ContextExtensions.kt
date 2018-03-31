package tachiyomi.util

import android.content.Context
import android.support.annotation.AttrRes

/**
 * Returns the color for the given attribute.
 *
 * @param resource the attribute.
 */
fun Context.getResourceColor(@AttrRes resource: Int): Int {
  val typedArray = obtainStyledAttributes(intArrayOf(resource))
  val attrValue = typedArray.getColor(0, 0)
  typedArray.recycle()
  return attrValue
}
