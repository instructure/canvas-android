/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 *
 */

package com.instructure.canvasapi2.unit

import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class SectionUnitTest : Assert() {

    @Test
    fun sectionsTest() {
        val sections: Array<Section> = sectionJSON.parse()
        Assert.assertNotNull(sections)

        val section = sections[0]
        Assert.assertNotNull(section)
        Assert.assertTrue(section.courseId == 1098050L)
        Assert.assertTrue(section.id == 1243410L)
        Assert.assertNull(section.endAt)
    }

    @Test
    fun sectionsWithStudentsTest() {
        val sections: Array<Section> = sectionsWithStudentsJSON.parse()
        Assert.assertNotNull(sections)

        val section = sections[0]
        Assert.assertNotNull(section)

        val sectionStudents = section.students
        Assert.assertNotNull(sectionStudents)

        val user = sectionStudents?.get(0)
        Assert.assertNotNull(user)
        Assert.assertTrue(user?.id == 3558540L)
        Assert.assertNotNull(user?.name)
        Assert.assertNotNull(user?.shortName)
    }

    /**
     * course section
     * @GET("/{courseid}/sections")
     * void getFirstPageSectionsList(@Path("courseid") long courseID, Callback<Section[]> callback);
     */
    @Language("JSON")
    private val sectionJSON = """
      [
        {
          "course_id": 1098050,
          "end_at": null,
          "id": 1243410,
          "name": "IOS Topdown 4 (June 19 2013)",
          "nonxlist_course_id": null,
          "start_at": null
        }
      ]"""


    /**
     * sections with students
     * @GET("/{courseid}/sections?include[]=students")
     * void getCourseSectionsWithStudents(@Path("courseid") long courseID, Callback<Section[]> callback);
     */
    @Language("JSON")
    private val sectionsWithStudentsJSON = """
      [
        {
          "course_id": 1098050,
          "end_at": null,
          "id": 1243410,
          "name": "IOS Topdown 4 (June 19 2013)",
          "nonxlist_course_id": null,
          "start_at": null,
          "students": [
            {
              "id": 3558540,
              "name": "S3First S3Last(5C)",
              "sortable_name": "S3Last(5C), S3First",
              "short_name": "S3First S3Last(5C)"
            },
            {
              "id": 3564935,
              "name": "S6First S6Last(IPad 3)",
              "sortable_name": "3), S6First S6Last(IPad",
              "short_name": "s6"
            },
            {
              "id": 3564934,
              "name": "S5First S5Last(4X)",
              "sortable_name": "S5Last(4X), S5First",
              "short_name": "S5First S5Last(4X)"
            },
            {
              "id": 3558541,
              "name": "S4First S4Last(Mini Retina)",
              "sortable_name": "Retina), S4First S4Last(Mini",
              "short_name": "S4First S4Last(Mini Retina)"
            },
            {
              "id": 3558537,
              "name": "S2First S2Last(Mini v1)",
              "sortable_name": "v1), S2First S2Last(Mini",
              "short_name": "S2First S2Last(Mini v1)"
            },
            {
              "id": 3564936,
              "name": "S7First S7Last",
              "sortable_name": "S7Last, S7First",
              "short_name": "S7First S7Last"
            },
            {
              "id": 3558536,
              "name": "S1First S1Last(5S)",
              "sortable_name": "S1Last(5S), S1First",
              "short_name": "S1First S1Last(5S)"
            }
          ]
        }
      ]"""

}
