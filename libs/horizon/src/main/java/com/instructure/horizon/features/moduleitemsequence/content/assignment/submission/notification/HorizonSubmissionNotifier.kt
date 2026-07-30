/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.features.moduleitemsequence.content.assignment.submission.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.HorizonActivity
import com.instructure.horizon.R
import com.instructure.pandautils.models.PushNotification
import com.instructure.pandautils.utils.Const

enum class SubmissionDeadlineThreshold(val minutesBefore: Long) {
    T24H(24 * 60),
    T1H(60)
}

class HorizonSubmissionNotifier(
    private val context: Context,
    private val notificationManager: NotificationManager,
    private val apiPrefs: ApiPrefs
) {

    private fun ensureChannel() {
        if (notificationManager.notificationChannels.any { it.id == CHANNEL_ID }) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.submissionNotificationChannel),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.submissionNotificationChannelDescription)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun postDeadlineReminder(
        submissionId: Long,
        assignmentName: String?,
        assignmentId: Long,
        courseId: Long,
        threshold: SubmissionDeadlineThreshold
    ) {
        val name = assignmentName.orEmpty()
        val (title, text) = when (threshold) {
            SubmissionDeadlineThreshold.T24H -> context.getString(R.string.submissionNotificationDeadline24hTitle) to
                context.getString(R.string.submissionNotificationDeadline24hText, name)
            SubmissionDeadlineThreshold.T1H -> context.getString(R.string.submissionNotificationDeadline1hTitle) to
                context.getString(R.string.submissionNotificationDeadline1hText, name)
        }
        notify(
            notificationId = deadlineNotificationId(submissionId, threshold),
            title = title,
            text = text,
            courseId = courseId,
            assignmentId = assignmentId,
            category = NotificationCompat.CATEGORY_REMINDER
        )
    }

    fun postSuccess(submissionId: Long, assignmentName: String?, assignmentId: Long, courseId: Long) {
        notify(
            notificationId = resultNotificationId(submissionId),
            title = context.getString(R.string.submissionNotificationSuccessTitle),
            text = context.getString(R.string.submissionNotificationSuccessText, assignmentName.orEmpty()),
            courseId = courseId,
            assignmentId = assignmentId,
            category = NotificationCompat.CATEGORY_STATUS
        )
    }

    fun postFailure(
        submissionId: Long,
        assignmentName: String?,
        assignmentId: Long,
        courseId: Long,
        reason: String?
    ) {
        val name = assignmentName.orEmpty()
        val text = if (reason.isNullOrBlank()) {
            context.getString(R.string.submissionNotificationFailureText, name)
        } else {
            context.getString(R.string.submissionNotificationFailureTextWithReason, name, reason)
        }
        notify(
            notificationId = resultNotificationId(submissionId),
            title = context.getString(R.string.submissionNotificationFailureTitle),
            text = text,
            courseId = courseId,
            assignmentId = assignmentId,
            category = NotificationCompat.CATEGORY_ERROR
        )
    }

    private fun notify(
        notificationId: Int,
        title: String,
        text: String,
        courseId: Long,
        assignmentId: Long,
        category: String
    ) {
        ensureChannel()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.notifications)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(assignmentDetailIntent(notificationId, courseId, assignmentId))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(category)
            .build()
        notificationManager.notify(notificationId, notification)
    }

    private fun assignmentDetailIntent(notificationId: Int, courseId: Long, assignmentId: Long): PendingIntent {
        val url = "${apiPrefs.fullDomain}/courses/$courseId/assignments/$assignmentId"
        val intent = Intent(context, HorizonActivity::class.java).apply {
            putExtra(Const.LOCAL_NOTIFICATION, true)
            putExtra(PushNotification.HTML_URL, url)
        }
        return PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun resultNotificationId(submissionId: Long) = submissionId.toInt()

    private fun deadlineNotificationId(submissionId: Long, threshold: SubmissionDeadlineThreshold): Int {
        val offset = when (threshold) {
            SubmissionDeadlineThreshold.T24H -> DEADLINE_24H_OFFSET
            SubmissionDeadlineThreshold.T1H -> DEADLINE_1H_OFFSET
        }
        return submissionId.toInt() + offset
    }

    companion object {
        const val CHANNEL_ID = "horizon_submissions"
        private const val DEADLINE_24H_OFFSET = 2_000_000
        private const val DEADLINE_1H_OFFSET = 3_000_000
    }
}
