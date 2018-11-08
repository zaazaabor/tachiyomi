package tachiyomi.ui.base

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import com.bluelinelabs.conductor.ChangeHandlerFrameLayout

class InsetsChangeHandlerFrameLayout @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : ChangeHandlerFrameLayout(context, attrs, defStyleAttr) {

  init {
    fitsSystemWindows = true

    // Look for replaced views and apply the insets again.
    setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
      override fun onChildViewAdded(parent: View, child: View) {
        child.fitsSystemWindows = true
        requestApplyInsets()
      }

      override fun onChildViewRemoved(parent: View, child: View) {
      }
    })
  }

  override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
    val childCount = childCount
    for (i in 0 until childCount) {
      val child = getChildAt(i)
      child.dispatchApplyWindowInsets(insets)
    }
    return insets
  }

}
