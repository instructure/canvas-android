/*
 * Copyright (C) 2021 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.pandautils.services

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.impl.utils.futures.SettableFuture
import com.google.android.gms.tasks.OnCompleteListener
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.CommunicationChannelsManager
import com.instructure.canvasapi2.utils.Logger
import okhttp3.ResponseBody
import retrofit2.Response

class PushNotificationRegistrationWorker(appContext: Context, params: WorkerParameters) : ListenableWorker(appContext, params) {

    private val future = SettableFuture.create<Result>()

    override fun startWork(): ListenableFuture<Result> {
        Logger.e("PushNotificationRegistrationWorker : startWork()")
        val runnable = Runnable {
            var token: String? = ""
            var responseBody: Response<ResponseBody>? = null
            try {
                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                    val apiRunnable = Runnable {
                        try {
                            token = task.result

                            if (!task.isSuccessful || token == null) {
                                Logger.e("PushNotificationRegistrationWorker: startWork() - failed to get instanceId (token)")
                                future.set(Result.failure())
                                return@Runnable
                            }

                            responseBody = CommunicationChannelsManager.addNewPushCommunicationChannelSynchronous(
                                token!!,
                                object : StatusCallback<ResponseBody>() {})
                            if (responseBody != null && responseBody?.code() == 200) {
                                Logger.e("PushNotificationRegistrationWorker : startWork() - Success registering push notifications.")
                            } else {
                                // We won't reschedule as this will re-register when the app starts.
                                Logger.e("PushNotificationRegistrationWorker : startWork() - Error registering push notifications.")
                            }
                            future.set(Result.success())
                        } catch (e: Throwable) {
                            handleError(e, responseBody, token)
                        }
                    }
                    Thread(apiRunnable).start()
                })
            } catch (e: Throwable) {
                handleError(e, responseBody, token)
            }
        }

        Thread(runnable).start()

        return future
    }

    private fun handleError(e: Throwable, responseBody: Response<ResponseBody>?, token: String?) {
        FirebaseCrashlytics.getInstance().recordException(Exception("PushNotificationRegistrationWorker : startWork() - Error registering push notifications.\n" +
            "Error message: ${e.message}\n" +
            "Token: $token\n" +
            "Canvas Token Registration Response Body: $responseBody\n\t" +
            "isSuccessful: ${responseBody?.isSuccessful}\n\t" +
            "Body: ${responseBody?.body()}\n\t" +
            "Code: ${responseBody?.code()}\n\t" +
            "Error body: ${responseBody?.errorBody()?.string()}\n\t" +
            "Headers: ${responseBody?.headers()}\n\t" +
            "Message: ${responseBody?.message()}"))

        future.set(Result.failure())
    }

    companion object {
        fun scheduleJob(context: Context, isMasquerading: Boolean) {
            Logger.d("PushNotificationRegistrationWorker : scheduleJob() isMasquerading: $isMasquerading")

            if (!isMasquerading) {
                val constraints: Constraints = Constraints.Builder().apply {
                    setRequiredNetworkType(NetworkType.CONNECTED)
                }.build()

                val request = OneTimeWorkRequestBuilder<PushNotificationRegistrationWorker>()
                    .setConstraints(constraints)
                    .build()

                WorkManager.getInstance(context)
                    .enqueueUniqueWork(PushNotificationRegistrationWorker::class.java.simpleName, ExistingWorkPolicy.KEEP, request)
            }
        }
    }
}