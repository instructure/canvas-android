/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

package com.instructure.student.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Html
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.managers.InboxManager
import com.instructure.canvasapi2.managers.StreamManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.StreamItem
import com.instructure.canvasapi2.utils.*
import com.instructure.student.R
import com.instructure.student.activity.NotificationWidgetRouter
import com.instructure.student.util.StringUtilities
import kotlinx.coroutines.Job
import java.io.Serializable
import java.util.Locale

class NotificationViewWidgetService : BaseRemoteViewsService(), Serializable {

    private var apiCallsJob: Job? = null

    override fun onGetViewFactory(intent: Intent): RemoteViewsService.RemoteViewsFactory = NotificationsRowFactory(intent)

    private inner class NotificationsRowFactory(private val intent: Intent) : CanvasWidgetRowFactory<StreamItem>() {

        override val layoutId: Int
            get() = getLayoutIdValue()

        override fun giveMeAppWidgetId(): Int = BaseRemoteViewsService.getAppWidgetId(intent)

        private fun getLayoutIdValue(): Int {
            if(intent.data != null) {
                val appWidgetId = Integer.valueOf(intent.data!!.schemeSpecificPart)
                if (BaseRemoteViewsService.shouldHideDetails(appWidgetId)) {
                    return R.layout.listview_widget_notifications_minimum_item_row
                }
            }
            return R.layout.listview_widget_notifications_item_row
        }

        @Suppress("DEPRECATION")
        override fun setViewData(streamItem: StreamItem, row: RemoteViews) {
            val appWidgetId = BaseRemoteViewsService.getAppWidgetId(intent)

            row.setViewVisibility(R.id.icon, View.VISIBLE)
            row.setImageViewResource(R.id.icon, getDrawableId(streamItem))
            row.setTextViewText(R.id.title, streamItem.getTitle(ContextKeeper.appContext))
            row.setTextColor(R.id.title, BaseRemoteViewsService.getWidgetTextColor(appWidgetId, applicationContext))

            if (streamItem.canvasContext != null && streamItem.canvasContext?.type != CanvasContext.Type.USER) {
                row.setInt(R.id.icon, "setColorFilter", getCanvasContextTextColor(appWidgetId, streamItem.canvasContext))
            } else if (streamItem.getStreamItemType() == StreamItem.Type.CONVERSATION) {
                val color = if(streamItem.canvasContext != null) getCanvasContextTextColor(appWidgetId, streamItem.canvasContext)
                            else BaseRemoteViewsService.getWidgetTextColor(appWidgetId, ContextKeeper.appContext)
                row.setInt(R.id.icon, "setColorFilter", color)
            } else {
                val color = if(streamItem.canvasContext != null) getCanvasContextTextColor(appWidgetId, streamItem.canvasContext)
                            else ContextCompat.getColor(applicationContext, R.color.textDanger)
                row.setInt(R.id.icon, "setColorFilter", color)
            }

            if (!BaseRemoteViewsService.shouldHideDetails(appWidgetId)) {
                val restrictQuantitativeData = (streamItem.canvasContext as? Course)?.settings?.restrictQuantitativeData ?: false
                val gradingScheme = (streamItem.canvasContext as? Course)?.gradingScheme ?: emptyList()
                val message = streamItem.getMessage(ContextKeeper.appContext, restrictQuantitativeData, gradingScheme)
                if (message != null) {
                    row.setTextViewText(R.id.message, StringUtilities.simplifyHTML(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY)))
                    row.setTextColor(R.id.message, BaseRemoteViewsService.getWidgetSecondaryTextColor(appWidgetId, applicationContext))
                } else {
                    row.setTextViewText(R.id.message, "")
                    row.setViewVisibility(R.id.message, View.GONE)
                }
            }

            var courseAndDate = ""
            if (streamItem.contextType == CanvasContext.Type.COURSE && streamItem.canvasContext != null) {
                courseAndDate = streamItem.canvasContext?.secondaryName + " "
            }
            courseAndDate += DateHelper.getDateTimeString(ContextKeeper.appContext, streamItem.updatedDate)
            row.setTextViewText(R.id.course_and_date, courseAndDate)
            row.setTextColor(R.id.course_and_date, BaseRemoteViewsService.getWidgetSecondaryTextColor(appWidgetId, applicationContext))

            row.setOnClickFillInIntent(R.id.widget_root, createIntent(streamItem))

        }

        override fun createIntent(streamItem: StreamItem): Intent = NotificationWidgetRouter.createIntent(ContextKeeper.appContext, streamItem)

        override fun clearViewData(row: RemoteViews) {
            row.setTextViewText(R.id.course_and_date, "")
            row.setTextViewText(R.id.message, "")
            row.setTextViewText(R.id.title, "")
            row.setViewVisibility(R.id.icon, View.GONE)
        }

        private fun getDrawableId(streamItem: StreamItem): Int {
            when (streamItem.getStreamItemType()) {
                StreamItem.Type.DISCUSSION_TOPIC, StreamItem.Type.DISCUSSION_ENTRY, StreamItem.Type.DISCUSSION_MENTION -> return R.drawable.ic_discussion
                StreamItem.Type.ANNOUNCEMENT -> return R.drawable.ic_announcement
                StreamItem.Type.SUBMISSION -> return R.drawable.ic_assignment
                StreamItem.Type.CONVERSATION -> return R.drawable.ic_inbox
                StreamItem.Type.MESSAGE ->
                    //a message could be related to an assignment, check the category
                    return when {
                        streamItem.contextType == CanvasContext.Type.COURSE -> R.drawable.ic_assignment
                        streamItem.notificationCategory.lowercase(Locale.getDefault()).contains("assignment graded") -> R.drawable.ic_grades
                        else -> R.drawable.ic_user_avatar
                    }
                StreamItem.Type.CONFERENCE -> return R.drawable.ic_conferences
                StreamItem.Type.COLLABORATION -> return R.drawable.ic_collaborations
                else -> return R.drawable.ic_announcement
            }
        }

        override fun loadData() {
            if(NetworkUtils.isNetworkAvailable && ApiPrefs.user != null) {
                try {
                    val courses = CourseManager.getCoursesSynchronousWithGradingScheme(true)
                            .filter { it.isFavorite && !it.accessRestrictedByDate && !it.isInvited() }
                    val groups = GroupManager.getFavoriteGroupsSynchronous(false)
                    val userStream = StreamManager.getUserStreamSynchronous(25, true).toMutableList()

                    userStream.sort()
                    userStream.reverse()

                    val courseMap = CourseManager.createCourseMap(courses)
                    val groupMap = GroupManager.createGroupMap(groups)

                    for (streamItem in userStream) {
                        streamItem.setCanvasContextFromMap(courseMap, groupMap)

                        // Load conversations if needed
                        if (streamItem.getStreamItemType() == StreamItem.Type.CONVERSATION) {
                            val conversation = InboxManager.getConversationSynchronous(streamItem.conversationId, true)
                            streamItem.setConversation(ContextKeeper.appContext, conversation, ApiPrefs.user!!.id, ContextKeeper.appContext.resources.getString(R.string.monologue))
                        }
                    }

                    setData(userStream)
                } catch (e: Throwable) {
                    Logger.e("Could not load " + this::class.java.simpleName + " widget. " + e.message)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        apiCallsJob?.cancel()
    }

    companion object {

        fun createIntent(context: Context, appWidgetId: Int): Intent {
            val intent = Intent(context, NotificationViewWidgetService::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.data = Uri.fromParts("appWidgetId", appWidgetId.toString(), null)
            return intent
        }
    }
}
