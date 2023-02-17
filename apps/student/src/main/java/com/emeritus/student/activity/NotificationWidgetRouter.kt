/*
 * Copyright (C) 2016 - present Instructure, Inc.
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
package com.emeritus.student.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import com.instructure.canvasapi2.models.StreamItem
import com.instructure.pandautils.utils.Const
import com.emeritus.student.R
import com.emeritus.student.fragment.NotificationListFragment.Companion.addFragmentForStreamItem

class NotificationWidgetRouter : ParentActivity() {
    private var streamItem: StreamItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent()
        streamItem?.let { addFragmentForStreamItem(it, context, true) }
        finish()
    }

    override fun handleIntent() {
        if (intent.hasExtra(Const.STREAM_ITEM)) {
            streamItem = intent.getParcelableExtra(Const.STREAM_ITEM)
        }
    }

    override fun contentResId() = 0

    override fun showHomeAsUp() = false

    override fun showTitleEnabled() = false

    override fun onUpPressed() {}

    companion object {
        fun createIntent(context: Context?, streamItem: StreamItem?): Intent {
            val intent = createIntent(
                context,
                NotificationWidgetRouter::class.java,
                R.layout.notification_widget_router_empty
            )
            intent.putExtra(Const.STREAM_ITEM, streamItem as Parcelable?)
            return intent
        }
    }
}
