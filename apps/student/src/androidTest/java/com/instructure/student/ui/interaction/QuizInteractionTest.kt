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
package com.instructure.student.ui.interaction

import com.instructure.canvas.espresso.Stub
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import org.junit.Test

class QuizInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.QUIZZES, TestCategory.INTERACTION, true)
    fun testOpensNativeView_essayQuiz() {
        // Quizzes with just Essay questions should open in a native view and not a WebView
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.QUIZZES, TestCategory.INTERACTION, true)
    fun testOpensNativeView_fileUploadQuiz() {
        // Quizzes with just File Upload questions should open in a native view and not a WebView
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.QUIZZES, TestCategory.INTERACTION, true)
    fun testOpensNativeView_fillInTheBlankQuiz() {
        // Quizzes with just Fill In The Blank questions should open in a native view and not a WebView
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.QUIZZES, TestCategory.INTERACTION, true)
    fun testOpensNativeView_matchingQuiz() {
        // Quizzes with just Matching questions should open in a native view and not a WebView
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.QUIZZES, TestCategory.INTERACTION, true)
    fun testOpensNativeView_multipleAnswerQuiz() {
        // Quizzes with just Multiple Answer questions should open in a native view and not a WebView
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.QUIZZES, TestCategory.INTERACTION, true)
    fun testOpensNativeView_multipleChoiceQuiz() {
        // Quizzes with just Multiple Choice questions should open in a native view and not a WebView
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.QUIZZES, TestCategory.INTERACTION, true)
    fun testOpensNativeView_multipleDropdownQuiz() {
        // Quizzes with just Multiple Dropdown questions should open in a native view and not a WebView
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.QUIZZES, TestCategory.INTERACTION, true)
    fun testOpensNativeView_numericalAnswerQuiz() {
        // Quizzes with just Numerical Answer questions should open in a native view and not a WebView
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.QUIZZES, TestCategory.INTERACTION, true)
    fun testOpensNativeView_textQuiz() {
        // Quizzes with just Text questions should open in a native view and not a WebView
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.QUIZZES, TestCategory.INTERACTION, true)
    fun testOpensNativeView_trueFalseQuiz() {
        // Quizzes with just True-False questions should open in a native view and not a WebView
    }
}
