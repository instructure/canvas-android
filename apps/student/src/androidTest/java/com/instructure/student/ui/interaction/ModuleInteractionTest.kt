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

class ModuleInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.MODULES, TestCategory.INTERACTION, true, FeatureCategory.ASSIGNMENTS)
    fun testModules_launchesIntoAssignment() {
        // Tapping an Assignment module item should navigate to that item's detail page
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.MODULES, TestCategory.INTERACTION, true, FeatureCategory.DISCUSSIONS)
    fun testModules_launchesIntoDiscussion() {
        // Tapping a Discussion module item should navigate to that item's detail page
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.MODULES, TestCategory.INTERACTION, true)
    fun testModules_launchesIntoExternalTool() {
        // Tapping an ExternalTool module item should navigate to that item's detail page
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.MODULES, TestCategory.INTERACTION, true)
    fun testModules_launchesIntoExternalURL() {
        // Tapping an ExternalURL module item should navigate to that item's detail page
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.MODULES, TestCategory.INTERACTION, true, FeatureCategory.FILES)
    fun testModules_launchesIntoFile() {
        // Tapping a File module item should navigate to that item's detail page
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.MODULES, TestCategory.INTERACTION, true, FeatureCategory.PAGES)
    fun testModules_launchesIntoPage() {
        // Tapping a Page module item should navigate to that item's detail page
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.MODULES, TestCategory.INTERACTION, true, FeatureCategory.QUIZZES)
    fun testModules_launchesIntoQuiz() {
        // Tapping a Quiz module item should navigate to that item's detail page
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.MODULES, TestCategory.INTERACTION, true)
    fun testModules_modulesExpandAndCollapse() {
        // Tapping a module should collapse and hide all of that module's items in the module list
        // Tapping a collapsed module should expand it
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.MODULES, TestCategory.INTERACTION, true)
    fun testModules_navigateBackToModuleListFromModuleItem() {
        // After entering the detail page for a module item, pressing the back button or back arrow should navigate back
        // to the module list. This should also work if the detail page is accessed via deep link
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.MODULES, TestCategory.INTERACTION, true)
    fun testModules_navigateToNextAndPreviousModuleItems() {
        // When viewing the detail page for an item in a module with multiple items, the detail page should have
        // 'next' and 'previous' navigation buttons. Clicking these should navigate to the next/previous module items.
    }
}
