/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.teacher.features.modules.list.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.analytics.SCREEN_VIEW_MODULE_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.NLongArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.withArgs
import com.instructure.teacher.features.modules.list.*
import com.instructure.teacher.mobius.common.ui.MobiusFragment
import com.instructure.teacher.mobius.common.ui.Presenter

@ScreenView(SCREEN_VIEW_MODULE_LIST)
class ModuleListFragment :
    MobiusFragment<ModuleListModel, ModuleListEvent, ModuleListEffect, ModuleListView, ModuleListViewState>() {

    val course by ParcelableArg<CanvasContext>(key = Const.COURSE)

    private val scrollToItemId by NLongArg(key = Const.MODULE_ITEM_ID)

    override fun makeEffectHandler() = ModuleListEffectHandler()

    override fun makeUpdate() = ModuleListUpdate()

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) = ModuleListView(inflater, parent, course)

    override fun makePresenter(): Presenter<ModuleListModel, ModuleListViewState> = ModuleListPresenter

    override fun makeInitModel(): ModuleListModel = ModuleListModel(course = course, scrollToItemId = scrollToItemId)

    override val eventSources = listOf(ModuleListEventBusSource())

    companion object {

        fun makeBundle(course: CanvasContext, scrollToModuleItemId: Long? = null) = Bundle().apply {
            putParcelable(Const.COURSE, course)
            if (scrollToModuleItemId != null) putLong(Const.MODULE_ITEM_ID, scrollToModuleItemId)
        }

        fun newInstance(args: Bundle) = ModuleListFragment().withArgs(args)

    }

}
