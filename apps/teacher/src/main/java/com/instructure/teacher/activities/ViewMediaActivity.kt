/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
package com.instructure.teacher.activities

import android.content.Context
import android.content.Intent
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrl
import com.instructure.interactions.router.Route
import com.instructure.pandautils.activities.BaseViewMediaActivity
import com.instructure.pandautils.analytics.SCREEN_VIEW_VIEW_MEDIA
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.models.EditableFile
import com.instructure.teacher.fragments.EditFileFolderFragment
import com.instructure.teacher.router.RouteMatcher

@ScreenView(SCREEN_VIEW_VIEW_MEDIA)
@PageView
class ViewMediaActivity : BaseViewMediaActivity() {

    override fun allowEditing() = true
    override fun allowCopyingUrl() = true
    override fun handleEditing(editableFile: EditableFile) {
        val args = EditFileFolderFragment.makeBundle(editableFile.file, editableFile.usageRights, editableFile.licenses, editableFile.canvasContext!!.id)
        RouteMatcher.route(this, Route(EditFileFolderFragment::class.java, editableFile.canvasContext, args))
    }

    @PageViewUrl
    private fun makePageViewUrl() = url

    companion object {
        fun createIntent(context: Context, route: Route): Intent {
            val intent = Intent(context, ViewMediaActivity::class.java)
            intent.putExtras(route.arguments)
            return intent
        }
    }
}
