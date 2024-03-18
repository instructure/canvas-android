/*
 * Copyright (C) 2021 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.update

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.toDate
import com.instructure.pandautils.R
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

const val IMMEDIATE_THRESHOLD = 4
const val FLEXIBLE_THRESHOLD = 2
const val DAYS_FOR_FLEXIBLE_UPDATE = 10
const val CHANNEL_ID = "appUpdatesChannel"
const val FLEXIBLE_UPDATE_REQUEST_CODE = 1801
const val IMMEDIATE_UPDATE_REQUEST_CODE = 1802
const val NOTIFICATION_ID = 2801

class UpdateManager(private val appUpdateManager: AppUpdateManager,
                    private val notificationManager: NotificationManager,
                    private val updatePrefs: UpdatePrefs) {

    fun checkForInAppUpdate(activity: Activity) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                appUpdateManager.completeUpdate()
            } else {
                if (shouldShowUpdateNotification(appUpdateInfo)) {
                    startInAppUpdate(activity, appUpdateInfo)
                }
            }
        }
    }

    private fun startInAppUpdate(activity: Activity, appUpdateInfo: AppUpdateInfo) {
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE ||
                appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
            if (appUpdateInfo.updatePriority() >= IMMEDIATE_THRESHOLD) {
                appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        activity,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE)
                                .setAllowAssetPackDeletion(true)
                                .build(),
                        IMMEDIATE_UPDATE_REQUEST_CODE
                )
            } else if (appUpdateInfo.updatePriority() >= FLEXIBLE_THRESHOLD && (appUpdateInfo.clientVersionStalenessDays()
                    ?: 0) >= DAYS_FOR_FLEXIBLE_UPDATE
            ) {
                val listener = InstallStateUpdatedListener {
                    if (it.installStatus() == InstallStatus.DOWNLOADED) {
                        registerNotificationChannel(activity)
                        val intent = Intent(activity, activity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        val pendingIntent: PendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                        val builder = NotificationCompat.Builder(activity, CHANNEL_ID)
                                .setSmallIcon(activity.applicationInfo.icon)
                                .setContentTitle(activity.getString(R.string.appUpdateReadyTitle))
                                .setContentText(activity.getString(R.string.appUpdateReadyDescription))
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setAutoCancel(true)
                                .setContentIntent(pendingIntent)
                        with(NotificationManagerCompat.from(activity)) {
                            notify(NOTIFICATION_ID, builder.build())
                        }
                    }
                }

                appUpdateManager.registerListener(listener)

                appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        activity,
                        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE)
                                .setAllowAssetPackDeletion(true)
                                .build(),
                        FLEXIBLE_UPDATE_REQUEST_CODE
                )
            }
        }
    }

    private fun registerNotificationChannel(context: Context) {
        val name = context.getString(R.string.notificationChannelNameInAppUpdate)
        val descriptionText = context.getString(R.string.notificationChannelDescriptionInAppUpdate)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun shouldShowUpdateNotification(appUpdateInfo: AppUpdateInfo): Boolean {

        if (updatePrefs.lastUpdateNotificationDate.isBlank() || updatePrefs.lastUpdateNotificationVersionCode != appUpdateInfo.availableVersionCode()) {
            updatePrefs.lastUpdateNotificationDate = Date().toApiString()
            updatePrefs.lastUpdateNotificationVersionCode = appUpdateInfo.availableVersionCode()
            updatePrefs.lastUpdateNotificationCount = 1
            updatePrefs.hasShownThisStart = true
            return true
        }

        if (appUpdateInfo.updatePriority() >= IMMEDIATE_THRESHOLD && !updatePrefs.hasShownThisStart) {
            updatePrefs.hasShownThisStart = true
            return true
        }

        if (appUpdateInfo.updatePriority() in FLEXIBLE_THRESHOLD until IMMEDIATE_THRESHOLD) {
            val lastUpdateDate = updatePrefs.lastUpdateNotificationDate.toDate()
            val currentDate = Date()
            val diff = TimeUnit.DAYS.convert(abs(currentDate.time - lastUpdateDate!!.time), TimeUnit.MILLISECONDS)

            if (diff >= FLEXIBLE_UPDATE_NOTIFICATION_INTERVAL_DAYS && updatePrefs.lastUpdateNotificationCount <= FLEXIBLE_UPDATE_NOTIFICATION_MAX_COUNT) {
                updatePrefs.lastUpdateNotificationCount += 1
                updatePrefs.lastUpdateNotificationDate = Date().toApiString()
                return true
            }
        }

        return false
    }
}