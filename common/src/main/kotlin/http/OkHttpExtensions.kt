/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.http

import io.reactivex.Single
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Returns a single of the execution of a network request or an error if the server is unreachable.
 * It's the responsibility of the caller to move execution to a background thread.
 */
fun Call.asSingle(): Single<Response> {
  return Single.fromCallable {
    clone().execute()
  }
}

/**
 * Returns a single of the execution of a network request or an error if the response is
 * unsuccessful. It's the responsibility of the caller to move execution to a background thread.
 */
fun Call.asSingleSuccess(): Single<Response> {
  return asSingle().doOnSuccess { response ->
    if (!response.isSuccessful) {
      response.close()
      throw Exception("HTTP error ${response.code()}")
    }
  }
}

suspend fun Call.await(): Response {
  return suspendCancellableCoroutine { continuation ->
    continuation.invokeOnCancellation {
      cancel()
    }
    enqueue(object : Callback {
      override fun onResponse(call: Call, response: Response) {
        if (response.isSuccessful) {
          continuation.resume(response)
        } else {
          continuation.resumeWithException(Exception("HTTP error ${response.code()}"))
        }
      }

      override fun onFailure(call: Call, e: IOException) {
        continuation.resumeWithException(e)
      }
    })
  }
}

suspend fun Call.awaitResponse(): Response {
  return suspendCancellableCoroutine { continuation ->
    continuation.invokeOnCancellation {
      cancel()
    }
    enqueue(object : Callback {
      override fun onResponse(call: Call, response: Response) {
        continuation.resume(response)
      }

      override fun onFailure(call: Call, e: IOException) {
        continuation.resumeWithException(e)
      }
    })
  }
}

/**
 * Returns a new call for this [request] that allows listening for the progress of the response
 * through a [listener].
 */
fun OkHttpClient.newCallWithProgress(request: Request, listener: ProgressListener): Call {
  val progressClient = newBuilder()
    .cache(null)
    .addNetworkInterceptor { chain ->
      val originalResponse = chain.proceed(chain.request())
      originalResponse.newBuilder()
        .body(ProgressResponseBody(originalResponse.body()!!, listener))
        .build()
    }
    .build()

  return progressClient.newCall(request)
}
