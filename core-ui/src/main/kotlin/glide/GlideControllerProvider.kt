/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.glide

import android.content.Context
import android.view.View
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.manager.Lifecycle
import com.bumptech.glide.manager.LifecycleListener
import com.bumptech.glide.manager.RequestManagerTreeNode
import com.bumptech.glide.util.Util
import java.util.Collections
import java.util.WeakHashMap

class GlideControllerProvider(
  private val controller: Controller
) : RequestManagerTreeNode, GlideProvider {

  private var lifecycle: ControllerLifecycle? = null

  private var requestManager: GlideRequests? = null

  init {
    controller.addLifecycleListener(object : Controller.LifecycleListener() {
      var hasDestroyedGlide: Boolean = false
      var hasExited: Boolean = false

      override fun postContextAvailable(controller: Controller, context: Context) {
        super.postContextAvailable(controller, context)
        lifecycle = ControllerLifecycle()
        requestManager = GlideRequests(Glide.get(context.applicationContext), lifecycle!!,
          this@GlideControllerProvider, context.applicationContext)
        hasDestroyedGlide = false
      }

      override fun postAttach(controller: Controller, view: View) {
        lifecycle?.onStart()
      }

      override fun postDetach(controller: Controller, view: View) {
        lifecycle?.onStop()
      }

      override fun postDestroy(controller: Controller) {
        // Last controllers in the backstack may be destroyed without transition (onChangeEnd() not getting called)
        val isLast = !controller.router.hasRootController()
        if ((hasExited || isLast) && !hasDestroyedGlide) {
          destroyGlide()
        }
      }

      override fun onChangeEnd(
        controller: Controller,
        changeHandler: ControllerChangeHandler,
        changeType: ControllerChangeType
      ) {
        // onChangeEnd() could be called after postDestroy(). We prefer to release Glide as
        // late as possible because releasing Glide clears out all ImageViews and they
        // appear blank during a transition.
        hasExited = !changeType.isEnter
        val viewDestroyed = controller.view == null
        if (hasExited && viewDestroyed && !hasDestroyedGlide) {
          destroyGlide()
        }
      }

      private fun destroyGlide() {
        lifecycle?.onDestroy()
        lifecycle = null
        requestManager = null
        hasDestroyedGlide = true
      }
    })
  }

  override fun get(): GlideRequests {
    if (controller.activity == null) {
      throw IllegalArgumentException(
        "You cannot start a load until the Controller has been bound to a Context.")
    }

    return requestManager ?: throw UninitializedPropertyAccessException(
      "requestManager not yet initialized for the given controller")
  }

  override fun getDescendants(): Set<RequestManager> = collectRequestManagers(controller)

  /**
   * Recursively gathers the [RequestManager]s of a given [Controller] and all its child controllers.
   * The [Controller]s in the hierarchy must implement [GlideController] in order for their
   * request managers to be collected.
   */
  private fun collectRequestManagers(
    controller: Controller,
    collected: MutableSet<RequestManager> = HashSet()
  ): Set<RequestManager> {

    if (!controller.isDestroyed && !controller.isBeingDestroyed) {
      if (controller is GlideController) {
        controller.glideProvider.requestManager?.let {
          collected.add(it)
        }
      }

      controller.childRouters
        .flatMap { childRouter -> childRouter.backstack }
        .map { routerTransaction -> routerTransaction.controller() }
        .forEach { controlr -> collectRequestManagers(controlr, collected) }
    }

    return collected
  }

  /**
   * A [com.bumptech.glide.manager.Lifecycle] implementation for tracking and notifying
   * listeners of [com.bluelinelabs.conductor.Controller] lifecycle events.
   */
  private class ControllerLifecycle : Lifecycle {

    private val lifecycleListeners =
      Collections.newSetFromMap(WeakHashMap<LifecycleListener, Boolean>())
    private var isStarted: Boolean = false
    private var isDestroyed: Boolean = false

    /**
     * Adds the given listener to the list of listeners to be notified on each lifecycle event.
     *
     *  The latest lifecycle event will be called on the given listener synchronously in this
     * method. If the activity or fragment is stopped, [LifecycleListener.onStop]} will be
     * called, and same for onStart and onDestroy.
     *
     *  Note - [com.bumptech.glide.manager.LifecycleListener]s that are added more than once
     * will have their lifecycle methods called more than once. It is the caller's responsibility to
     * avoid adding listeners multiple times.
     */
    override fun addListener(listener: LifecycleListener) {
      lifecycleListeners.add(listener)

      when {
        isDestroyed -> listener.onDestroy()
        isStarted -> listener.onStart()
        else -> listener.onStop()
      }
    }

    override fun removeListener(listener: LifecycleListener) {
      lifecycleListeners.remove(listener)
    }

    fun onStart() {
      isStarted = true
      for (lifecycleListener in Util.getSnapshot(lifecycleListeners)) {
        lifecycleListener.onStart()
      }
    }

    fun onStop() {
      isStarted = false
      for (lifecycleListener in Util.getSnapshot(lifecycleListeners)) {
        lifecycleListener.onStop()
      }
    }

    fun onDestroy() {
      isDestroyed = true
      for (lifecycleListener in Util.getSnapshot(lifecycleListeners)) {
        lifecycleListener.onDestroy()
      }
    }
  }
}
