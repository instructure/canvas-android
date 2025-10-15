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

import com.instructure.canvas.espresso.annotations.Stub
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.teacher.features.syllabus.SyllabusModel
import com.instructure.teacher.features.syllabus.ui.SyllabusRepositoryFragment
import com.instructure.teacher.ui.rendertests.renderpages.SyllabusRenderPage
import com.instructure.teacher.ui.utils.TeacherRenderTest
import com.spotify.mobius.runners.WorkRunner
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Test
import java.lang.Thread.sleep

@HiltAndroidTest
class SyllabusRenderTest : TeacherRenderTest() {

    private val syllabusRenderPage = SyllabusRenderPage()

    private val syllabusDescription = "course description"
    private lateinit var baseModel: SyllabusModel

    @Before
    fun setup() {
        val courseId = 123L
        baseModel = SyllabusModel(
            courseId = courseId,
            course = DataResult.Success(Course(id = courseId, name = "Test Course", syllabusBody = syllabusDescription)),
            isLoading = false,
            syllabus = ScheduleItem.createSyllabus("", "<p>$syllabusDescription</p>"),
            events = DataResult.Success(emptyList()),
            summaryAllowed = true
        )
    }

    @Test
    fun displaysToolbarTitles() {
        val model = baseModel.copy()
        loadPageWithModel(model)

        syllabusRenderPage.assertDisplaysToolbarText("Syllabus")
        syllabusRenderPage.assertDisplaysToolbarText(model.course!!.dataOrThrow.name) // Assert subtitle
    }

    @Test
    fun doesNotDisplaySyllabus() {
        val model = baseModel.copy(syllabus = null)
        loadPageWithModel(model)

        syllabusRenderPage.assertDoesNotDisplaySyllabus()
    }

    @Test
    @Stub
    fun displaysSyllabus() {
        val model = baseModel.copy()
        loadPageWithModel(model)

        syllabusRenderPage.assertDisplaysSyllabus(syllabusDescription, shouldDisplayTabs = false)
    }

    @Test
    fun displaysEmpty() {
        val model = baseModel.copy(syllabus = null)
        loadPageWithModel(model)

        syllabusRenderPage.assertDisplaysEmpty()
    }

    @Test
    fun displaysError() {
        val model = baseModel.copy(syllabus = null, events = DataResult.Fail())
        loadPageWithModel(model)

        syllabusRenderPage.assertDisplaysError()
    }

    @Test
    fun displaysEvents() {
        val model = baseModel.copy(syllabus = null, events = DataResult.Success(List(3) { ScheduleItem(title = it.toString()) }))
        loadPageWithModel(model)

        syllabusRenderPage.assertDisplaysEvents()
    }

    @Test
    fun tappingEventsDisplaysEvents() {
        val model = baseModel.copy(events = DataResult.Success(List(3) { ScheduleItem(title = it.toString()) }))
        loadPageWithModel(model)

        syllabusRenderPage.assertDisplaysSyllabus(syllabusDescription)
        syllabusRenderPage.clickEventsTab()
        syllabusRenderPage.assertDisplaysEvents()
    }

    @Test
    fun swipingToEventsDisplaysEvents() {
        val model = baseModel.copy(events = DataResult.Success(List(3) { ScheduleItem(title = it.toString()) }))
        loadPageWithModel(model)

        syllabusRenderPage.assertDisplaysSyllabus(syllabusDescription)
        syllabusRenderPage.swipeToEventsTab()
        syllabusRenderPage.assertDisplaysEvents()
    }

    @Test
    fun cannotSwipeToSyllabus() {
        val model = baseModel.copy(syllabus = null, events = DataResult.Success(List(3) { ScheduleItem(title = it.toString()) }))
        loadPageWithModel(model)

        syllabusRenderPage.assertDisplaysEvents()
        syllabusRenderPage.swipeToSyllabusTab()
        syllabusRenderPage.assertDisplaysEvents()
    }

    @Test
    fun editShownIfTeacherHavePermissionToEdit() {
        val permissions = CanvasContextPermission(canManageContent = true)
        val model = baseModel.copy(syllabus = null, events = DataResult.Success(List(3) { ScheduleItem(title = it.toString()) }), permissions = DataResult.Success(permissions))
        loadPageWithModel(model)

        syllabusRenderPage.assertDisplayEditIcon()
    }

    private fun loadPageWithModel(model: SyllabusModel) {
        val emptyEffectRunner = object : WorkRunner {
            override fun dispose() = Unit
            override fun post(runnable: Runnable) = Unit
        }
        val canvasContext = model.course?.dataOrNull ?: Course(id = model.courseId)
        val fragment = SyllabusRepositoryFragment.newInstance(canvasContext)!!.apply {
            overrideInitModel = model
            loopMod = { it.effectRunner { emptyEffectRunner } }
        }
        activityRule.activity.loadFragment(fragment)
        sleep(3000) // Need to wait here a bit because loadFragment needs some time.
    }
}