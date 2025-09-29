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

import com.instructure.canvasapi2.HideAssignmentGradesForSectionsMutation
import com.instructure.canvasapi2.HideAssignmentGradesMutation
import com.instructure.canvasapi2.PostAssignmentGradesForSectionsMutation
import com.instructure.canvasapi2.PostAssignmentGradesMutation
import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.PostPolicyManager
import com.instructure.canvasapi2.managers.ProgressManager
import com.instructure.canvasapi2.managers.SectionManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Progress
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.pandautils.features.speedgrader.grade.SpeedGraderGradingEventHandler
import com.instructure.teacher.features.postpolicies.PostGradeEffect
import com.instructure.teacher.features.postpolicies.PostGradeEffectHandler
import com.instructure.teacher.features.postpolicies.PostGradeEvent
import com.instructure.teacher.features.postpolicies.ui.PostGradeView
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executors

class PostGradeEffectHandlerTest : Assert() {
    private lateinit var view: PostGradeView
    private lateinit var effectHandler: PostGradeEffectHandler
    private lateinit var consumer: Consumer<PostGradeEvent>
    private lateinit var connection: Connection<PostGradeEffect>

    private val progressId = "135"

    private val speedGraderGradingEventHandler: SpeedGraderGradingEventHandler = mockk(relaxed = true)
    private val postPolicyManager: PostPolicyManager = mockk(relaxed = true)

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        Dispatchers.setMain(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
        view = mockk(relaxed = true)
        effectHandler = PostGradeEffectHandler(speedGraderGradingEventHandler, postPolicyManager).apply { view = this@PostGradeEffectHandlerTest.view }
        consumer = mockk(relaxed = true)
        connection = effectHandler.connect(consumer)
    }

    @Test
    fun `LoadData calls the consumer with the loaded data`() {
        val assignmentId = 123L
        val courseId = 321L

        val sections = listOf(
            Section(id = 135),
            Section(id = 246)
        )
        val submissions = listOf(
            Submission(id = 111),
            Submission(id = 222)
        )

        mockkObject(SectionManager)
        every { SectionManager.getAllSectionsForCourseAsync(courseId, true) } returns mockk {
            coEvery { await() } returns DataResult.Success(sections)
        }

        mockkObject(AssignmentManager)
        every { AssignmentManager.getAllSubmissionsForAssignmentAsync(courseId, assignmentId, true) } returns mockk {
            coEvery { await() } returns DataResult.Success(submissions)
        }

        connection.accept(PostGradeEffect.LoadData(Assignment(id = 123, courseId = 321)))

        verify(timeout = 100) {
            consumer.accept(PostGradeEvent.DataLoaded(sections, submissions))
        }
        confirmVerified(consumer)
    }

    @Test
    fun `LoadData calls the consumer with the loaded data when failed`() {
        val assignmentId = 123L
        val courseId = 321L

        val sections = emptyList<Section>()
        val submissions = emptyList<Submission>()

        mockkObject(SectionManager)
        every { SectionManager.getAllSectionsForCourseAsync(courseId, true) } returns mockk {
            coEvery { await() } returns DataResult.Fail(Failure.Exception(Throwable()))
        }

        mockkObject(AssignmentManager)
        every { AssignmentManager.getAllSubmissionsForAssignmentAsync(courseId, assignmentId, true) } returns mockk {
            coEvery { await() } returns DataResult.Fail(Failure.Exception(Throwable()))
        }

        connection.accept(PostGradeEffect.LoadData(Assignment(id = 123, courseId = 321)))

        verify(timeout = 100) {
            consumer.accept(PostGradeEvent.DataLoaded(sections, submissions))
        }
        confirmVerified(consumer)
    }

