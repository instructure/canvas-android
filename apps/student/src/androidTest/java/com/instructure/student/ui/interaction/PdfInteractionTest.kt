/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */    package com.instructure.student.ui.interaction

import com.instructure.canvas.espresso.Stub
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import org.junit.Test

class PdfInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true, FeatureCategory.ANNOTATIONS)
    fun testAnnotations_viewAndSelectAnnotationsInSubmission() {

    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true, FeatureCategory.ANNOTATIONS)
    fun testAnnotations_selectAndCommentOnAnnotationWithNoExistingComments() {

    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true, FeatureCategory.ANNOTATIONS)
    fun testAnnotations_selectAndCommentOnAnnotationWithExistingComments() {

    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.FILES, TestCategory.INTERACTION, true, FeatureCategory.ANNOTATIONS)
    fun testAnnotations_openPdfFilesInPSPDFKit() {
        // Annotation toolbar icon needs to be present
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION, true, FeatureCategory.ANNOTATIONS)
    fun testAnnotations_openPdfsInPSPDFKitFromLinksInAssignment() {
        // Annotation toolbar icon needs to be present, this link is specific to assignment details, as that was the advertised use case
    }
}