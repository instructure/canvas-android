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

package com.emeritus.student.widget

import android.app.PendingIntent
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService

import com.emeritus.student.R
import com.emeritus.student.activity.LoginActivity
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.NetworkUtils
import kotlin.collections.ArrayList

abstract class CanvasWidgetRowFactory<I> : RemoteViewsService.RemoteViewsFactory {

    private var data: List<I> = ArrayList()

    protected abstract val layoutId: Int
    protected abstract fun loadData()

    override fun onCreate() {}

    override fun onDestroy() {}

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    fun setData(streamItems: List<I>) {
        data = streamItems
    }

    @Suppress("UNCHECKED_CAST")
    override fun onDataSetChanged() {
        if (ApiPrefs.user == null || !NetworkUtils.isNetworkAvailable) {
            return
        }

        loadData()
    }

    override fun getCount(): Int {
        return if(data.isEmpty() || ApiPrefs.user == null) 1
               else data.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val row = RemoteViews(ContextKeeper.appContext.packageName, layoutId)

        if (setViewLoginVisibility(row)) {
            return row
        }

        if (data.isEmpty() || ApiPrefs.user == null) {
            setEmptyViewText(row, R.string.noItemsToDisplayShort)
            return row
        }

        if (position < data.size && data[position] != null) setViewData(data[position], row)
        return row
    }

    private fun setViewLoginVisibility(row: RemoteViews): Boolean {
        return if (ApiPrefs.user == null) {
            //clear out any data that is currently there
            clearViewData(row)

            setEmptyViewText(row, R.string.notLoggedIn)
            //create log in intent
            val intent = LoginActivity.createIntent(ContextKeeper.appContext)
            val pendingIntent = PendingIntent.getActivity(
                    ContextKeeper.appContext, CanvasWidgetProvider.cycleBit++, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            row.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            true
        } else {
            row.setViewVisibility(R.id.is_not_logged_in, View.GONE)
            false
        }
    }

    private fun setEmptyViewText(row: RemoteViews, textResId: Int) {
        val textColor =
            BaseRemoteViewsService.getWidgetTextColor(giveMeAppWidgetId(), ContextKeeper.appContext)
        row.setViewVisibility(R.id.is_not_logged_in, View.VISIBLE)
        row.setTextColor(R.id.is_not_logged_in, textColor)
        row.setTextViewText(R.id.is_not_logged_in, ContextKeeper.appContext.getString(textResId))
    }

    protected abstract fun setViewData(streamItem: I, row: RemoteViews)
    protected abstract fun createIntent(streamItem: I): Intent
    protected abstract fun giveMeAppWidgetId(): Int
    protected abstract fun clearViewData(row: RemoteViews)
}
