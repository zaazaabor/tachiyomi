package tachiyomi.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets

/**
 * This view allows to draw the status bar of a certain color. The implementation is basically a
 * stripped DrawerLayout.
 *
 * TODO this class was introduced if we ever change the color of the toolbar (the reader had a
 * different one). If all the views in this activity share the same toolbar, this won't be needed.
 */
class StatusBarInsetsViewGroup @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

  private var statusBarBackground: Drawable? = null

  private var drawStatusBarBackground = false

  private var lastInsets: WindowInsets? = null

  init {
    setOnApplyWindowInsetsListener { _, insets ->
      setChildInsets(insets, insets.systemWindowInsetTop > 0)
      insets.consumeSystemWindowInsets()
    }

    systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
      View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

    val a = context.obtainStyledAttributes(intArrayOf(android.R.attr.colorPrimaryDark))
    try {
      statusBarBackground = a.getDrawable(0)
    } finally {
      a.recycle()
    }
  }

  fun setChildInsets(insets: WindowInsets, draw: Boolean) {
    lastInsets = insets
    drawStatusBarBackground = draw
    setWillNotDraw(!draw && background == null)
    requestLayout()
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
    val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

    setMeasuredDimension(widthSize, heightSize)

    val lastInsets = lastInsets
    val applyInsets = lastInsets != null && fitsSystemWindows

    val childCount = childCount
    for (i in 0 until childCount) {
      val child = getChildAt(i)
      if (child.visibility == View.GONE) {
        continue
      }

      val lp = child.layoutParams as LayoutParams

      if (applyInsets) {
        if (child.fitsSystemWindows) {
          var wi = lastInsets
          wi = wi!!.replaceSystemWindowInsets(wi.systemWindowInsetLeft,
            wi.systemWindowInsetTop, wi.systemWindowInsetRight,
            wi.systemWindowInsetBottom)

          child.dispatchApplyWindowInsets(wi)
        } else {
          var wi = lastInsets
          wi = wi!!.replaceSystemWindowInsets(wi.systemWindowInsetLeft,
            wi.systemWindowInsetTop, wi.systemWindowInsetRight,
            wi.systemWindowInsetBottom)

          lp.leftMargin = wi.systemWindowInsetLeft
          lp.topMargin = wi.systemWindowInsetTop
          lp.rightMargin = wi.systemWindowInsetRight
          lp.bottomMargin = wi.systemWindowInsetBottom
        }
      }

      val contentWidthSpec = MeasureSpec.makeMeasureSpec(
        widthSize - lp.leftMargin - lp.rightMargin, View.MeasureSpec.EXACTLY)
      val contentHeightSpec = MeasureSpec.makeMeasureSpec(
        heightSize - lp.topMargin - lp.bottomMargin, View.MeasureSpec.EXACTLY)
      child.measure(contentWidthSpec, contentHeightSpec)
    }
  }

  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    val childCount = childCount
    for (i in 0 until childCount) {
      val child = getChildAt(i)

      if (child.visibility == View.GONE) {
        continue
      }

      val lp = child.layoutParams as LayoutParams

      child.layout(lp.leftMargin, lp.topMargin,
        lp.leftMargin + child.measuredWidth,
        lp.topMargin + child.measuredHeight)
    }
  }

  override fun onDraw(c: Canvas) {
    super.onDraw(c)
    val statusBarBackground = statusBarBackground
    if (drawStatusBarBackground && statusBarBackground != null) {
      val inset = lastInsets?.systemWindowInsetTop ?: 0
      if (inset > 0) {
        statusBarBackground.setBounds(0, 0, width, inset)
        statusBarBackground.draw(c)
      }
    }
  }

  fun setStatusBarBackground(bg: Drawable?) {
    statusBarBackground = bg
    invalidate()
  }

  fun setStatusBarBackgroundColor(@ColorInt color: Int) {
    statusBarBackground = ColorDrawable(color)
    invalidate()
  }

  override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
    return LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
  }

  override fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
    return if (p is LayoutParams)
      LayoutParams(p)
    else if (p is ViewGroup.MarginLayoutParams)
      LayoutParams(p)
    else
      LayoutParams(p)
  }

  override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
    return p is LayoutParams && super.checkLayoutParams(p)
  }

  override fun generateLayoutParams(attrs: AttributeSet): ViewGroup.LayoutParams {
    return LayoutParams(context, attrs)
  }

  class LayoutParams : ViewGroup.MarginLayoutParams {

    internal val LAYOUT_ATTRS = intArrayOf(android.R.attr.layout_gravity)

    var gravity = Gravity.NO_GRAVITY
    internal var onScreen: Float = 0.toFloat()
    internal var isPeeking: Boolean = false
    internal var openState: Int = 0

    constructor(c: Context, attrs: AttributeSet?) : super(c, attrs) {

      val a = c.obtainStyledAttributes(attrs, LAYOUT_ATTRS)
      this.gravity = a.getInt(0, Gravity.NO_GRAVITY)
      a.recycle()
    }

    constructor(width: Int, height: Int) : super(width, height) {}

    constructor(width: Int, height: Int, gravity: Int) : this(width, height) {
      this.gravity = gravity
    }

    constructor(source: LayoutParams) : super(source) {
      this.gravity = source.gravity
    }

    constructor(source: ViewGroup.LayoutParams) : super(source) {}

    constructor(source: ViewGroup.MarginLayoutParams) : super(source) {}

    companion object {
      private val FLAG_IS_OPENED = 0x1
      private val FLAG_IS_OPENING = 0x2
      private val FLAG_IS_CLOSING = 0x4
    }
  }

}
