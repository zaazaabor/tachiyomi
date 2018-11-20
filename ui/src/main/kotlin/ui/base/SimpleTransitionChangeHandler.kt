/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.base

import android.transition.Transition
import android.transition.TransitionSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.bluelinelabs.conductor.changehandler.TransitionChangeHandler
import com.bluelinelabs.conductor.changehandler.TransitionChangeHandler.OnTransitionPreparedListener
import com.bluelinelabs.conductor.internal.TransitionUtils
import java.util.ArrayList

/**
 * A TransitionChangeHandler that facilitates using different Transitions for the entering view, the exiting view,
 * and shared elements between the two.
 */
// Much of this class is based on FragmentTransition.java and FragmentTransitionCompat21.java from the Android support library
abstract class SimpleTransitionChangeHandler : TransitionChangeHandler() {

  private var exitTransition: Transition? = null

  private var enterTransition: Transition? = null

  override fun getTransition(
    container: ViewGroup, from: View?, to: View?, isPush: Boolean
  ): Transition {
    exitTransition = getExitTransition(container, from, to, isPush)
    enterTransition = getEnterTransition(container, from, to, isPush)

    if (enterTransition == null && exitTransition == null) {
      throw IllegalStateException(
        "SimpleTransitionChangeHandler must have at least one transaction.")
    }

    return mergeTransitions(isPush)
  }

  override fun prepareForTransition(
    container: ViewGroup,
    from: View?,
    to: View?,
    transition: Transition,
    isPush: Boolean,
    onTransitionPreparedListener: TransitionChangeHandler.OnTransitionPreparedListener
  ) {
    val listener = OnTransitionPreparedListener {
      configureTransition(container, from, to, transition, isPush)
      onTransitionPreparedListener.onPrepared()
    }

    listener.onPrepared()
  }

  private fun configureTransition(
    container: ViewGroup, from: View?, to: View?, transition: Transition, isPush: Boolean
  ) {
    val nonExistentView = View(container.context)
    var exitTransition = exitTransition

    val exitingViews = if (exitTransition != null) {
      configureEnteringExitingViews(exitTransition, from, nonExistentView).toMutableList()
    } else {
      null
    }
    if (exitingViews == null || exitingViews.isEmpty()) {
      exitTransition = null
    }

    if (enterTransition != null) {
      enterTransition!!.addTarget(nonExistentView)
    }

    val enteringViews = ArrayList<View>()
    scheduleRemoveTargets(transition, enterTransition, enteringViews, exitTransition, exitingViews)
    scheduleTargetChange(container, to, nonExistentView, enteringViews, exitingViews)
  }

  private fun scheduleTargetChange(
    container: ViewGroup, to: View?, nonExistentView: View,
    enteringViews: MutableList<View>, exitingViews: MutableList<View>?
  ) {
    OneShotPreDrawListener.add(true, container, Runnable {
      val enterTransition = enterTransition
      if (enterTransition != null) {
        enterTransition.removeTarget(nonExistentView)
        val views = configureEnteringExitingViews(enterTransition, to, nonExistentView)
        enteringViews.addAll(views)
      }

      if (exitingViews != null) {
        val exitTransition = exitTransition
        if (exitTransition != null) {
          val tempExiting = ArrayList<View>()
          tempExiting.add(nonExistentView)
          TransitionUtils.replaceTargets(exitTransition, exitingViews, tempExiting)
        }
        exitingViews.clear()
        exitingViews.add(nonExistentView)
      }
    })
  }

  private fun mergeTransitions(isPush: Boolean): Transition {
    val overlap =
      enterTransition == null || exitTransition == null || allowTransitionOverlap(isPush)

    if (overlap) {
      return TransitionUtils.mergeTransitions(TransitionSet.ORDERING_TOGETHER, exitTransition,
        enterTransition)
    } else {
      val staggered =
        TransitionUtils.mergeTransitions(TransitionSet.ORDERING_SEQUENTIAL, exitTransition,
          enterTransition)
      return TransitionUtils.mergeTransitions(TransitionSet.ORDERING_TOGETHER, staggered)
    }
  }

