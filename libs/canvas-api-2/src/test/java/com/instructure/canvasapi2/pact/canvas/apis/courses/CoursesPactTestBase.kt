/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.canvasapi2.pact.canvas.apis.courses

import au.com.dius.pact.consumer.Pact
import au.com.dius.pact.consumer.PactProviderRuleMk2
import au.com.dius.pact.consumer.PactVerification
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.PactSpecVersion
import au.com.dius.pact.model.RequestResponsePact
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.pact.canvas.objects.PactCourseFieldInfo
import com.instructure.canvasapi2.pact.canvas.objects.assertCoursePopulated
import com.instructure.canvasapi2.pact.canvas.objects.populateCourseFields
import io.pactfoundation.consumer.dsl.LambdaDsl
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CoursesPactTestBase {

    @Rule
    @JvmField
    val provider = PactProviderRuleMk2("Canvas LMS API", PactSpecVersion.V2, this)

    fun createService() : CourseAPI.CoursesInterface {

        val client = Retrofit.Builder()
                .baseUrl(provider.url + "/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        return client.create(CourseAPI.CoursesInterface::class.java)
    }

    val favoriteCourseFields1 = PactCourseFieldInfo(courseId = 1L, isFavorite = true, numEnrollments = 1)
    val favoriteCourseFields2 = PactCourseFieldInfo(courseId = 2L, isFavorite = true)

    val favoriteCoursesBody = LambdaDsl.newJsonArray { array ->
        array.`object` { obj ->
            obj.populateCourseFields(favoriteCourseFields1)
        }
        array.`object` { obj ->
            obj.populateCourseFields(favoriteCourseFields2)
        }
    }.build()

    @Pact(consumer = "mobile")
    fun createFavoriteCoursesPact(builder: PactDslWithProvider) : RequestResponsePact {
        return builder
                .given("4 courses, 2 favorited")

                .uponReceiving("A request for favorite courses")
                .path("/api/v1/users/self/favorites/courses")
                .method("GET")
                .query("include[]=term&include[]=total_scores&include[]=license&include[]=is_public&include[]=needs_grading_count&include[]=permissions&include[]=current_grading_period_scores&include[]=course_image&include[]=favorites")
                // TODO: Headers

                .willRespondWith()
                .status(200)
                .body(favoriteCoursesBody)
                // TODO: Headers

                .toPact()
    }

    @Test
    @PactVerification(fragment = "createFavoriteCoursesPact")
    fun `should grab favorited courses`() {
        val service = createService()

        val getFavoritesCall = service.favoriteCourses
        val getFavoritesResult = getFavoritesCall.execute()

        assertNotNull("Expected non-null response body", getFavoritesResult.body())
        val courseList = getFavoritesResult.body()!!
        assertEquals("returned list size",2, courseList.count())

        val course0 = courseList[0]
        val course1 = courseList[1]

        assertCoursePopulated(description = "first course", course = course0, fieldInfo = favoriteCourseFields1)
        assertCoursePopulated(description = "second course", course = course1, fieldInfo = favoriteCourseFields2)
    }
}