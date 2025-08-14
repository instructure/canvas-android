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
package com.instructure.teacher.features.postpolicies.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.models.Assignment
import com.instructure.pandautils.analytics.SCREEN_VIEW_POST_GRADE_POLICY
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.features.speedgrader.grade.SpeedGraderGradingEventHandler
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.teacher.databinding.FragmentPostGradeBinding
import com.instructure.teacher.features.postpolicies.PostGradeEffect
import com.instructure.teacher.features.postpolicies.PostGradeEffectHandler
import com.instructure.teacher.features.postpolicies.PostGradeEvent
import com.instructure.teacher.features.postpolicies.PostGradeModel
import com.instructure.teacher.features.postpolicies.PostGradePresenter
import com.instructure.teacher.features.postpolicies.PostGradeUpdate
import com.instructure.teacher.mobius.common.ui.MobiusFragment

const val IS_HIDE_GRADE_MODE = "isHideGradeMode"

@ScreenView(SCREEN_VIEW_POST_GRADE_POLICY)
abstract class PostGradeMobiusFragment : MobiusFragment<PostGradeModel, PostGradeEvent, PostGradeEffect,
        PostGradeView, PostGradeViewState, FragmentPostGradeBinding>() {

    abstract fun getEventHandler(): SpeedGraderGradingEventHandler

    private var assignment: Assignment by ParcelableArg(Assignment(), Const.ASSIGNMENT)
    private var isHidingGrades: Boolean by BooleanArg(false, IS_HIDE_GRADE_MODE)

    override fun makeEffectHandler() = PostGradeEffectHandler(getEventHandler())

    override fun makeUpdate() = PostGradeUpdate()

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) = PostGradeView(inflater, parent)

    override fun makePresenter() = PostGradePresenter

    override fun makeInitModel() = PostGradeModel(assignment = assignment, isHidingGrades = isHidingGrades)


}
