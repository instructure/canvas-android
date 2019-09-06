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

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.models.Submission
import com.instructure.teacher.features.postpolicies.*
import com.instructure.teacher.unit.utils.matchesEffects
import com.instructure.teacher.unit.utils.matchesFirstEffects
import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import com.spotify.mobius.test.NextMatchers.*
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Assert
import org.junit.Test

class PostGradeUpdateTest : Assert() {
    private val initSpec = InitSpec(PostGradeUpdate()::init)
    private val updateSpec = UpdateSpec(PostGradeUpdate()::update)

    private val assignment = Assignment()
    private val initModel = PostGradeModel(assignment, false)

    @Test
    fun `Initializes into a loading state`() {
        initSpec
            .whenInit(initModel)
            .then(
                assertThatFirst<PostGradeModel, PostGradeEffect>(
                    FirstMatchers.hasModel(initModel.copy(isLoading = true)),
                    matchesFirstEffects(PostGradeEffect.LoadData(assignment))
                )
            )
    }

    @Test
    fun `PostGradesClicked results in HideGrades if isHidingGrades`() {
        val model = initModel.copy(isHidingGrades = true, sections = emptyList())
        val expectedModel = model.copy(isProcessing = true)
        updateSpec
            .given(model)
            .whenEvent(PostGradeEvent.PostGradesClicked)
            .then(
                assertThatNext<PostGradeModel, PostGradeEffect>(
                    hasModel(expectedModel),
                    matchesEffects(PostGradeEffect.HideGrades(assignment.id, emptyList()))
                )
            )
    }

    @Test
    fun `PostGradesClicked results in HideGrades with sections if isHidingGrades`() {
        val model = initModel.copy(
            isHidingGrades = true,
            sections = listOf(PostSection(Section(id = 123), true), PostSection(Section(id = 321), false))
        )
        val expectedModel = model.copy(isProcessing = true)
        updateSpec
            .given(model)
            .whenEvent(PostGradeEvent.PostGradesClicked)
            .then(
                assertThatNext<PostGradeModel, PostGradeEffect>(
                    hasModel(expectedModel),
                    matchesEffects(PostGradeEffect.HideGrades(assignment.id, listOf("123")))
                )
            )
    }

    @Test
    fun `PostGradesClicked results in PostGrades if not isHidingGrades`() {
        val model = initModel.copy(postGradedOnly = false, isHidingGrades = false, sections = emptyList())
        val expectedModel = model.copy(isProcessing = true)
        updateSpec
            .given(model)
            .whenEvent(PostGradeEvent.PostGradesClicked)
            .then(
                assertThatNext<PostGradeModel, PostGradeEffect>(
                    hasModel(expectedModel),
                    matchesEffects(PostGradeEffect.PostGrades(assignment.id, emptyList(), false))
                )
            )
    }

    @Test
    fun `PostGradesClicked results in PostGrades with graded only if not isHidingGrades`() {
        val model = initModel.copy(postGradedOnly = true, isHidingGrades = false, sections = emptyList())
        val expectedModel = model.copy(isProcessing = true)
        updateSpec
            .given(model)
            .whenEvent(PostGradeEvent.PostGradesClicked)
            .then(
                assertThatNext<PostGradeModel, PostGradeEffect>(
                    hasModel(expectedModel),
                    matchesEffects(PostGradeEffect.PostGrades(assignment.id, emptyList(), true))
                )
            )
    }

    @Test
    fun `PostGradesClicked results in PostGrades with sections if not isHidingGrades`() {
        val model = initModel.copy(
            postGradedOnly = false,
            isHidingGrades = false,
            sections = listOf(PostSection(Section(id = 123), true), PostSection(Section(id = 321), false))
        )
        val expectedModel = model.copy(isProcessing = true)
        updateSpec
            .given(model)
            .whenEvent(PostGradeEvent.PostGradesClicked)
            .then(
                assertThatNext<PostGradeModel, PostGradeEffect>(
                    hasModel(expectedModel),
                    matchesEffects(PostGradeEffect.PostGrades(assignment.id, listOf("123"), false))
                )
            )
    }

