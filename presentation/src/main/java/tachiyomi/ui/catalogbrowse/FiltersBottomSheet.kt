package tachiyomi.ui.catalogbrowse

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.doOnPreDraw
import com.google.android.material.bottomsheet.BottomSheetBehavior

class FiltersBottomSheet @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null
) : FrameLayout(context, attributeSet) {

  private var footer: View? = null

  private var maxOffset = 0f

  private var contentPaddingBottom: Int? = null

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)

    // The sticky footer is the last view in the group
    val footer = footer ?: getChildAt(childCount - 1).also { footer = it }

    // The content is the second to last view in the group
    val content = getChildAt(childCount - 2)

    // Get or store the original padding bottom
    val contentPaddingBottom = contentPaddingBottom ?: content.paddingBottom
      .also { contentPaddingBottom = it }

    // Apply padding to content
    content.setPadding(content.paddingLeft, content.paddingTop, content.paddingRight,
      footer.measuredHeight + contentPaddingBottom)
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    super.onLayout(changed, left, top, right, bottom)

    val footer = footer!! // Can't be null since it was retrieved when measuring

    val behavior = BottomSheetBehavior.from(parent as View)

    // Since the max offset is measured after layout, wait for the first draw event to retrieve
    // the value and set the initial state
    (parent as View).doOnPreDraw {
      maxOffset = (maxOffsetField.get(behavior) as Int).toFloat()

      when (behavior.state) {
        BottomSheetBehavior.STATE_EXPANDED -> footer.translationY = 0f
        BottomSheetBehavior.STATE_COLLAPSED -> footer.translationY = -maxOffset
      }
    }
  }

  fun onSlide(slideOffset: Float) {
    footer?.translationY = if (slideOffset >= 0) {
      -maxOffset * (1 - slideOffset)
    } else {
      -maxOffset
    }
  }

  private companion object {
    val maxOffsetField = BottomSheetBehavior::class.java.getDeclaredField("collapsedOffset").apply {
      isAccessible = true
    }
  }

}
