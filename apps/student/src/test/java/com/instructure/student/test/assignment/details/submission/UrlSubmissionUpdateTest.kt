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
package com.instructure.student.test.assignment.details.submission

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.student.mobius.assignmentDetails.submission.text.TextSubmissionEffect
import com.instructure.student.mobius.assignmentDetails.submission.text.TextSubmissionEvent
import com.instructure.student.mobius.assignmentDetails.submission.text.TextSubmissionModel
import com.instructure.student.mobius.assignmentDetails.submission.url.UrlSubmissionEffect
import com.instructure.student.mobius.assignmentDetails.submission.url.UrlSubmissionEvent
import com.instructure.student.mobius.assignmentDetails.submission.url.UrlSubmissionModel
import com.instructure.student.mobius.assignmentDetails.submission.url.UrlSubmissionUpdate
import com.instructure.student.test.util.matchesEffects
import com.instructure.student.test.util.matchesFirstEffects
import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.UpdateSpec
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.net.URLEncoder

class UrlSubmissionUpdateTest : Assert() {

    private val initSpec = InitSpec(UrlSubmissionUpdate()::init)
    private val updateSpec = UpdateSpec(UrlSubmissionUpdate()::update)

    private lateinit var course: Course
    private lateinit var assignment: Assignment
    private lateinit var initModel: UrlSubmissionModel

    private val url = "https://www.instructure.com"

    @Before
    fun setup() {
        course = Course()
        assignment = Assignment(id = 1234L, courseId = course.id, name = "name")
        initModel = UrlSubmissionModel(course.id, assignment.id, initialUrl = url, isSubmittable = true)
    }

    @Test
    fun `Initializes with an InitializeUrl effect`() {
        val startModel = initModel.copy(initialUrl = url)
        initSpec
                .whenInit(startModel)
                .then(
                        assertThatFirst(
                                FirstMatchers.hasModel(startModel),
                                matchesFirstEffects
                                <UrlSubmissionModel, UrlSubmissionEffect>(UrlSubmissionEffect.InitializeUrl(url))
                        )
                )
    }

    @Test
    fun `UrlChanged event with non empty url results in model change to isSubmittable`() {
        val startModel = initModel.copy(isSubmittable = false, initialUrl = "")
        val expectedModel = startModel.copy(isSubmittable = true)

        updateSpec
                .given(startModel)
                .whenEvent(UrlSubmissionEvent.UrlChanged(url))
                .then(
                        UpdateSpec.assertThatNext(
                                NextMatchers.hasModel(expectedModel)
                        )
                )
    }

    @Test
    fun `UrlChanged event with empty url results in model change to not isSubmittable`() {
        val url = ""
        val startModel = initModel.copy(isSubmittable = true)
        val expectedModel = startModel.copy(isSubmittable = false)

        updateSpec
                .given(startModel)
                .whenEvent(UrlSubmissionEvent.UrlChanged(url))
                .then(
                        UpdateSpec.assertThatNext(
                                NextMatchers.hasModel(expectedModel)
                        )
                )
    }

    @Test
    fun `SubmitClicked event results in SubmitUrl effect`() {
        updateSpec
                .given(initModel)
                .whenEvent(UrlSubmissionEvent.SubmitClicked(url))
                .then(
                        UpdateSpec.assertThatNext(
                                matchesEffects<UrlSubmissionModel, UrlSubmissionEffect>(UrlSubmissionEffect.SubmitUrl(url, course.id, assignment.id))
                        )
                )
    }
}