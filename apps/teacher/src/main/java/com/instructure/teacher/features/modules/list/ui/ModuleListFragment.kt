/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 */

package com.instructure.teacher.features.modules.list.ui

import android.os.Bundle
import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.apis.ProgressAPI
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.withArgs
import com.instructure.teacher.features.modules.list.ModuleListEffectHandler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ModuleListFragment : ModuleListMobiusFragment() {

    @Inject
    lateinit var moduleApi: ModuleAPI.ModuleInterface

    @Inject
    lateinit var progressApi: ProgressAPI.ProgressInterface

    @Inject
    lateinit var fileApi: FileFolderAPI.FilesFoldersInterface

    override fun makeEffectHandler() = ModuleListEffectHandler(moduleApi, progressApi, fileApi)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false
    }

    companion object {

        fun makeBundle(course: CanvasContext, scrollToModuleItemId: Long? = null) = Bundle().apply {
            putParcelable(Const.COURSE, course)
            if (scrollToModuleItemId != null) putLong(Const.MODULE_ITEM_ID, scrollToModuleItemId)
        }

        fun newInstance(args: Bundle) = ModuleListFragment().withArgs(args)

    }
}