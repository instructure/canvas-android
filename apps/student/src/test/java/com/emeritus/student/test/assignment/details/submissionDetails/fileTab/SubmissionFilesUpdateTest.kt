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
package com.emeritus.student.test.assignment.details.submissionDetails.fileTab

import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Course
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.files.SubmissionFilesEffect
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.files.SubmissionFilesEvent
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.files.SubmissionFilesModel
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.files.SubmissionFilesUpdate
import com.instructure.student.test.util.matchesEffects
import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SubmissionFilesUpdateTest : Assert() {

    private val initSpec = InitSpec(SubmissionFilesUpdate()::init)
    private val updateSpec = UpdateSpec(SubmissionFilesUpdate()::update)

    private lateinit var initModel: SubmissionFilesModel

    @Before
    fun setup() {
        initModel = SubmissionFilesModel(
            canvasContext = Course(),
            files = listOf(
                Attachment(id = 123L),
                Attachment(id = 456L)
            ),
            selectedFileId = 123L
        )
    }

    @Test
    fun `Initializes without model changes and without effects`() {
        val expectedModel = initModel.copy()
        initSpec
            .whenInit(initModel)
            .then(
                assertThatFirst(
                    FirstMatchers.hasModel(expectedModel),
                    FirstMatchers.hasNoEffects()
                )
            )
    }

    @Test
    fun `FileClicked event produces no change if file is already selected`() {
        updateSpec.given(initModel)
            .whenEvent(SubmissionFilesEvent.FileClicked(123L))
            .then(
                assertThatNext(
                    NextMatchers.hasNothing()
                )
            )
    }

    @Test
    fun `FileClicked event updates model and produces BroadcastFileSelected effect`() {
        val expectedModel = initModel.copy(selectedFileId = 456L)
        val expectedEffect = SubmissionFilesEffect.BroadcastFileSelected(Attachment(id= 456L))
        updateSpec.given(initModel)
            .whenEvent(SubmissionFilesEvent.FileClicked(456L))
            .then(
                assertThatNext<SubmissionFilesModel, SubmissionFilesEffect>(
                    hasModel(expectedModel),
                    matchesEffects(expectedEffect)
                )
            )
    }

}
