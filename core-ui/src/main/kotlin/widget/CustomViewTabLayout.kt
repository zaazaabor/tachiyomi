/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tachiyomi.ui.widget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.database.DataSetObserver
import android.util.AttributeSet
import android.view.Gravity
import android.view.SoundEffectConstants
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.annotation.Dimension
import androidx.appcompat.app.ActionBar
import androidx.core.util.Pools
import androidx.core.view.GravityCompat
import androidx.core.view.PointerIconCompat
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SCROLL_STATE_DRAGGING
import androidx.viewpager.widget.ViewPager.SCROLL_STATE_IDLE
import androidx.viewpager.widget.ViewPager.SCROLL_STATE_SETTLING
import com.google.android.material.animation.AnimationUtils
import tachiyomi.core.ui.R
import java.lang.ref.WeakReference
import java.util.ArrayList

@Suppress("MemberVisibilityCanBePrivate", "unused")
open class CustomViewTabLayout(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = R.attr.tabStyle
) : HorizontalScrollView(context, attrs, defStyleAttr) {

  private val tabs = ArrayList<Tab>()
  private var selectedTab: Tab? = null

  protected val scrollContainer = LinearLayout(context)
  protected val tabContainer = LinearLayout(context)

  protected var tabPaddingStart: Int = 0
  protected var tabPaddingTop: Int = 0
  protected var tabPaddingEnd: Int = 0
  protected var tabPaddingBottom: Int = 0

  protected var tabIndicatorAnimationDuration: Int = 300

  private val selectedListeners = ArrayList<OnTabSelectedListener>()
  private var currentVpSelectedListener: OnTabSelectedListener? = null

  private var scrollAnimator: ValueAnimator? = null

  internal var viewPager: ViewPager? = null
  private var pagerAdapter: PagerAdapter? = null
  private var pagerAdapterObserver: DataSetObserver? = null
  private var pageChangeListener: TabLayoutOnPageChangeListener? = null
  private var adapterChangeListener: AdapterChangeListener? = null
  private var setupViewPagerImplicitly: Boolean = false

  // Pool we use as a simple RecyclerBin
  private val tabViewPool = Pools.SimplePool<TabView>(12)
  private val tabPool = Pools.SynchronizedPool<Tab>(16)

  init {
    isHorizontalScrollBarEnabled = false

    scrollContainer.gravity = GravityCompat.START
    tabContainer.gravity = GravityCompat.START

    scrollContainer.addView(tabContainer, 0, LayoutParams(
      LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT))
    super.addView(scrollContainer, 0, LayoutParams(
      LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT))

    val a = context.obtainStyledAttributes(
      attrs,
      com.google.android.material.R.styleable.TabLayout,
      defStyleAttr,
      com.google.android.material.R.style.Widget_Design_TabLayout
    )

    tabPaddingBottom =
      a.getDimensionPixelSize(com.google.android.material.R.styleable.TabLayout_tabPadding, 0)
    tabPaddingEnd = tabPaddingBottom
    tabPaddingTop = tabPaddingEnd
    tabPaddingStart = tabPaddingTop
    tabPaddingStart =
      a.getDimensionPixelSize(com.google.android.material.R.styleable.TabLayout_tabPaddingStart,
        tabPaddingStart)
    tabPaddingTop =
      a.getDimensionPixelSize(com.google.android.material.R.styleable.TabLayout_tabPaddingTop,
        tabPaddingTop)
    tabPaddingEnd =
      a.getDimensionPixelSize(com.google.android.material.R.styleable.TabLayout_tabPaddingEnd,
        tabPaddingEnd)
    tabPaddingBottom =
      a.getDimensionPixelSize(com.google.android.material.R.styleable.TabLayout_tabPaddingBottom,
        tabPaddingBottom)

    tabIndicatorAnimationDuration =
      a.getInt(com.google.android.material.R.styleable.TabLayout_tabIndicatorAnimationDuration,
        tabIndicatorAnimationDuration)

    a.recycle()
  }

  fun setScrollPosition(
    position: Int,
    positionOffset: Float,
    updateSelectedText: Boolean
  ) {
    val roundedPosition = Math.round(position + positionOffset)
    if (roundedPosition < 0 || roundedPosition >= tabContainer.childCount) {
      return
    }

    // Now update the scroll position, canceling any running animation
    val scrollAnimator = scrollAnimator
    if (scrollAnimator != null && scrollAnimator.isRunning) {
      scrollAnimator.cancel()
    }
    scrollTo(calculateScrollXForTab(position, positionOffset), 0)

    // Update the 'selected state' view as we scroll, if enabled
    if (updateSelectedText) {
      setSelectedTabView(roundedPosition)
    }
  }

  fun addTab(tab: Tab, position: Int = tabs.size, setSelected: Boolean = tabs.isEmpty()) {
    if (tab.parent !== this) {
      throw IllegalArgumentException("Tab belongs to a different TabLayout.")
    }
    configureTab(tab, position)
    addTabView(tab)

    if (setSelected) {
      tab.select()
    }
  }

  fun addOnTabSelectedListener(listener: OnTabSelectedListener) {
    if (!selectedListeners.contains(listener)) {
      selectedListeners.add(listener)
    }
  }

  fun removeOnTabSelectedListener(listener: OnTabSelectedListener) {
    selectedListeners.remove(listener)
  }

  fun clearOnTabSelectedListeners() {
    selectedListeners.clear()
  }

  fun newTab(): Tab {
    val tab = createTabFromPool()
    tab.parent = this
    tab.view = createTabView(tab)
    return tab
  }

  protected fun createTabFromPool(): Tab {
    var tab = tabPool.acquire()
    if (tab == null) {
      tab = Tab()
    }
    return tab
  }

  protected fun releaseFromTabPool(tab: Tab): Boolean {
    return tabPool.release(tab)
  }

  fun getTabCount(): Int {
    return tabs.size
  }

  fun getTabAt(index: Int): Tab? {
    return tabs.getOrNull(index)
  }

  fun getSelectedTabPosition(): Int {
    return selectedTab?.position ?: -1
  }

  fun removeTab(tab: Tab) {
    if (tab.parent !== this) {
      throw IllegalArgumentException("Tab does not belong to this TabLayout.")
    }

    removeTabAt(tab.position)
  }

  fun removeTabAt(position: Int) {
    val selectedTabPosition = selectedTab?.position ?: 0
    removeTabViewAt(position)

    val removedTab = tabs.removeAt(position)
    if (removedTab != null) {
      removedTab.reset()
      releaseFromTabPool(removedTab)
    }

    val newTabCount = tabs.size
    for (i in position until newTabCount) {
      tabs[i].position = i
    }

    if (selectedTabPosition == position) {
      selectTab(if (tabs.isEmpty()) null else tabs[Math.max(0, position - 1)])
    }
  }

  fun removeAllTabs() {
    // Remove all the views
    for (i in tabContainer.childCount - 1 downTo 0) {
      removeTabViewAt(i)
    }

    val i = tabs.iterator()
    while (i.hasNext()) {
      val tab = i.next()
      i.remove()
      tab.reset()
      releaseFromTabPool(tab)
    }

    selectedTab = null
  }

  fun setupWithViewPager(
    viewPager: ViewPager?,
    autoRefresh: Boolean = true,
    implicitSetup: Boolean = false
  ) {
    val currViewPager = this.viewPager
    if (currViewPager != null) {
      // If we've already been setup with a ViewPager, remove us from it
      pageChangeListener?.let {
        currViewPager.removeOnPageChangeListener(it)
      }
      adapterChangeListener?.let {
        currViewPager.removeOnAdapterChangeListener(it)
      }
    }

    currentVpSelectedListener?.let {
      // If we already have a tab selected listener for the ViewPager, remove it
      removeOnTabSelectedListener(it)
      currentVpSelectedListener = null
    }

    if (viewPager != null) {
      this.viewPager = viewPager

      // Add our custom OnPageChangeListener to the ViewPager
      if (pageChangeListener == null) {
        pageChangeListener = TabLayoutOnPageChangeListener(this)
      }
      val pageChangeListener = pageChangeListener!!
      pageChangeListener.reset()
      viewPager.addOnPageChangeListener(pageChangeListener)

      // Now we'll add a tab selected listener to set ViewPager's current item
      currentVpSelectedListener = ViewPagerOnTabSelectedListener(viewPager)
      val vpSelectedListener = currentVpSelectedListener!!
      addOnTabSelectedListener(vpSelectedListener)

      val adapter = viewPager.adapter
      if (adapter != null) {
        // Now we'll populate ourselves from the pager adapter, adding an observer if
        // autoRefresh is enabled
        setPagerAdapter(adapter, autoRefresh)
      }

      // Add a listener so that we're notified of any adapter changes
      if (adapterChangeListener == null) {
        adapterChangeListener = AdapterChangeListener()
      }
      val adapterChangeListener = adapterChangeListener!!
      adapterChangeListener.setAutoRefresh(autoRefresh)
      viewPager.addOnAdapterChangeListener(adapterChangeListener)

      // Now update the scroll position to match the ViewPager's current item
      setScrollPosition(viewPager.currentItem, 0f, true)
    } else {
      // We've been given a null ViewPager so we need to clear out the internal state,
      // listeners and observers
      this.viewPager = null
      setPagerAdapter(null, false)
    }

    setupViewPagerImplicitly = implicitSetup
  }

  override fun shouldDelayChildPressedState(): Boolean {
    // Only delay the pressed state if the tabs can scroll
    return getTabScrollRange() > 0
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    if (viewPager == null) {
      // If we don't have a ViewPager already, check if our parent is a ViewPager to
      // setup with it automatically
      val vp = parent
      if (vp is ViewPager) {
        // If we have a ViewPager parent and we've been added as part of its decor, let's
        // assume that we should automatically setup to display any titles
        setupWithViewPager(vp, true, true)
      }
    }
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()

    if (setupViewPagerImplicitly) {
      // If we've been setup with a ViewPager implicitly, let's clear out any listeners, etc
      setupWithViewPager(null)
      setupViewPagerImplicitly = false
    }
  }

  private fun getTabScrollRange(): Int {
    return Math.max(0, tabContainer.width - width - paddingLeft - paddingRight)
  }

  protected fun setPagerAdapter(adapter: PagerAdapter?, addObserver: Boolean) {
    if (pagerAdapter != null && pagerAdapterObserver != null) {
      // If we already have a PagerAdapter, unregister our observer
      pagerAdapter!!.unregisterDataSetObserver(pagerAdapterObserver!!)
    }

    pagerAdapter = adapter

    if (addObserver && adapter != null) {
      // Register our observer on the new adapter
      if (pagerAdapterObserver == null) {
        pagerAdapterObserver = PagerAdapterObserver()
      }
      adapter.registerDataSetObserver(pagerAdapterObserver!!)
    }

    // Finally make sure we reflect the new adapter
    populateFromPagerAdapter()
  }

  protected fun populateFromPagerAdapter() {
    removeAllTabs()

    val pagerAdapter = pagerAdapter
    if (pagerAdapter != null) {
      val adapterCount = pagerAdapter.count
      for (i in 0 until adapterCount) {
        addTab(newTab(), setSelected = false)
      }

      // Make sure we reflect the currently set ViewPager item
      val viewPager = viewPager
      if (viewPager != null && adapterCount > 0) {
        val curItem = viewPager.currentItem
        if (curItem != getSelectedTabPosition() && curItem < getTabCount()) {
          selectTab(getTabAt(curItem))
        }
      }
    }
  }

  private fun createTabView(tab: Tab): TabView {
    var tabView: TabView? = tabViewPool.acquire()
    if (tabView == null) {
      tabView = TabView(context)
    }
    tabView.tab = tab
    tabView.isFocusable = true
    return tabView
  }

  private fun configureTab(tab: Tab, position: Int) {
    tab.position = position
    tabs.add(position, tab)

    val count = tabs.size
    for (i in position + 1 until count) {
      tabs[i].position = i
    }
  }

  private fun addTabView(tab: Tab) {
    val tabView = tab.view ?: return
    tabView.isSelected = false
    tabView.isActivated = false
    tabContainer.addView(tabView, tab.position, createLayoutParamsForTabs())
  }

  private fun createLayoutParamsForTabs(): LinearLayout.LayoutParams {
    val lp = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
    updateTabViewLayoutParams(lp)
    return lp
  }

  private fun updateTabViewLayoutParams(lp: LinearLayout.LayoutParams) {
    lp.width = LinearLayout.LayoutParams.WRAP_CONTENT
    lp.weight = 0f
  }

  @Dimension(unit = Dimension.PX)
  internal fun dpToPx(@Dimension(unit = Dimension.DP) dps: Int): Int {
    return Math.round(resources.displayMetrics.density * dps)
  }

  @SuppressLint("SwitchIntDef")
  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    var heightSpec = heightMeasureSpec
    // If we have a MeasureSpec which allows us to decide our height, try and use the default
    // height
    val idealHeight = dpToPx(getDefaultHeight()) + paddingTop + paddingBottom

    when (View.MeasureSpec.getMode(heightMeasureSpec)) {
      View.MeasureSpec.AT_MOST -> heightSpec = View.MeasureSpec.makeMeasureSpec(
        Math.min(idealHeight, View.MeasureSpec.getSize(heightMeasureSpec)),
        View.MeasureSpec.EXACTLY)
      View.MeasureSpec.UNSPECIFIED -> heightSpec =
        View.MeasureSpec.makeMeasureSpec(idealHeight, View.MeasureSpec.EXACTLY)
    }

    // Now super measure itself using the (possibly) modified height spec
    super.onMeasure(widthMeasureSpec, heightSpec)
  }

  private fun removeTabViewAt(position: Int) {
    val view = tabContainer.getChildAt(position) as? TabView
    tabContainer.removeViewAt(position)
    if (view != null) {
      view.reset()
      tabViewPool.release(view)
    }
    requestLayout()
  }

  private fun childrenNeedLayout(): Boolean {
    val count = tabContainer.childCount
    for (i in 0 until count) {
      val children = tabContainer.getChildAt(i)
      if (children.width <= 0) {
        return true
      }
    }
    return false
  }

  private fun animateToTab(newPosition: Int) {
    if (newPosition == Tab.INVALID_POSITION) {
      return
    }

    if (windowToken == null
      || !ViewCompat.isLaidOut(this)
      || childrenNeedLayout()) {
      // If we don't have a window token, or we haven't been laid out yet just draw the new
      // position now
      setScrollPosition(newPosition, 0f, true)
      return
    }

    val startScrollX = scrollX
    val targetScrollX = calculateScrollXForTab(newPosition, 0f)

    if (startScrollX != targetScrollX) {
      ensureScrollAnimator()

      scrollAnimator?.setIntValues(startScrollX, targetScrollX)
      scrollAnimator?.start()
    }
  }

  private fun ensureScrollAnimator() {
    if (scrollAnimator == null) {
      val animator = ValueAnimator()
      scrollAnimator = animator
      animator.interpolator = AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR
      animator.duration = tabIndicatorAnimationDuration.toLong()
      animator.addUpdateListener { scrollTo(animator.animatedValue as Int, 0) }
    }
  }

  private fun setSelectedTabView(position: Int) {
    val tabCount = tabContainer.childCount
    if (position < tabCount) {
      for (i in 0 until tabCount) {
        val child = tabContainer.getChildAt(i)
        child.isSelected = i == position
        child.isActivated = i == position
      }
    }
  }

  fun selectTab(tab: Tab?, updateIndicator: Boolean = true) {
    val currentTab = selectedTab

    if (currentTab === tab) {
      if (currentTab != null) {
        dispatchTabReselected(tab!!)
        animateToTab(tab.position)
      }
    } else {
      val newPosition = if (tab != null) tab.position else Tab.INVALID_POSITION
      if (updateIndicator) {
        if ((currentTab == null || currentTab.position == Tab.INVALID_POSITION)
          && newPosition != Tab.INVALID_POSITION) {
          // If we don't currently have a tab, just draw the indicator
          setScrollPosition(newPosition, 0f, true)
        } else {
          animateToTab(newPosition)
        }
        if (newPosition != Tab.INVALID_POSITION) {
          setSelectedTabView(newPosition)
        }
      }
      // Setting selectedTab before dispatching 'tab unselected' events, so that currentTab's state
      // will be interpreted as unselected
      selectedTab = tab
      if (currentTab != null) {
        dispatchTabUnselected(currentTab)
      }
      if (tab != null) {
        dispatchTabSelected(tab)
      }
    }
  }

  private fun dispatchTabSelected(tab: Tab) {
    for (i in selectedListeners.indices.reversed()) {
      selectedListeners[i].onTabSelected(tab)
    }
  }

  private fun dispatchTabUnselected(tab: Tab) {
    for (i in selectedListeners.indices.reversed()) {
      selectedListeners[i].onTabUnselected(tab)
    }
  }

  private fun dispatchTabReselected(tab: Tab) {
    for (i in selectedListeners.indices.reversed()) {
      selectedListeners[i].onTabReselected(tab)
    }
  }

  private fun calculateScrollXForTab(position: Int, positionOffset: Float): Int {
    val selectedChild = tabContainer.getChildAt(position)
    val nextChild = if (position + 1 < tabContainer.childCount)
      tabContainer.getChildAt(position + 1)
    else
      null
    val selectedWidth = selectedChild?.width ?: 0
    val nextWidth = nextChild?.width ?: 0

    // base scroll amount: places center of tab in center of parent
    val scrollBase = selectedChild!!.left + selectedWidth / 2 - width / 2
    // offset amount: fraction of the distance between centers of tabs
    val scrollOffset = ((selectedWidth + nextWidth).toFloat() * 0.5f * positionOffset).toInt()

    return if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR)
      scrollBase + scrollOffset
    else
      scrollBase - scrollOffset
  }

  @Dimension(unit = Dimension.DP)
  private fun getDefaultHeight(): Int {
    return 48
  }

  override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
    // We don't care about the layout params of any views added to us, since we don't actually
    // add them. The only view we add is the SlidingTabStrip, which is done manually.
    // We return the default layout params so that we don't blow up if we're given a TabItem
    // without android:layout_* values.
    return generateDefaultLayoutParams()
  }

  class Tab {
    var tag: Any? = null
    var position: Int = INVALID_POSITION

    var parent: CustomViewTabLayout? = null
    var view: TabView? = null

    var customView: View? = null
      set (value) {
        field = value
        updateView()
      }

    fun select() {
      val parent = parent ?: throw IllegalArgumentException("Tab not attached to a TabLayout")
      parent.selectTab(this)
    }

    fun isSelected(): Boolean {
      val parent = parent ?: throw IllegalArgumentException("Tab not attached to a TabLayout")
      return parent.getSelectedTabPosition() == position
    }

    internal fun updateView() {
      view?.update()
    }

    internal fun reset() {
      parent = null
      view = null
      tag = null
      position = INVALID_POSITION
      customView = null
    }

    companion object {
      const val INVALID_POSITION = -1
    }
  }

  inner class TabView(context: Context) : LinearLayout(context) {

    var tab: Tab? = null
      set(tab) {
        if (tab !== this.tab) {
          field = tab
          update()
        }
      }

    private var customView: View? = null

    init {
      ViewCompat.setPaddingRelative(
        this, tabPaddingStart, tabPaddingTop, tabPaddingEnd, tabPaddingBottom)
      gravity = Gravity.CENTER
      orientation = LinearLayout.HORIZONTAL
      isClickable = true
      ViewCompat.setPointerIcon(
        this, PointerIconCompat.getSystemIcon(getContext(), PointerIconCompat.TYPE_HAND))
    }

    override fun performClick(): Boolean {
      val handled = super.performClick()

      val tab = tab
      if (tab != null) {
        if (!handled) {
          playSoundEffect(SoundEffectConstants.CLICK)
        }
        tab.select()
        return true
      } else {
        return handled
      }
    }

    override fun setSelected(selected: Boolean) {
      super.setSelected(selected)

      // Always dispatch this to the child views, regardless of whether the value has
      // changed
      customView?.isSelected = selected
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
      super.onInitializeAccessibilityEvent(event)
      // This view masquerades as an action bar tab.
      event.className = ActionBar.Tab::class.java.name
    }

    @TargetApi(14)
    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
      super.onInitializeAccessibilityNodeInfo(info)
      // This view masquerades as an action bar tab.
      info.className = ActionBar.Tab::class.java.name
    }

    fun reset() {
      tab = null
      isSelected = false
    }

    fun update() {
      val tab = this.tab
      val custom = tab?.customView
      if (custom != null) {
        val customParent = custom.parent
        if (customParent !== this) {
          if (customParent != null) {
            (customParent as ViewGroup).removeView(custom)
          }
          addView(custom)
        }
        customView = custom
      } else {
        // We do not have a custom view. Remove one if it already exists
        if (customView != null) {
          removeView(customView)
          customView = null
        }
      }

      // Finally update our selected state
      isSelected = tab != null && tab.isSelected()
    }

  }

  class TabLayoutOnPageChangeListener(tabLayout: CustomViewTabLayout) : ViewPager.OnPageChangeListener {
    private val tabLayoutRef = WeakReference(tabLayout)
    private var previousScrollState: Int = 0
    private var scrollState: Int = 0

    override fun onPageScrollStateChanged(state: Int) {
      previousScrollState = scrollState
      scrollState = state
    }

    override fun onPageScrolled(
      position: Int, positionOffset: Float, positionOffsetPixels: Int
    ) {
      val tabLayout = tabLayoutRef.get()
      if (tabLayout != null) {
        // Only update the text selection if we're not settling, or we are settling after
        // being dragged
        val updateText =
          scrollState != SCROLL_STATE_SETTLING || previousScrollState == SCROLL_STATE_DRAGGING
        tabLayout.setScrollPosition(position, positionOffset, updateText)
      }
    }

    override fun onPageSelected(position: Int) {
      val tabLayout = tabLayoutRef.get()
      if (tabLayout != null
        && tabLayout.getSelectedTabPosition() != position
        && position < tabLayout.getTabCount()) {
        // Select the tab, only updating the indicator if we're not being dragged/settled
        // (since onPageScrolled will handle that).
        val updateIndicator =
          scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_SETTLING
            && previousScrollState == SCROLL_STATE_IDLE
        tabLayout.selectTab(tabLayout.getTabAt(position), updateIndicator)
      }
    }

    internal fun reset() {
      scrollState = SCROLL_STATE_IDLE
      previousScrollState = scrollState
    }
  }

  interface OnTabSelectedListener {

    fun onTabSelected(tab: Tab)

    fun onTabUnselected(tab: Tab)

    fun onTabReselected(tab: Tab)
  }

  class ViewPagerOnTabSelectedListener(
    private val viewPager: ViewPager
  ) : OnTabSelectedListener {

    override fun onTabSelected(tab: Tab) {
      viewPager.currentItem = tab.position
    }

    override fun onTabUnselected(tab: Tab) {
      // No-op
    }

    override fun onTabReselected(tab: Tab) {
      // No-op
    }
  }

  private inner class PagerAdapterObserver internal constructor() : DataSetObserver() {
    override fun onChanged() {
      populateFromPagerAdapter()
    }

    override fun onInvalidated() {
      populateFromPagerAdapter()
    }
  }

  private inner class AdapterChangeListener : ViewPager.OnAdapterChangeListener {

    private var autoRefresh: Boolean = false

    override fun onAdapterChanged(
      viewPager: ViewPager,
      oldAdapter: PagerAdapter?,
      newAdapter: PagerAdapter?
    ) {
      if (this@CustomViewTabLayout.viewPager === viewPager) {
        setPagerAdapter(newAdapter, autoRefresh)
      }
    }

    fun setAutoRefresh(autoRefresh: Boolean) {
      this.autoRefresh = autoRefresh
    }
  }

}
