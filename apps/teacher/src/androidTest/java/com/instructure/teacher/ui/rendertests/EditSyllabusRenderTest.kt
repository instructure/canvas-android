/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.teacher.ui.rendertests

import com.instructure.canvasapi2.models.Course
import com.instructure.teacher.features.syllabus.edit.EditSyllabusFragment
import com.instructure.teacher.features.syllabus.edit.EditSyllabusViewState
import com.instructure.teacher.ui.rendertests.renderpages.EditSyllabusRenderPage
import com.instructure.teacher.ui.utils.TeacherRenderTest
import com.spotify.mobius.runners.WorkRunner
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class EditSyllabusRenderTest : TeacherRenderTest() {

    private val editSyllabusRenderPage = EditSyllabusRenderPage()

    @Test
    fun displayCorrectLoadedState() {
        val viewState = EditSyllabusViewState.Loaded("Syllabus body", true)
        loadPageWithState(viewState)

        editSyllabusRenderPage.assertLoadedStateDisplayed()
    }

    @Test
    fun displayCorrectSavedState() {
        val viewState = EditSyllabusViewState.Saving
        loadPageWithState(viewState)

        editSyllabusRenderPage.assertSavingStateDisplayed()
    }

    @Test
    fun displayCorrectDataWithSummaryNotAllowed() {
        val viewState = EditSyllabusViewState.Loaded("Syllabus body", false)
        loadPageWithState(viewState)

        editSyllabusRenderPage.assertCorrectDataDisplayed("Syllabus body", false)
    }

    @Test
    fun displayCorrectDataWithSummaryAllowed() {
        val viewState = EditSyllabusViewState.Loaded("Syllabus body", true)
        loadPageWithState(viewState)

        editSyllabusRenderPage.assertCorrectDataDisplayed("Syllabus body", true)
    }

    private fun loadPageWithState(viewState: EditSyllabusViewState) {
        val emptyEffectRunner = object : WorkRunner {
            override fun dispose() = Unit
            override fun post(runnable: Runnable) = Unit
        }
        val bundle = EditSyllabusFragment.createArgs(Course(), false)
        val fragment = EditSyllabusFragment.newInstance(bundle).apply {
            overrideInitViewState = viewState
            loopMod = { it.effectRunner { emptyEffectRunner } }
        }
        activityRule.activity.loadFragment(fragment)
    }
}