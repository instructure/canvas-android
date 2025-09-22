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
 */package com.instructure.teacher.features.postpolicies.ui

import android.os.Bundle
import com.instructure.canvasapi2.managers.PostPolicyManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.pandautils.features.speedgrader.grade.SpeedGraderGradingEventHandler
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.withArgs
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PostGradeFragment : PostGradeMobiusFragment() {

    @Inject
    lateinit var speedGraderGradingEventHandler: SpeedGraderGradingEventHandler

    @Inject
    lateinit var postPolicyManager: PostPolicyManager

    override fun getPolicyManager(): PostPolicyManager = postPolicyManager

    override fun getEventHandler(): SpeedGraderGradingEventHandler = speedGraderGradingEventHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false
    }

    companion object {
        fun newInstance(assignment: Assignment, isHideGradeMode: Boolean) = PostGradeFragment().withArgs(
            Bundle().apply {
            putParcelable(Const.ASSIGNMENT, assignment)
            putBoolean(IS_HIDE_GRADE_MODE, isHideGradeMode)
        })
    }
}