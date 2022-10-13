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
 */
package com.instructure.teacher.unit.postpolicies

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.models.Submission
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.teacher.R
import com.instructure.teacher.features.postpolicies.PostGradeModel
import com.instructure.teacher.features.postpolicies.PostGradePresenter
import com.instructure.teacher.features.postpolicies.PostSection
import com.instructure.teacher.features.postpolicies.ui.PostGradeViewState
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class PostGradePresenterTest : Assert() {

    private val assignment = Assignment(id = 123L, courseId = 321L)
    private lateinit var context: Context

    private lateinit var initModel: PostGradeModel

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        initModel = PostGradeModel(
            assignment = assignment,
            isHidingGrades = false
        )

        mockkObject(ColorKeeper)
        // TODO Fix test
//        every { ColorKeeper.colorFromCourseId(assignment.courseId) } returns assignment.courseId.toInt()
    }

    private fun postedSubmissionList() = listOf(Submission(postedAt = Date()))
    private fun hiddenSubmissionList() = listOf(Submission(postedAt = null))
    private fun bothSubmissionList() = listOf(Submission(postedAt = null), Submission(postedAt = Date()))

    private fun postedModel() = initModel.copy(isHidingGrades = false, submissions = hiddenSubmissionList())
    private fun hiddenModel() = initModel.copy(isHidingGrades = true, submissions = postedSubmissionList())

    @Test
    fun `Returns loading state when isLoading`() {
        val model = initModel.copy(isLoading = true)
        val expectedState = PostGradeViewState.Loading(assignment.courseId.toInt())
        val actualState = PostGradePresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns empty state for hiding grades when all grades hidden`() {
        val model = initModel.copy(isHidingGrades = true, submissions = hiddenSubmissionList())
        val expectedState = PostGradeViewState.EmptyViewState(R.drawable.ic_panda_all_hidden, "All Hidden", "All grades are currently hidden.")
        val actualState = PostGradePresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns empty state for posting grades when all grades posted`() {
        val model = initModel.copy(isHidingGrades = false, submissions = postedSubmissionList())
        val expectedState = PostGradeViewState.EmptyViewState(R.drawable.ic_panda_all_posted, "All Posted", "All grades are currently posted.")
        val actualState = PostGradePresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns courseColor when loaded`() {
        val model = postedModel()
        val actualState = PostGradePresenter.present(model, context) as PostGradeViewState.LoadedViewState
        assertEquals(assignment.courseId.toInt(), actualState.courseColor)
    }

    @Test
    fun `Returns sections when loaded`() {
        val model = postedModel().copy(sections = listOf(
            PostSection(Section(), selected = false, courseColor = assignment.courseId.toInt()),
            PostSection(Section(), selected = true, courseColor = assignment.courseId.toInt())
        ))
        val actualState = PostGradePresenter.present(model, context) as PostGradeViewState.LoadedViewState
        assertEquals(model.sections, actualState.sections)
    }

    @Test
    fun `Returns specific sections on when loaded`() {
        val model = postedModel().copy(specificSectionsVisible = true)
        val actualState = PostGradePresenter.present(model, context) as PostGradeViewState.LoadedViewState
        assertEquals(true, actualState.specificSectionsVisible)
    }

    @Test
    fun `Returns specific sections off when loaded`() {
        val model = postedModel().copy(specificSectionsVisible = false)
        val actualState = PostGradePresenter.present(model, context) as PostGradeViewState.LoadedViewState
        assertEquals(false, actualState.specificSectionsVisible)
    }

    @Test
    fun `Returns post processing on when loaded`() {
        val model = postedModel().copy(isProcessing = true)
        val actualState = PostGradePresenter.present(model, context) as PostGradeViewState.LoadedViewState
        assertEquals(true, actualState.postProcessing)
    }

    @Test
    fun `Returns post processing off when loaded`() {
        val model = postedModel().copy(isProcessing = false)
        val actualState = PostGradePresenter.present(model, context) as PostGradeViewState.LoadedViewState
        assertEquals(false, actualState.postProcessing)
    }

    @Test
    fun `Returns statusText for posting grades for single hidden when loaded`() {
        val model = postedModel()
        val actualState = PostGradePresenter.present(model, context) as PostGradeViewState.LoadedViewState
        assertEquals("${model.submissions.size} grade currently hidden", actualState.statusText)
    }

    @Test
    fun `Returns statusText for hidden grades for single posted when loaded`() {
        val model = hiddenModel()
        val actualState = PostGradePresenter.present(model, context) as PostGradeViewState.LoadedViewState
        assertEquals("${model.submissions.size} grade currently posted", actualState.statusText)
    }

    @Test
    fun `Returns statusText for posting grades for multiple when loaded`() {
        val model = postedModel().copy(submissions = hiddenSubmissionList() + hiddenSubmissionList())
        val actualState = PostGradePresenter.present(model, context) as PostGradeViewState.LoadedViewState
        assertEquals("${model.submissions.size} grades currently hidden", actualState.statusText)
    }

    @Test
    fun `Returns statusText for hidden grades when for multiple loaded`() {
        val model = hiddenModel().copy(submissions = postedSubmissionList() + postedSubmissionList())
        val actualState = PostGradePresenter.present(model, context) as PostGradeViewState.LoadedViewState
        assertEquals("${model.submissions.size} grades currently posted", actualState.statusText)
    }

    @Test
    fun `Returns gradedText for posting grades when loaded`() {
        val model = postedModel()
        val actualState = PostGradePresenter.present(model, context) as PostGradeViewState.LoadedViewState
        assertEquals("Everyone", actualState.gradedOnlyText)
    }

    @Test
    fun `Returns gradedText for posting grades with graded only when loaded`() {
        val model = postedModel().copy(postGradedOnly = true)
        val actualState = PostGradePresenter.present(model, context) as PostGradeViewState.LoadedViewState
        assertEquals("Graded", actualState.gradedOnlyText)
    }

    @Test
    fun `Returns gradedText for hidden grades when loaded`() {
        val model = hiddenModel()
        val actualState = PostGradePresenter.present(model, context) as PostGradeViewState.LoadedViewState
        assertEquals(null, actualState.gradedOnlyText)
    }

    @Test
    fun `Returns postText for posting grades when loaded`() {
        val model = postedModel()
        val actualState = PostGradePresenter.present(model, context) as PostGradeViewState.LoadedViewState
        assertEquals("Post Grades", actualState.postText)
    }

    @Test
    fun `Returns postText for processing posting grades when loaded`() {
        val model = postedModel().copy(isProcessing = true)
        val actualState = PostGradePresenter.present(model, context) as PostGradeViewState.LoadedViewState
        assertEquals(null, actualState.postText)
    }

    @Test
    fun `Returns postText for hidden grades when loaded`() {
        val model = hiddenModel()
        val actualState = PostGradePresenter.present(model, context) as PostGradeViewState.LoadedViewState
        assertEquals("Hide Grades", actualState.postText)
    }

    @Test
    fun `Returns postText for processing hidden grades when loaded`() {
        val model = hiddenModel().copy(isProcessing = true)
        val actualState = PostGradePresenter.present(model, context) as PostGradeViewState.LoadedViewState
        assertEquals(null, actualState.postText)
    }
}