    @Test
    fun `HideGrades calls the API to hide grades with exception`() {
        val assignmentId = 123L

        val sections = emptyList<String>()

        coEvery { postPolicyManager.hideGradesAsync(assignmentId) } throws Exception()

        connection.accept(PostGradeEffect.HideGrades(assignmentId, sections))

        coVerify(timeout = 100) {
            postPolicyManager.hideGradesAsync(assignmentId)
        }
        verify(timeout = 100) {
            consumer.accept(PostGradeEvent.PostFailed)
        }
        confirmVerified(postPolicyManager, consumer)
    }

    @Test
    fun `HideGrades calls the API to hide grades with null response`() {
        val assignmentId = 123L

        val sections = emptyList<String>()

        coEvery { postPolicyManager.hideGradesAsync(assignmentId) } returns HideAssignmentGradesMutation.Data(null)

        connection.accept(PostGradeEffect.HideGrades(assignmentId, sections))

        coVerify(timeout = 100) {
            postPolicyManager.hideGradesAsync(assignmentId)
        }
        verify(timeout = 100) {
            consumer.accept(PostGradeEvent.PostStarted(null))
        }
        confirmVerified(postPolicyManager, consumer)
    }

    @Test
    fun `HideGrades calls the API to hide grades`() {
        val assignmentId = 123L

        val sections = emptyList<String>()

        coEvery { postPolicyManager.hideGradesAsync(assignmentId) } returns HideAssignmentGradesMutation.Data(
            HideAssignmentGradesMutation.HideAssignmentGrades(
                HideAssignmentGradesMutation.Progress(progressId)
            )
        )

        connection.accept(PostGradeEffect.HideGrades(assignmentId, sections))

        coVerify(timeout = 100) {
            postPolicyManager.hideGradesAsync(assignmentId)
        }
        verify(timeout = 100) {
            consumer.accept(PostGradeEvent.PostStarted(progressId))
        }
        confirmVerified(postPolicyManager, consumer)
    }

    @Test
    fun `HideGrades calls the API to hide grades with sections`() {
        val assignmentId = 123L

        val sections = listOf("1", "2", "3")

        coEvery {
            postPolicyManager.hideGradesForSectionsAsync(assignmentId, sections)
        } returns HideAssignmentGradesForSectionsMutation.Data(
            HideAssignmentGradesForSectionsMutation.HideAssignmentGradesForSections(
                HideAssignmentGradesForSectionsMutation.Progress(progressId)
            )
        )

        connection.accept(PostGradeEffect.HideGrades(assignmentId, sections))

        coVerify(timeout = 100) {
            postPolicyManager.hideGradesForSectionsAsync(assignmentId, sections)
        }
        verify(timeout = 100) {
            consumer.accept(PostGradeEvent.PostStarted(progressId))
        }
        confirmVerified(postPolicyManager, consumer)
    }

    @Test
    fun `PostGrades calls the API to hide grades with exception`() {
        val assignmentId = 123L
        val gradedOnly = false

        val sections = emptyList<String>()

        coEvery { postPolicyManager.postGradesAsync(assignmentId, gradedOnly) } throws Exception()

        connection.accept(PostGradeEffect.PostGrades(assignmentId, sections, gradedOnly))

        coVerify(timeout = 100) {
            postPolicyManager.postGradesAsync(assignmentId, gradedOnly)
        }
        verify(timeout = 100) {
            consumer.accept(PostGradeEvent.PostFailed)
        }
        confirmVerified(postPolicyManager, consumer)
    }

    @Test
    fun `PostGrades calls the API to post grades`() {
        val assignmentId = 123L
        val gradedOnly = false

        val sections = emptyList<String>()

        coEvery { postPolicyManager.postGradesAsync(assignmentId, gradedOnly) } returns PostAssignmentGradesMutation.Data(
            PostAssignmentGradesMutation.PostAssignmentGrades(PostAssignmentGradesMutation.Progress(progressId))
        )

        connection.accept(PostGradeEffect.PostGrades(assignmentId, sections, gradedOnly))

        coVerify(timeout = 100) {
            postPolicyManager.postGradesAsync(assignmentId, gradedOnly)
        }
        verify(timeout = 100) {
            consumer.accept(PostGradeEvent.PostStarted(progressId))
        }
        confirmVerified(postPolicyManager, consumer)
    }

