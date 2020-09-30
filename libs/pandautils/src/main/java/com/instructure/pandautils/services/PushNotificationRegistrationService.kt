/*
 * Copyright (C) 2018 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.instructure.pandautils.services

import android.content.Context
import com.firebase.jobdispatcher.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.iid.FirebaseInstanceId
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.CommunicationChannelsManager
import com.instructure.canvasapi2.utils.Logger
import okhttp3.ResponseBody
import retrofit2.Response

class PushNotificationRegistrationService : JobService() {

    class YUNoWorkPushNotifsException(msg: String) : Throwable(msg)

    override fun onStartJob(job: JobParameters): Boolean {
        Logger.e("PushNotificationRegistrationService : onStartJob()")
        val runnable = Runnable {
            var firebaseInstance: FirebaseInstanceId? = null
            var token: String? = ""
            var responseBody: Response<ResponseBody>? = null
            try {
                firebaseInstance = FirebaseInstanceId.getInstance()
                firebaseInstance.instanceId.addOnCompleteListener(OnCompleteListener { task ->
                    val apiRunnable = Runnable {
                        try {
                            token = task.result?.token

                            if (!task.isSuccessful || token == null) {
                                Logger.e("PushNotificationRegistrationService: onStartJob() - failed to get instanceId (token)")
                                jobFinished(job, false)
                                return@Runnable
                            }

                            responseBody = CommunicationChannelsManager.addNewPushCommunicationChannelSynchronous(
                                token!!,
                                object : StatusCallback<ResponseBody>() {})
                            if (responseBody != null && responseBody?.code() == 200) {
                                Logger.e("PushNotificationRegistrationService : onStartJob() - Success registering push notifications.")
                            } else {
                                // We won't reschedule as this will re-register when the app starts.
                                Logger.e("PushNotificationRegistrationService : onStartJob() - Error registering push notifications.")
                            }
                            jobFinished(job, false)
                        } catch (e: Throwable) {
                            handleError(e, job, firebaseInstance, responseBody, token)
                        }
                    }
                    Thread(apiRunnable).start()
                })
            } catch (e: Throwable) {
                handleError(e, job, firebaseInstance, responseBody, token)
            }
        }

        Thread(runnable).start()

        return true // Answers the question: "Is there still work going on?"
    }

    override fun onStopJob(job: JobParameters): Boolean {
        return false // Answers the question: "Should this job be retried?"
    }

    private fun handleError(e: Throwable, job: JobParameters, firebaseInstance: FirebaseInstanceId?, responseBody: Response<ResponseBody>?, token: String?) {
        FirebaseCrashlytics.getInstance().recordException(YUNoWorkPushNotifsException("PushNotificationRegistrationService : onStartJob() - Error registering push notifications.\n" +
            "Error message: ${e.message}\n" +
            "InstanceId: $firebaseInstance\n\t" +
                "Id: ${firebaseInstance?.id}\n\t" +
                "Creation Time: ${firebaseInstance?.creationTime}\n\t" +
                "Token: $token\n" +
            "Canvas Token Registration Response Body: $responseBody\n\t" +
                "isSuccessful: ${responseBody?.isSuccessful}\n\t" +
                "Body: ${responseBody?.body()}\n\t" +
                "Code: ${responseBody?.code()}\n\t" +
                "Error body: ${responseBody?.errorBody()?.string()}\n\t" +
                "Headers: ${responseBody?.headers()}\n\t" +
                "Message: ${responseBody?.message()}"))

        jobFinished(job, false)
    }

    companion object {
        fun scheduleJob(context: Context, isMasquerading: Boolean) {
            Logger.d("PushNotificationRegistrationService : scheduleJob() isMasquerading: $isMasquerading")

            if(!isMasquerading) {
                val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))
                val job = dispatcher.newJobBuilder()
                        .setService(PushNotificationRegistrationService::class.java)
                        .setTag(PushNotificationRegistrationService::class.java.simpleName)
                        .setTrigger(Trigger.NOW)
                        .setRecurring(false)
                        .setReplaceCurrent(false)
                        .addConstraint(Constraint.ON_ANY_NETWORK)
                        .build()
                // Attempt to clear out any bad jobs so a real one can work
                dispatcher.cancelAll()
                dispatcher.schedule(job)
            }
        }
    }
}
