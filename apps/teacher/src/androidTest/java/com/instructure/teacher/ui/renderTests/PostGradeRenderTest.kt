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
package com.instructure.teacher.ui.renderTests

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Section
import com.instructure.espresso.swipeRight
import com.instructure.teacher.R
import com.instructure.teacher.features.postpolicies.PostSection
import com.instructure.teacher.features.postpolicies.ui.PostGradeFragment
import com.instructure.teacher.features.postpolicies.ui.PostGradeViewState
import com.instructure.teacher.ui.renderTests.pages.PostGradeRenderPage
import com.instructure.teacher.ui.utils.TeacherRenderTest
import com.spotify.mobius.runners.WorkRunner
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class PostGradeRenderTest : TeacherRenderTest() {

    private val page = PostGradeRenderPage()

    @Test
    fun displaysEmptyViewForHidingGrades() {
        val state = PostGradeViewState.EmptyViewState(R.drawable.ic_panda_all_hidden, "All Hidden", "They're all hidden")
        loadPageWithViewState(true, state)
        page.assertEmptyView()
    }

    @Test
    fun displaysEmptyViewForPostingGrades() {
        val state = PostGradeViewState.EmptyViewState(R.drawable.ic_panda_all_posted, "All Posted", "They're all posted")
        loadPageWithViewState(false, state)
        page.assertEmptyView()
    }

    @Test
    fun displaysPostingGrades() {
        val sectionsOn = false
        val state = PostGradeViewState.LoadedViewState(
            courseColor = 0,
            statusText = "2 grades hidden",
            gradedOnlyText = "Everyone",
            specificSectionsVisible = sectionsOn,
            postText = "Post Grades",
            postProcessing = false,
            sections = emptyList()
        )
        loadPageWithViewState(false, state)
        page.assertPostedView(sectionsOn)
    }

    @Test
    fun displaysPostingGradesWithSections() {
        val sectionsOn = true
        val state = PostGradeViewState.LoadedViewState(
            courseColor = 0,
            statusText = "2 grades hidden",
            gradedOnlyText = "Everyone",
            specificSectionsVisible = sectionsOn,
            postText = "Post Grades",
            postProcessing = false,
            sections = listOf(PostSection(Section(name = "Test Section"), true))
        )
        loadPageWithViewState(false, state)
        page.assertPostedView(sectionsOn)
    }

    @Test
    fun displaysHidingGrades() {
        val sectionsOn = false
        val state = PostGradeViewState.LoadedViewState(
            courseColor = 0,
            statusText = "2 grades posted",
            gradedOnlyText = null,
            specificSectionsVisible = sectionsOn,
            postText = "Hide Grades",
            postProcessing = false,
            sections = emptyList()
        )
        loadPageWithViewState(true, state)
        page.assertHiddenView(sectionsOn)
    }

    @Test
    fun displaysHidingGradesWithSections() {
        val sectionsOn = true
        val state = PostGradeViewState.LoadedViewState(
            courseColor = 0,
            statusText = "2 grades posted",
            gradedOnlyText = null,
            specificSectionsVisible = sectionsOn,
            postText = "Hide Grades",
            postProcessing = false,
            sections = listOf(PostSection(Section(name = "Test Section"), true))
        )
        loadPageWithViewState(true, state)
        page.assertHiddenView(sectionsOn)
    }

    private fun loadPageWithViewState(
        isHidingGrades: Boolean,
        state: PostGradeViewState,
        assignment: Assignment = Assignment(name = "Test Assignment", id = 123L, courseId = 321L)
    ): PostGradeFragment {
        val emptyEffectRunner = object : WorkRunner {
            override fun dispose() = Unit
            override fun post(runnable: Runnable) = Unit
        }
        val fragment = PostGradeFragment.newInstance(assignment, isHidingGrades).apply {
            overrideInitViewState = state
            loopMod = { it.effectRunner { emptyEffectRunner } }
        }
        activityRule.activity.loadFragment(fragment)

        if (state is PostGradeViewState.EmptyViewState) {
            page.postEmptyLayout.swipeRight()
        } else {
            page.postPolicyStatusCount.swipeRight()
        }

        return fragment
    }

}
