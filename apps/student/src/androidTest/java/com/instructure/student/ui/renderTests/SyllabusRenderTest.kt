/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
 */
package com.instructure.student.ui.renderTests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvas.espresso.annotations.Stub
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.ui.utils.StudentRenderTest
import com.instructure.student.mobius.syllabus.SyllabusModel
import com.instructure.student.mobius.syllabus.ui.SyllabusRepositoryFragment
import com.spotify.mobius.runners.WorkRunner
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SyllabusRenderTest : StudentRenderTest() {

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
            events = DataResult.Success(emptyList())
        )
    }

    @Test
    fun displaysToolbarTitles() {
        val model = baseModel.copy()
        loadPageWithModel(model)

        syllabusRenderPage.assertDisplaysToolbarTitle("Syllabus")
        syllabusRenderPage.assertDisplaysToolbarSubtitle(model.course!!.dataOrThrow.name)
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

    private fun loadPageWithModel(model: SyllabusModel) {
        val emptyEffectRunner = object : WorkRunner {
            override fun dispose() = Unit
            override fun post(runnable: Runnable) = Unit
        }
        val route = SyllabusRepositoryFragment.makeRoute(model.course?.dataOrNull ?: Course(id = model.courseId))
        val fragment = SyllabusRepositoryFragment.newInstance(route)!!.apply {
            overrideInitModel = model
            loopMod = { it.effectRunner { emptyEffectRunner } }
        }
        activityRule.activity.loadFragment(fragment)
        sleep(3000) // Need to wait here a bit because loadFragment needs some time.
    }

}
