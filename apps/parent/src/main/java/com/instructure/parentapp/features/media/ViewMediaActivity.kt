/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.parentapp.features.media

import android.content.Context
import android.content.Intent
import com.instructure.pandautils.activities.BaseViewMediaActivity
import com.instructure.pandautils.analytics.SCREEN_VIEW_VIEW_MEDIA
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.models.EditableFile

@ScreenView(SCREEN_VIEW_VIEW_MEDIA)
class ViewMediaActivity : BaseViewMediaActivity() {
    override fun allowEditing() = false
    override fun allowCopyingUrl() = false
    override fun handleEditing(editableFile: EditableFile) {}

    companion object {
        fun createIntent(context: Context, url: String, contentType: String, displayName: String?): Intent {
            val bundle = makeBundle(url, null, contentType, displayName, true)
            return Intent(context, ViewMediaActivity::class.java).putExtras(bundle)
        }
    }
}