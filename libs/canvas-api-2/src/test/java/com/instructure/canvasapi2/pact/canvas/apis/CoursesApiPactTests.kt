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
import com.instructure.canvasapi2.pact.canvas.apis.ApiPactTestBase
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
import retrofit2.http.GET

class CoursesApiPactTests : ApiPactTestBase() {

    // Common logic
    private fun createService() : CourseAPI.CoursesInterface {

        val client = getClient()
        return client.create(CourseAPI.CoursesInterface::class.java)
    }


    //
    //region Test grabbing favorite courses
    //

    val favoriteCoursesQuery = "include[]=term&include[]=total_scores&include[]=license&include[]=is_public&include[]=needs_grading_count&include[]=permissions&include[]=current_grading_period_scores&include[]=course_image&include[]=favorites"
    val favoriteCoursesFieldInfo = listOf(
            PactCourseFieldInfo.fromQueryString(courseId = 1, isFavorite = true, query = favoriteCoursesQuery),
            PactCourseFieldInfo.fromQueryString(courseId = 2, isFavorite = true, query = favoriteCoursesQuery)
    )
    val favoriteCoursesResponseBody =  LambdaDsl.newJsonArray { array ->
        for(fieldInfo in favoriteCoursesFieldInfo) {
            array.`object` { obj ->
                obj.populateCourseFields(fieldInfo)
            }
        }
    }.build()

