/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.features.speedgrader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.Text
import androidx.compose.ui.platform.ComposeView
import com.instructure.interactions.router.Route
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.compose.CanvasTheme

class SpeedGraderFragment : BaseCanvasFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                CanvasTheme {
                    Text("SPEEDGRADER")
                }
            }
        }
    }

    companion object {
        fun newInstance(route: Route): SpeedGraderFragment {
            return SpeedGraderFragment()
        }

        fun newInstance(bundle: Bundle): SpeedGraderFragment {
            return SpeedGraderFragment()
        }
    }
}