    @Test
    fun `SpecificSectionsToggled results in model specificSectionsVisible to true`() {
        val model = initModel.copy(specificSectionsVisible = false)
        val expectedModel = model.copy(specificSectionsVisible = true)
        updateSpec
            .given(model)
            .whenEvent(PostGradeEvent.SpecificSectionsToggled)
            .then(
                assertThatNext<PostGradeModel, PostGradeEffect>(
                    hasModel(expectedModel),
                    hasNoEffects()
                )
            )
    }

    @Test
    fun `SpecificSectionsToggled results in model specificSectionsVisible to false`() {
        val model = initModel.copy(specificSectionsVisible = true)
        val expectedModel = model.copy(specificSectionsVisible = false)
        updateSpec
            .given(model)
            .whenEvent(PostGradeEvent.SpecificSectionsToggled)
            .then(
                assertThatNext<PostGradeModel, PostGradeEffect>(
                    hasModel(expectedModel),
                    hasNoEffects()
                )
            )
    }

    @Test
    fun `SectionToggled results in model section to true`() {
        val sectionId = 123L
        val otherSectionId = 321L
        val model = initModel.copy(sections = listOf(
            PostSection(Section(id = sectionId), false),
            PostSection(Section(id = otherSectionId), false)
        ))
        val expectedModel = model.copy(sections = listOf(
            PostSection(Section(id = sectionId), true),
            PostSection(Section(id = otherSectionId), false)
        ))

        updateSpec
            .given(model)
            .whenEvent(PostGradeEvent.SectionToggled(sectionId))
            .then(
                assertThatNext<PostGradeModel, PostGradeEffect>(
                    hasModel(expectedModel),
                    hasNoEffects()
                )
            )
    }

    @Test
    fun `SectionToggled results in model section to false`() {
        val sectionId = 123L
        val otherSectionId = 321L
        val model = initModel.copy(sections = listOf(
            PostSection(Section(id = sectionId), true),
            PostSection(Section(id = otherSectionId), true)
        ))
        val expectedModel = model.copy(sections = listOf(
            PostSection(Section(id = sectionId), false),
            PostSection(Section(id = otherSectionId), true)
        ))

        updateSpec
            .given(model)
            .whenEvent(PostGradeEvent.SectionToggled(sectionId))
            .then(
                assertThatNext<PostGradeModel, PostGradeEffect>(
                    hasModel(expectedModel),
                    hasNoEffects()
                )
            )
    }

    @Test
    fun `GradedOnlySelected results in model graded only to true`() {
        val model = initModel.copy(postGradedOnly = false)
        val expectedModel = model.copy(postGradedOnly = true)

        updateSpec
            .given(model)
            .whenEvent(PostGradeEvent.GradedOnlySelected(true))
            .then(
                assertThatNext<PostGradeModel, PostGradeEffect>(
                    hasModel(expectedModel),
                    hasNoEffects()
                )
            )
    }

    @Test
    fun `GradedOnlySelected results in model graded only to false`() {
        val model = initModel.copy(postGradedOnly = true)
        val expectedModel = model.copy(postGradedOnly = false)

        updateSpec
            .given(model)
            .whenEvent(PostGradeEvent.GradedOnlySelected(false))
            .then(
                assertThatNext<PostGradeModel, PostGradeEffect>(
                    hasModel(expectedModel),
                    hasNoEffects()
                )
            )
    }

    @Test
    fun `DataLoaded results in model changes`() {
        val sections = listOf(
            Section(id = 123),
            Section(id = 321)
        )
        val submissions = listOf(
            Submission(id = 123),
            Submission(id = 321)
        )
        val model = initModel.copy(sections = emptyList(), submissions = emptyList())
        val expectedModel = model.copy(sections = sections.map { PostSection(it, false) }, submissions = submissions)

        updateSpec
            .given(model)
            .whenEvent(PostGradeEvent.DataLoaded(sections, submissions))
            .then(
                assertThatNext<PostGradeModel, PostGradeEffect>(
                    hasModel(expectedModel),
                    hasNoEffects()
                )
            )
    }

    @Test
    fun `GradesPosted results in ShowGradesPosted effect`() {
        val isHidingGrades = true
        val model = initModel.copy(isHidingGrades = isHidingGrades)

        updateSpec
            .given(model)
            .whenEvent(PostGradeEvent.GradesPosted)
            .then(
                assertThatNext<PostGradeModel, PostGradeEffect>(
                    hasNoModel(),
                    hasEffects(PostGradeEffect.ShowGradesPosted(isHidingGrades))
                )
            )
    }
}