  internal fun configureEnteringExitingViews(
    transition: Transition, view: View?,
    nonExistentView: View
  ): List<View> {
    val viewList = ArrayList<View>()
    if (view != null) {
      captureTransitioningViews(viewList, view)
    }
    if (!viewList.isEmpty()) {
      viewList.add(nonExistentView)
      TransitionUtils.addTargets(transition, viewList)
    }
    return viewList
  }

  private fun captureTransitioningViews(transitioningViews: MutableList<View>, view: View) {
    if (view.visibility == View.VISIBLE) {
      if (view is ViewGroup) {
        if (view.isTransitionGroup) {
          transitioningViews.add(view)
        } else {
          val count = view.childCount
          for (i in 0 until count) {
            val child = view.getChildAt(i)
            captureTransitioningViews(transitioningViews, child)
          }
        }
      } else {
        transitioningViews.add(view)
      }
    }
  }

  private fun scheduleRemoveTargets(
    overallTransition: Transition,
    enterTransition: Transition?, enteringViews: List<View>?,
    exitTransition: Transition?, exitingViews: List<View>?
  ) {
    overallTransition.addListener(object : Transition.TransitionListener {
      override fun onTransitionStart(transition: Transition) {
        if (enterTransition != null && enteringViews != null) {
          TransitionUtils.replaceTargets(enterTransition, enteringViews, null)
        }
        if (exitTransition != null && exitingViews != null) {
          TransitionUtils.replaceTargets(exitTransition, exitingViews, null)
        }
      }

      override fun onTransitionEnd(transition: Transition) {}

      override fun onTransitionCancel(transition: Transition) {}

      override fun onTransitionPause(transition: Transition) {}

      override fun onTransitionResume(transition: Transition) {}
    })
  }

  /**
   * Should return the transition that will be used on the exiting ("from") view, if one is desired.
   */
  abstract fun getExitTransition(
    container: ViewGroup, from: View?, to: View?, isPush: Boolean
  ): Transition?

  /**
   * Should return the transition that will be used on the entering ("to") view, if one is desired.
   */

  abstract fun getEnterTransition(
    container: ViewGroup, from: View?, to: View?, isPush: Boolean
  ): Transition?

  /**
   * Should return whether or not the the exit transition and enter transition should overlap. If true,
   * the enter transition will start as soon as possible. Otherwise, the enter transition will wait until the
   * completion of the exit transition. Defaults to true.
   */
  fun allowTransitionOverlap(isPush: Boolean): Boolean {
    return true
  }

  private class OneShotPreDrawListener private constructor(
    private val preDrawReturnValue: Boolean,
    private val view: View,
    private val runnable: Runnable
  ) : ViewTreeObserver.OnPreDrawListener, View.OnAttachStateChangeListener {

    private var viewTreeObserver = view.viewTreeObserver

    override fun onPreDraw(): Boolean {
      removeListener()
      runnable.run()
      return preDrawReturnValue
    }

    private fun removeListener() {
      if (viewTreeObserver!!.isAlive) {
        viewTreeObserver!!.removeOnPreDrawListener(this)
      } else {
        view.viewTreeObserver.removeOnPreDrawListener(this)
      }
      view.removeOnAttachStateChangeListener(this)
    }

    override fun onViewAttachedToWindow(v: View) {
      viewTreeObserver = v.viewTreeObserver
    }

    override fun onViewDetachedFromWindow(v: View) {
      removeListener()
    }

    companion object {

      fun add(preDrawReturnValue: Boolean, view: View, runnable: Runnable): OneShotPreDrawListener {
        val listener = OneShotPreDrawListener(preDrawReturnValue, view, runnable)
        view.viewTreeObserver.addOnPreDrawListener(listener)
        view.addOnAttachStateChangeListener(listener)
        return listener
      }
    }

  }

}
