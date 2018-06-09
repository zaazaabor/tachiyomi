package tachiyomi.ui.catalogbrowse

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class FiltersBottomSheetDialog(context: Context) : BottomSheetDialog(context) {

  private lateinit var behavior: BottomSheetBehavior<FrameLayout>

  private lateinit var frame: FiltersBottomSheet

  private val callback = object : BottomSheetBehavior.BottomSheetCallback() {

    override fun onSlide(bottomSheet: View, slideOffset: Float) {
      frame.onSlide(slideOffset)
    }

    override fun onStateChanged(bottomSheet: View, newState: Int) {
      if (newState == BottomSheetBehavior.STATE_HIDDEN) {
        cancel()
      }
    }

  }

  override fun setContentView(view: View) {
    super.setContentView(view)
    frame = view as FiltersBottomSheet
    val bottomSheet = window.decorView.findViewById<View>(
      com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
    behavior = BottomSheetBehavior.from(bottomSheet)
    behavior.setBottomSheetCallback(callback)
  }

}
