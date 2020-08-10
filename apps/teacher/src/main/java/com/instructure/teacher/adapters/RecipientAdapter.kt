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
 */
package com.instructure.teacher.adapters

import android.content.Context
import com.android.ex.chips.BaseRecipientAdapter
import com.android.ex.chips.RecipientManager
import com.instructure.teacher.view.CanvasRecipientManager

class RecipientAdapter(context: Context?) : BaseRecipientAdapter(context) {
    override fun getRecipientManager(): RecipientManager {
        return CanvasRecipientManager.getInstance().setRecipientCallback(this)
    }

    val canvasRecipientManager: CanvasRecipientManager
        get() = CanvasRecipientManager.getInstance().setRecipientCallback(this)
}
