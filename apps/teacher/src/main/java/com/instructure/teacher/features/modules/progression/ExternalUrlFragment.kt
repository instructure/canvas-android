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

package com.instructure.teacher.features.modules.progression

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.interactions.MasterDetailInteractions
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.features.modules.externalurl.ExternalUrlScreen
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.NullableStringArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.isTablet
import com.instructure.pandautils.utils.launchCustomTab
import com.instructure.teacher.R

class ExternalUrlFragment : BaseCanvasFragment() {

    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)
    private var itemTitle: String by StringArg(key = Const.ACTION_BAR_TITLE)
    private var externalUrl: String? by NullableStringArg(key = Const.URL)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CanvasTheme {
                    // On tablets the module pager is the detail pane of a master/detail layout, so the
                    // nav button toggles the master pane instead of navigating back, matching every
                    // other module item screen.
                    val masterDetail = activity as? MasterDetailInteractions
                    var isMasterVisible by remember {
                        mutableStateOf(isTablet && masterDetail?.isMasterVisible == true)
                    }
                    val togglesMasterPane = isTablet && masterDetail != null

                    ExternalUrlScreen(
                        title = itemTitle,
                        onNavigationClick = {
                            if (togglesMasterPane) {
                                masterDetail.toggleExpandCollapse()
                                isMasterVisible = masterDetail.isMasterVisible
                            } else {
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }
                        },
                        courseColor = Color(canvasContext.color),
                        onOpenClick = externalUrl?.takeIf { it.isNotBlank() }?.let { url ->
                            { requireActivity().launchCustomTab(url, canvasContext.color) }
                        },
                        navIconRes = when {
                            !togglesMasterPane -> R.drawable.ic_back_arrow
                            isMasterVisible -> R.drawable.ic_collapse_horizontal
                            else -> R.drawable.ic_expand_horizontal
                        },
                        navIconContentDescription = stringResource(
                            when {
                                !togglesMasterPane -> R.string.back
                                isMasterVisible -> R.string.contentDescriptionCollapsePage
                                else -> R.string.contentDescriptionExpandPage
                            }
                        )
                    )
                }
            }
        }
    }

    companion object {
        fun makeBundle(canvasContext: CanvasContext, itemTitle: String, externalUrl: String?) = Bundle().apply {
            putParcelable(Const.CANVAS_CONTEXT, canvasContext)
            putString(Const.ACTION_BAR_TITLE, itemTitle)
            putString(Const.URL, externalUrl)
        }

        fun newInstance(bundle: Bundle) = ExternalUrlFragment().apply {
            arguments = bundle
        }
    }
}
