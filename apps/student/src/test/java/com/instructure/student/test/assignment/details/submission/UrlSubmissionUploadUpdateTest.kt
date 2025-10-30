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
import com.instructure.student.mobius.assignmentDetails.submission.url.*
import com.instructure.student.test.util.matchesEffects
import com.instructure.student.test.util.matchesFirstEffects
import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.UpdateSpec
import org.junit.Assert
import org.junit.Before
import org.junit.Test


class UrlSubmissionUploadUpdateTest : Assert() {

    private val initSpec = InitSpec(UrlSubmissionUploadUpdate()::init)
    private val updateSpec = UpdateSpec(UrlSubmissionUploadUpdate()::update)

    private lateinit var course: Course
    private lateinit var assignment: Assignment
    private lateinit var initModel: UrlSubmissionUploadModel

    private val defaultValidUrl = "https://www.instructure.com"

    @Before
    fun setup() {
        course = Course()
        assignment = Assignment(id = 1234L, courseId = course.id, name = "name")
        initModel = UrlSubmissionUploadModel(course, assignment.id, initialUrl = defaultValidUrl, isSubmittable = true, assignmentName = assignment.name)
    }

    @Test
    fun `Initializes with an InitializeUrl effect`() {
        val startModel = initModel.copy(initialUrl = defaultValidUrl)
        initSpec
            .whenInit(startModel)
            .then(
                assertThatFirst(
                    FirstMatchers.hasModel(startModel),
                    matchesFirstEffects
                    <UrlSubmissionUploadModel, UrlSubmissionUploadEffect>(
                        UrlSubmissionUploadEffect.InitializeUrl(defaultValidUrl)
                    )
                )
            )
    }

    @Test
    fun `UrlChanged event with url containing http protocol sends empty preview effect`() {
        val startModel = initModel.copy(isSubmittable = false, urlError = MalformedUrlError.NONE)
        val expectedModel = startModel.copy(isSubmittable = true, urlError = MalformedUrlError.CLEARTEXT)
        val url = "http://www.instructure.com"
        val expectedPreviewUrl = ""

        updateSpec
            .given(startModel)
            .whenEvent(UrlSubmissionUploadEvent.UrlChanged(url))
            .then(
                UpdateSpec.assertThatNext<UrlSubmissionUploadModel, UrlSubmissionUploadEffect>(
                    NextMatchers.hasModel(expectedModel),
                    matchesEffects(
                        UrlSubmissionUploadEffect.ShowUrlPreview(expectedPreviewUrl)
                    )
                )
            )
    }

    @Test
    fun `UrlChanged event with blank url results in model change`() {
        val url = ""
        val startModel = initModel.copy(isSubmittable = true, urlError = MalformedUrlError.CLEARTEXT)
        val expectedModel = startModel.copy(isSubmittable = false, urlError = MalformedUrlError.NONE)

        updateSpec
            .given(startModel)
            .whenEvent(UrlSubmissionUploadEvent.UrlChanged(url))
            .then(
                UpdateSpec.assertThatNext(
                    NextMatchers.hasModel(expectedModel)
                )
            )
    }

    @Test
    fun `UrlChanged event with invalid url results in model change to !isSubmittable`() {
        val invalidUrl = "abc123"
        val startModel = initModel.copy(isSubmittable = true)
        val expectedModel = startModel.copy(isSubmittable = false)

        updateSpec
            .given(startModel)
            .whenEvent(UrlSubmissionUploadEvent.UrlChanged(invalidUrl))
            .then(
                UpdateSpec.assertThatNext(
                    NextMatchers.hasModel(expectedModel)
                )
            )
    }

    @Test
    fun `UrlChanged event with url that doesn't have https prepended prepends https to url in resulting effect`() {
        val startModel = initModel
        val expectedModel = initModel.copy(
            isSubmittable = true
        )
        val url = "www.instructure.com"
        val expectedPreviewUrl = "https://$url"
        val expectedPreviewUrlEffect = UrlSubmissionUploadEffect.ShowUrlPreview(expectedPreviewUrl)

        updateSpec
            .given(startModel)
            .whenEvent(UrlSubmissionUploadEvent.UrlChanged(url))
            .then(
                UpdateSpec.assertThatNext<UrlSubmissionUploadModel, UrlSubmissionUploadEffect>(
                    NextMatchers.hasModel(expectedModel),
                    NextMatchers.hasEffects(expectedPreviewUrlEffect)
                )
            )
    }

    @Test
    fun `UrlChanged event with valid url results in model change to isSubmittable and hide error effect`() {
        val startModel = initModel.copy(isSubmittable = false, initialUrl = "")
        val expectedModel = startModel.copy(isSubmittable = true)

        updateSpec
            .given(startModel)
            .whenEvent(UrlSubmissionUploadEvent.UrlChanged(defaultValidUrl))
            .then(
                UpdateSpec.assertThatNext<UrlSubmissionUploadModel, UrlSubmissionUploadEffect>(
                    NextMatchers.hasModel(expectedModel),
                    NextMatchers.hasEffects(UrlSubmissionUploadEffect.ShowUrlPreview(defaultValidUrl))
                )
            )
    }

    @Test
    fun `SubmitClicked event with valid url results in SubmitUrl effect`() {
        val expectedEffect: UrlSubmissionUploadEffect = UrlSubmissionUploadEffect.SubmitUrl(defaultValidUrl, course, assignment.id, assignment.name, 1L)

        updateSpec
            .given(initModel)
            .whenEvent(UrlSubmissionUploadEvent.SubmitClicked(defaultValidUrl))
            .then(
                UpdateSpec.assertThatNext(
                    NextMatchers.hasEffects(expectedEffect)
                )
            )
    }
}