    @Pact(consumer = "mobile")
    fun getFavoriteCoursesPact(builder: PactDslWithProvider) : RequestResponsePact {
        return builder
                .given(MAIN_PROVIDER_STATE)

                .uponReceiving("A request for favorite courses")
                .path("/api/v1/users/self/favorites/courses")
                .method("GET")
                .query(favoriteCoursesQuery)
                // TODO: Headers

                .willRespondWith()
                .status(200)
                .body(favoriteCoursesResponseBody)
                // TODO: Headers

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getFavoriteCoursesPact")
    fun `grab favorited courses`() {
        val service = createService()

        val getFavoritesCall = service.favoriteCourses
        val getFavoritesResult = getFavoritesCall.execute()

        assertNotNull("Expected non-null response body", getFavoritesResult.body())
        val courseList = getFavoritesResult.body()!!
        assertEquals("returned list size",2, courseList.count())

        for(index in 0..courseList.size-1) {
            val course = courseList[index]
            val fieldInfo = favoriteCoursesFieldInfo[index]

            assertCoursePopulated(description = "course $index", course = course, fieldInfo = fieldInfo)
        }
    }
    //endregion

    //
    //region Test grabbing all courses
    //

    val allCoursesQuery = "include[]=term&include[]=total_scores&include[]=license&include[]=is_public&include[]=needs_grading_count&include[]=permissions&include[]=favorites&include[]=current_grading_period_scores&include[]=course_image&include[]=sections&state[]=completed&state[]=available"
    val allCoursesFieldInfo = listOf(
            PactCourseFieldInfo.fromQueryString(courseId = 1, isFavorite = true, query = allCoursesQuery),
            PactCourseFieldInfo.fromQueryString(courseId = 2, isFavorite = true, query = allCoursesQuery),
            PactCourseFieldInfo.fromQueryString(courseId = 3, isFavorite = false, query = allCoursesQuery),
            PactCourseFieldInfo.fromQueryString(courseId = 4, isFavorite = false, query = allCoursesQuery)

    )
    val allCoursesResponseBody =  LambdaDsl.newJsonArray { array ->
        for(fieldInfo in allCoursesFieldInfo) {
            array.`object` { obj ->
                obj.populateCourseFields(fieldInfo)
            }
        }
    }.build()

    @Pact(consumer = "mobile")
    fun getAllCoursesPact(builder: PactDslWithProvider) : RequestResponsePact {
        return builder
                .given(MAIN_PROVIDER_STATE)

                .uponReceiving("A request for all courses")
                .path("/api/v1/courses")
                .method("GET")
                .query(allCoursesQuery)
                // TODO: Headers

                .willRespondWith()
                .status(200)
                .body(allCoursesResponseBody)
                // TODO: Headers

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getAllCoursesPact")
    fun `grab all courses`() {
        val service = createService()

        val getCoursesCall = service.firstPageCourses
        val getCoursesResult = getCoursesCall.execute()

        assertNotNull("Expected non-null response body", getCoursesResult.body())
        val courseList = getCoursesResult.body()!!
        assertEquals("returned list size",4, courseList.count())

        for(index in 0..courseList.size-1) {
            val course = courseList[index]
            val fieldInfo = allCoursesFieldInfo[index]

            assertCoursePopulated(description = "course $index", course = course, fieldInfo = fieldInfo)
        }
    }
    //endregion

    //
    //region Test grabbing a single course
    //

    val singleCourseQuery = "include[]=term&include[]=permissions&include[]=license&include[]=is_public&include[]=needs_grading_count&include[]=course_image"
    val singleCourseFieldInfo = PactCourseFieldInfo.fromQueryString(courseId = 1, isFavorite = true, query = singleCourseQuery)
    val singleCourseResponseBody = LambdaDsl.newJsonBody { obj ->
        obj.populateCourseFields(singleCourseFieldInfo)
    }.build()

    @Pact(consumer = "mobile")
    fun getSingleCoursePact(builder: PactDslWithProvider) : RequestResponsePact {
        return builder
                .given(MAIN_PROVIDER_STATE)

                .uponReceiving("A request for course 1")
                .path("/api/v1/courses/1")
                .method("GET")
                .query(singleCourseQuery)
                // TODO: Headers

                .willRespondWith()
                .status(200)
                .body(singleCourseResponseBody)
                // TODO: Headers

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getSingleCoursePact")
    fun `grab course 1`() {
        val service = createService()

        val getCourseCall = service.getCourse(1L)
        val getCourseResult = getCourseCall.execute()

        assertNotNull("Expected non-null response body", getCourseResult.body())
        val course = getCourseResult.body()!!

        assertCoursePopulated(description = "returned course", course = course, fieldInfo = singleCourseFieldInfo)
    }
    //endregion

    //
    //region Test grabbing a single course with a grade
    //

    val courseWithGradeQuery = "include[]=term&include[]=permissions&include[]=license&include[]=is_public&include[]=needs_grading_count&include[]=total_scores&include[]=current_grading_period_scores&include[]=course_image"
    val courseWithGradeFieldInfo = PactCourseFieldInfo.fromQueryString(courseId = 1, isFavorite = true, query = courseWithGradeQuery)
    val courseWithGradeResponseBody = LambdaDsl.newJsonBody { obj ->
        obj.populateCourseFields(courseWithGradeFieldInfo)
    }.build()

    @Pact(consumer = "mobile")
    fun getCourseWithGradePact(builder: PactDslWithProvider) : RequestResponsePact {
        return builder
                .given(MAIN_PROVIDER_STATE)

                .uponReceiving("A request for course 1 with grade")
                .path("/api/v1/courses/1")
                .method("GET")
                .query(courseWithGradeQuery)
                // TODO: Headers

                .willRespondWith()
                .status(200)
                .body(courseWithGradeResponseBody)
                // TODO: Headers

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getCourseWithGradePact")
    fun `grab course 1 with grade`() {
        val service = createService()

        val getCourseCall = service.getCourseWithGrade(1L)
        val getCourseResult = getCourseCall.execute()

        assertNotNull("Expected non-null response body", getCourseResult.body())
        val course = getCourseResult.body()!!

        assertCoursePopulated(description = "returned course", course = course, fieldInfo = courseWithGradeFieldInfo)
    }
    //endregion

    // TODO: Grab course with syllabus?

    //
    //region grab dashboard cards
    //

    // Should just return ids for the two favorite courses, right?
    val dashboardCardResponseBody = LambdaDsl.newJsonArray { array ->
        array.`object` { obj ->
            obj.id("id", 1L)
        }
        array.`object` { obj ->
            obj.id("id", 2L)
        }
    }.build()

    @Pact(consumer = "mobile")
    fun getDashboardCardsPact(builder: PactDslWithProvider) : RequestResponsePact {
        return builder
                .given(MAIN_PROVIDER_STATE)

                .uponReceiving("A request for user's dashboard cards")
                .path("/api/v1/dashboard/dashboard_cards")
                .method("GET")
                // TODO: Headers

                .willRespondWith()
                .status(200)
                .body(dashboardCardResponseBody)
                // TODO: Headers

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getDashboardCardsPact")
    fun `grab dashboard cards for user`() {
        val service = createService()

        val getDashboardsCall = service.dashboardCourses
        val getDashboardsResult = getDashboardsCall.execute()

        assertNotNull("Expected non-null response body", getDashboardsResult.body())
        val dashboardCards = getDashboardsResult.body()!!
        assertEquals("Dashboard card count", 2, dashboardCards.size)
        assertEquals("Dashboard card 0 id", 1, dashboardCards[0].id)
        assertEquals("Dashboard card 1 id", 2, dashboardCards[1].id)
    }

    //endregion
}