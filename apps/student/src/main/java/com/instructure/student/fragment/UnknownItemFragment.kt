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

package com.instructure.student.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.StreamItem
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.validOrNull
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_UNKNOWN_ITEM
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.databinding.UnknownItemBinding

@ScreenView(SCREEN_VIEW_UNKNOWN_ITEM)
class UnknownItemFragment : ParentFragment() {

    private val binding by viewBinding(UnknownItemBinding::bind)

    private var streamItem: StreamItem by ParcelableArg(key = Const.STREAM_ITEM)
    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)

    override fun title(): String = ""

    override fun applyTheme() {
        with (binding) {
            toolbar.title =
                streamItem.getTitle(requireContext())?.toString().validOrNull() ?: getString(R.string.message)
            toolbar.setupAsBackButton(this@UnknownItemFragment)
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, canvasContext)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = layoutInflater.inflate(R.layout.unknown_item, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        streamItem.getMessage(requireContext()).let { binding.message.setVisible(it.isValid()).text = it }
        streamItem.notificationCategory.let { binding.notificationCategory.setVisible(it.isValid()).text = it }
        streamItem.updatedDate.let {
            binding.updatedDateTime.setVisible(it != null).text = DateHelper.getDateTimeString(requireContext(), it)
        }
    }

    companion object {
        fun makeRoute(canvasContext: CanvasContext, item: StreamItem): Route {
            val bundle = canvasContext.makeBundle { Bundle().apply { putParcelable(Const.STREAM_ITEM, item) }}
            return Route(UnknownItemFragment::class.java, canvasContext, bundle)
        }

        private fun validateRoute(route: Route): Boolean = route.canvasContext != null && route.arguments.getParcelable<StreamItem>(Const.STREAM_ITEM) != null

        fun newInstance(route: Route): UnknownItemFragment? {
            if (!validateRoute(route)) return null
            return UnknownItemFragment().withArgs(route.canvasContext!!.makeBundle(route.arguments))
        }
    }
}
