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
import android.text.TextUtils
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.instructure.student.R
import com.instructure.student.activity.InterwebsToApplication
import com.instructure.canvasapi2.managers.CalendarEventManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.managers.ToDoManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.models.ToDo
import com.instructure.canvasapi2.utils.*
import com.instructure.pandautils.utils.ColorKeeper
import java.io.Serializable
import java.util.*

class TodoViewWidgetService : BaseRemoteViewsService(), Serializable {

    override fun onGetViewFactory(intent: Intent): RemoteViewsService.RemoteViewsFactory {
        return TodoViewsFactory(intent)
    }

    private inner class TodoViewsFactory(private val intent: Intent) : CanvasWidgetRowFactory<ToDo>() {

        public override val layoutId: Int
            get() {
                if (BaseRemoteViewsService.shouldHideDetails(BaseRemoteViewsService.getAppWidgetId(intent))) {
                    return R.layout.listview_widget_todo_minimum_item_row
                }
                return R.layout.listview_widget_todo_item_row
            }

        override fun giveMeAppWidgetId(): Int {
            return BaseRemoteViewsService.getAppWidgetId(intent)
        }

        override fun setViewData(streamItem: ToDo, row: RemoteViews) {

            row.setViewVisibility(R.id.icon, View.VISIBLE)

            when {
                streamItem.type == ToDo.Type.UpcomingEvent -> row.setImageViewResource(R.id.icon, R.drawable.ic_calendar)
                streamItem.assignment!!.quizId > 0 -> row.setImageViewResource(R.id.icon, R.drawable.ic_quiz)
                streamItem.assignment!!.discussionTopicHeader != null -> row.setImageViewResource(R.id.icon, R.drawable.ic_discussion)
                else -> row.setImageViewResource(R.id.icon, R.drawable.ic_assignment)
            }

            if (streamItem.canvasContext != null && streamItem.canvasContext!!.type != CanvasContext.Type.USER) {
                row.setInt(R.id.icon, "setColorFilter", ColorKeeper.getOrGenerateColor(streamItem.canvasContext))
            } else {
                row.setInt(R.id.icon, "setColorFilter", R.color.textDanger)
            }

            val appWidgetId = BaseRemoteViewsService.getAppWidgetId(intent)
            row.setTextColor(R.id.title, BaseRemoteViewsService.getWidgetTextColor(appWidgetId, applicationContext))

            val title = streamItem.title
            row.setTextViewText(R.id.title, title)

            if (BaseRemoteViewsService.shouldHideDetails(appWidgetId)) {
                if (streamItem.dueDate != null) {
                    val formattedDueDate = DateHelper.getDateTimeString(ContextKeeper.appContext, streamItem.dueDate)
                    row.setTextViewText(R.id.message, formattedDueDate)
                    row.setViewVisibility(R.id.message, View.VISIBLE)
                    row.setTextColor(R.id.message, BaseRemoteViewsService.getWidgetSecondaryTextColor(appWidgetId, applicationContext))
                } else {
                    row.setViewVisibility(R.id.message, View.GONE)
                }
            } else {
                val message = formatDetailsString(streamItem, getCourseCode(streamItem))
                if (!TextUtils.isEmpty(message)) {
                    row.setTextViewText(R.id.message, message)
                    row.setViewVisibility(R.id.message, View.VISIBLE)
                    row.setTextColor(R.id.message, BaseRemoteViewsService.getWidgetSecondaryTextColor(appWidgetId, applicationContext))
                } else {
                    row.setViewVisibility(R.id.message, View.GONE)
                }

                val dueDate = streamItem.dueDate
                if (dueDate != null) {
                    val formattedDueDate = DateHelper.getDateTimeString(ContextKeeper.appContext, streamItem.dueDate)
                    row.setTextViewText(R.id.course_and_date, formattedDueDate)
                    row.setViewVisibility(R.id.course_and_date, View.VISIBLE)
                    row.setTextColor(R.id.course_and_date, BaseRemoteViewsService.getWidgetSecondaryTextColor(appWidgetId, applicationContext))
                } else {
                    row.setViewVisibility(R.id.course_and_date, View.GONE)
                }
            }

            //get assignment description
            row.setOnClickFillInIntent(R.id.widget_root, createIntent(streamItem))
        }

        override fun clearViewData(row: RemoteViews) {
            row.setTextViewText(R.id.course_and_date, "")
            row.setTextViewText(R.id.message, "")
            row.setTextViewText(R.id.title, "")
            row.setViewVisibility(R.id.icon, View.GONE)
        }

        override fun createIntent(streamItem: ToDo): Intent {
            // It's possible we cant route to to do so we just go to the list.
            var url = ""
            if (streamItem.assignment != null) {
                // Launch assignment details fragment.
                url = streamItem.assignment!!.htmlUrl!!
            } else if (streamItem.scheduleItem != null) {
                // It's a Calendar event from the Upcoming API.
                url = streamItem.scheduleItem!!.htmlUrl!!
            }

            val uri = Uri.parse(url)
            return InterwebsToApplication.createIntent(ContextKeeper.appContext, uri)
        }

        private fun formatDetailsString(toDo: ToDo, courseCode: String?): String? {
            var todoDetails: String?
            when (toDo.type) {
                ToDo.Type.Grading -> {
                    todoDetails = resources.getQuantityString(R.plurals.to_do_needs_grading, toDo.needsGradingCount, toDo.needsGradingCount)

                    todoDetails += " - $courseCode"
                }
                else -> todoDetails = courseCode
            }

            return todoDetails
        }

        private fun getCourseCode(event: ToDo?): String? {
            return if (event?.canvasContext == null) null
                   else event.canvasContext!!.name
        }

        override fun loadData() {
            if(NetworkUtils.isNetworkAvailable && ApiPrefs.user != null) {
                try {
                    val courses = CourseManager.getCoursesSynchronous(true)
                            .filter { it.isFavorite && !it.accessRestrictedByDate && !it.isInvited() }
                    val groups = GroupManager.getFavoriteGroupsSynchronous(true)
                    val todos = ToDoManager.getTodosSynchronous(ApiPrefs.user!!, true)
                    val scheduleItems = CalendarEventManager.getUpcomingEventsSynchronous(true)

                    val courseMap = CourseManager.createCourseMap(courses)
                    val groupMap = GroupManager.createGroupMap(groups)

                    val eventTodos = scheduleItems
                        .filter { it.type == ScheduleItem.TYPE_EVENT }
                        .map { ToDo.toDoWithScheduleItem(it) }
                    val dataStream = mergeToDosWithEvents(todos, eventTodos)
                    dataStream.forEach { ToDo.setContextInfo(it, courseMap, groupMap) }

                    setData(dataStream.filter { it.canvasContext != null })
                } catch (e: Throwable) {
                    Logger.e("Could not load " + this::class.java.simpleName + " widget. " + e.message)
                }
            }
        }

        private fun mergeToDosWithEvents(todoList: List<ToDo>?, eventList: List<ToDo>?): List<ToDo> {
            val todos = todoList ?: emptyList()
            val events = eventList ?: emptyList()

            // Return combined list, sorted by date
            val defaultDate = Date(0)
            return (todos + events).sortedBy { it.comparisonDate ?: defaultDate }
        }
    }

    companion object {

        fun createIntent(context: Context, appWidgetId: Int): Intent {
            val intent = Intent(context, TodoViewWidgetService::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.data = Uri.fromParts("appWidgetId", appWidgetId.toString(), null)
            return intent
        }
    }
}

