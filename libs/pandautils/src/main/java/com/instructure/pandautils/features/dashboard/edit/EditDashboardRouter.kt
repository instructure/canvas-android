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

package com.instructure.pandautils.features.dashboard.edit

import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.instructure.canvasapi2.models.CanvasContext

interface EditDashboardRouter {
    fun routeCourse(canvasContext: CanvasContext?)

    fun showSnackbar(fragment: Fragment, resId: Int) {
        val view = fragment.view ?: return
        Snackbar.make(view, resId, Snackbar.LENGTH_LONG).show()
        view.announceForAccessibility(fragment.requireContext().getString(resId))
    }
}