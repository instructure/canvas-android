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
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService

import com.instructure.student.R
import com.instructure.student.activity.InterwebsToApplication
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.*
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.Const

import java.io.Serializable

class GradesViewWidgetService : BaseRemoteViewsService(), Serializable {

    override fun onGetViewFactory(intent: Intent): RemoteViewsService.RemoteViewsFactory {
        return GradesRowFactory(intent)
    }

    private inner class GradesRowFactory(private val intent: Intent) : CanvasWidgetRowFactory<Course>() {

        override val layoutId: Int
            get() {
                if (BaseRemoteViewsService.shouldHideDetails(BaseRemoteViewsService.getAppWidgetId(intent))) {
                    return R.layout.listview_widget_grades_minimum_item_row
                }
                return R.layout.listview_widget_grades_item_row
            }

        override fun giveMeAppWidgetId(): Int {
            return BaseRemoteViewsService.getAppWidgetId(intent)
        }

        override fun createIntent(streamItem: Course): Intent {
            val domain = ApiPrefs.fullDomain

            //Construct URL to route to grades page
            val courseUrl = Const.COURSE_URL + streamItem.id
            val url = domain + courseUrl + Const.GRADE_URL
            val uri = Uri.parse(url)

            return InterwebsToApplication.createIntent(ContextKeeper.appContext, uri)
        }

        override fun setViewData(streamItem: Course, row: RemoteViews) {
            if (streamItem.term == null) {
                return
            }

            row.setViewVisibility(R.id.courseIndicator, View.VISIBLE)

            val appWidgetId = BaseRemoteViewsService.getAppWidgetId(intent)
            row.setTextColor(R.id.courseName, BaseRemoteViewsService.getWidgetTextColor(appWidgetId, applicationContext))
            row.setTextViewText(R.id.courseName, streamItem.name)

            if (!BaseRemoteViewsService.shouldHideDetails(appWidgetId)) {
                row.setTextViewText(R.id.courseTerm, streamItem.term?.name)
            }

            if (streamItem.isTeacher || streamItem.isTA) {
                row.setViewVisibility(R.id.gradeLayout, View.GONE)
            } else {
                row.setViewVisibility(R.id.gradeLayout, View.VISIBLE)
                val courseGrade = streamItem.getCourseGrade(false)
                if (courseGrade == null || courseGrade.isLocked) {
                    // Lock it up!
                    row.setViewVisibility(R.id.courseGrade, View.GONE)
                    row.setViewVisibility(R.id.lockedGradeImage, View.VISIBLE)
                } else {
                    row.setViewVisibility(R.id.courseGrade, View.VISIBLE)
                    row.setViewVisibility(R.id.lockedGradeImage, View.GONE)
                    row.setTextColor(R.id.courseGrade, ColorKeeper.getOrGenerateColor(streamItem))
                    if (courseGrade.noCurrentGrade) {
                        row.setTextViewText(R.id.courseGrade, applicationContext.getString(R.string.noGradeText))
                    } else {
                        row.setTextViewText(R.id.courseGrade, NumberHelper.doubleToPercentage(courseGrade.currentScore, 2))
                    }
                }
            }
            row.setOnClickFillInIntent(R.id.widget_root, createIntent(streamItem))

            row.setInt(R.id.courseIndicator, "setColorFilter", ColorKeeper.getOrGenerateColor(streamItem))
        }
        
        override fun clearViewData(row: RemoteViews) {
            row.setTextViewText(R.id.courseGrade, "")
            row.setTextViewText(R.id.courseTerm, "")
            row.setTextViewText(R.id.courseName, "")
            row.setViewVisibility(R.id.lockedGradeImage, View.GONE)
            row.setViewVisibility(R.id.courseIndicator, View.GONE)
        }

        override fun loadData() {
            if(NetworkUtils.isNetworkAvailable && ApiPrefs.user != null) {
                try {
                    // Force network so we always get the latest data for grades
                    setData(CourseManager.getCoursesSynchronous(true).filter
                    { it.isFavorite && it.isCurrentEnrolment() && !it.isInvited() })
                } catch (e: Throwable) {
                    Logger.e("Could not load " + this::class.java.simpleName + " widget. " + e.message)
                }
            }
        }
    }

    companion object {

        fun createIntent(context: Context, appWidgetId: Int): Intent {
            val intent = Intent(context, GradesViewWidgetService::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.data = Uri.fromParts("appWidgetId", appWidgetId.toString(), null)
            return intent
        }
    }
}


