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
import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.pandautils.analytics.SCREEN_VIEW_MODULE_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.NLongArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.withArgs
import com.instructure.teacher.databinding.FragmentModuleListBinding
import com.instructure.teacher.features.modules.list.*
import com.instructure.teacher.mobius.common.ui.MobiusFragment
import com.instructure.teacher.mobius.common.ui.Presenter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@PageView(url = "{canvasContext}/modules")
@ScreenView(SCREEN_VIEW_MODULE_LIST)
abstract class ModuleListMobiusFragment : MobiusFragment<ModuleListModel, ModuleListEvent, ModuleListEffect,
        ModuleListView, ModuleListViewState, FragmentModuleListBinding>() {

    @get:PageViewUrlParam("canvasContext")
    val canvasContext by ParcelableArg<CanvasContext>(key = Const.COURSE)

    private val scrollToItemId by NLongArg(key = Const.MODULE_ITEM_ID)

    override fun makeUpdate() = ModuleListUpdate()

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) = ModuleListView(inflater, parent, canvasContext)

    override fun makePresenter(): Presenter<ModuleListModel, ModuleListViewState> = ModuleListPresenter

    override fun makeInitModel(): ModuleListModel = ModuleListModel(course = canvasContext, scrollToItemId = scrollToItemId)

    override val eventSources = listOf(ModuleListEventBusSource())



}