    @Test
    fun `PostGrades calls the API to post grades with sections`() {
        val assignmentId = 123L
        val gradedOnly = true

        val sections = listOf("1", "2", "3")

        coEvery {
            postPolicyManager.postGradesForSectionsAsync(assignmentId, gradedOnly, sections)
        } returns PostAssignmentGradesForSectionsMutation.Data(
            PostAssignmentGradesForSectionsMutation.PostAssignmentGradesForSections(
                PostAssignmentGradesForSectionsMutation.Progress(progressId)
            )
        )

        connection.accept(PostGradeEffect.PostGrades(assignmentId, sections, gradedOnly))

        coVerify(timeout = 100) {
            postPolicyManager.postGradesForSectionsAsync(assignmentId, gradedOnly, sections)
        }
        verify(timeout = 100) {
            consumer.accept(PostGradeEvent.PostStarted(progressId))
        }
        confirmVerified(postPolicyManager, consumer)
    }

    @Test
    fun `ShowGradesPosted calls showGradesPosted on the view`() {
        val assignmentId = 123L
        val isHidingGrades = true

        connection.accept(PostGradeEffect.ShowGradesPosted(isHidingGrades, assignmentId))

        verify(timeout = 100) {
            view.showGradesPosted(isHidingGrades, assignmentId)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowPostFailed calls showGradesPosted on the view`() {
        val isHidingGrades = true

        connection.accept(PostGradeEffect.ShowPostFailed(isHidingGrades))

        verify(timeout = 100) {
            view.showPostFailed(isHidingGrades)
        }
        confirmVerified(view)
    }

    @Test
    fun `WatchForProgress calls the consumer with PostFailed when given null id`() {
        val progressId: String? = null

        connection.accept(PostGradeEffect.WatchForProgress(progressId))

        verify(timeout = 100) {
            consumer.accept(PostGradeEvent.PostFailed)
        }
        confirmVerified(consumer)
    }

    @Test
    fun `WatchForProgress calls the consumer with PostFailed when progress API result fails`() {
        mockkStatic("kotlinx.coroutines.DelayKt")
        coEvery { delay(1000L) } returns Unit

        mockkObject(ProgressManager)
        coEvery {
            ProgressManager.getProgressAsync(progressId).await()
        } returns DataResult.Fail(null)

        connection.accept(PostGradeEffect.WatchForProgress(progressId))

        verify(timeout = 100) {
            consumer.accept(PostGradeEvent.PostFailed)
        }
        confirmVerified(consumer)
    }

    @Test
    fun `WatchForProgress calls the consumer with PostFailed when progress is failed`() {
        mockkStatic("kotlinx.coroutines.DelayKt")
        coEvery { delay(1000L) } returns Unit

        mockkObject(ProgressManager)
        coEvery {
            ProgressManager.getProgressAsync(progressId).await()
        } returns DataResult.Success(Progress(workflowState = "failed"))

        connection.accept(PostGradeEffect.WatchForProgress(progressId))

        verify(timeout = 100) {
            consumer.accept(PostGradeEvent.PostFailed)
        }
        confirmVerified(consumer)
    }

    @Test
    fun `WatchForProgress calls the consumer with GradesPosted`() {
        mockkStatic("kotlinx.coroutines.DelayKt")
        coEvery { delay(1000L) } returns Unit

        mockkObject(ProgressManager)
        coEvery {
            ProgressManager.getProgressAsync(progressId).await()
        } returnsMany listOf(
            DataResult.Success(Progress(workflowState = "queued")),
            DataResult.Success(Progress(workflowState = "running")),
            DataResult.Success(Progress(workflowState = "completed"))
        )

        connection.accept(PostGradeEffect.WatchForProgress(progressId))

        verify(timeout = 100) {
            consumer.accept(PostGradeEvent.GradesPosted)
        }
        confirmVerified(consumer)
    }
}
