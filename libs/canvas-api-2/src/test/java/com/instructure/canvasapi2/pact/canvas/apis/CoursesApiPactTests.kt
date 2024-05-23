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
package com.instructure.canvasapi2.pact.canvas.apis

import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit.PactVerification
import au.com.dius.pact.core.model.RequestResponsePact
import au.com.dius.pact.core.model.annotations.Pact
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.pact.canvas.logic.PactCourseFieldConfig
import com.instructure.canvasapi2.pact.canvas.logic.assertCoursePopulated
import com.instructure.canvasapi2.pact.canvas.logic.populateCourseFields
import io.pactfoundation.consumer.dsl.LambdaDsl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.net.URLEncoder

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
    val favoriteCoursesPath = "/api/v1/users/self/favorites/courses"
    val favoriteCoursesFieldInfo = listOf(
            PactCourseFieldConfig.fromQueryString(courseId = 3, isFavorite = true, query = favoriteCoursesQuery)
    )
    val favoriteCoursesResponseBody =  LambdaDsl.newJsonArray { array ->
        for(fieldInfo in favoriteCoursesFieldInfo) {
            array.`object` { obj ->
                obj.populateCourseFields(fieldInfo)
            }
        }
    }.build()

    @Pact(consumer = "android")
    fun getFavoriteCoursesPact(builder: PactDslWithProvider) : RequestResponsePact {
        return builder
                .given(MAIN_PROVIDER_STATE)

                .uponReceiving("A request for favorite courses")
                .path(favoriteCoursesPath)
                .method("GET")
                .query(favoriteCoursesQuery)
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(200)
                .body(favoriteCoursesResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getFavoriteCoursesPact")
    fun `grab favorited courses`() {
        val service = createService()

        val getFavoritesCall = service.favoriteCourses
        val getFavoritesResult = getFavoritesCall.execute()

        assertQueryParamsAndPath(getFavoritesCall, favoriteCoursesQuery, favoriteCoursesPath)

        assertNotNull("Expected non-null response body", getFavoritesResult.body())
        val courseList = getFavoritesResult.body()!!
        assertEquals("returned list size",1, courseList.count())

        for(index in 0..courseList.size-1) {
            val course = courseList[index]
            val fieldInfo = favoriteCoursesFieldInfo[index]

            assertCoursePopulated(description = "course $index", course = course, fieldConfig = fieldInfo)
        }
    }
    //endregion

    //
    //region Test grabbing all courses
    //

    val allCoursesQuery = "include[]=term&include[]=total_scores&include[]=license&include[]=is_public&include[]=needs_grading_count&include[]=permissions&include[]=favorites&include[]=current_grading_period_scores&include[]=course_image&include[]=banner_image&include[]=sections&include[]=settings&state[]=completed&state[]=available"
    val allCoursesPath = "/api/v1/courses"
    val allCoursesFieldInfo = listOf(
            // Evidently, permissions info is *not* returned from this call, even though include[]=permissions is specified
            PactCourseFieldConfig.fromQueryString(courseId = 2, isFavorite = false, query = allCoursesQuery).copy(includePermissions = false),
            PactCourseFieldConfig.fromQueryString(courseId = 3, isFavorite = true, query = allCoursesQuery).copy(includePermissions = false)
    )
    val allCoursesResponseBody =  LambdaDsl.newJsonArray { array ->
        for(fieldInfo in allCoursesFieldInfo) {
            array.`object` { obj ->
                obj.populateCourseFields(fieldInfo)
            }
        }
    }.build()

    @Pact(consumer = "android")
    fun getAllCoursesPact(builder: PactDslWithProvider) : RequestResponsePact {
        return builder
                .given(MAIN_PROVIDER_STATE)

                .uponReceiving("A request for all courses")
                .path(allCoursesPath)
                .method("GET")
                .query(allCoursesQuery)
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(200)
                .body(allCoursesResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getAllCoursesPact")
    fun `grab all courses`() {
        val service = createService()

        val getCoursesCall = service.firstPageCourses
        val getCoursesResult = getCoursesCall.execute()

        assertQueryParamsAndPath(getCoursesCall, allCoursesQuery, allCoursesPath)

        assertNotNull("Expected non-null response body", getCoursesResult.body())
        val courseList = getCoursesResult.body()!!
        assertEquals("returned list size",2, courseList.count())

        for(index in 0..courseList.size-1) {
            val course = courseList[index]
            val fieldInfo = allCoursesFieldInfo[index]

            assertCoursePopulated(description = "course $index", course = course, fieldConfig = fieldInfo)
        }
    }
    //endregion

    //
    //region Test grabbing a single course
    //

    val singleCourseQuery = "include[]=term&include[]=permissions&include[]=license&include[]=is_public&include[]=needs_grading_count&include[]=course_image"
    val singleCoursePath = "/api/v1/courses/3"
    val singleCourseFieldInfo = PactCourseFieldConfig.fromQueryString(courseId = 3, isFavorite = true, query = singleCourseQuery)
    val singleCourseResponseBody = LambdaDsl.newJsonBody { obj ->
        obj.populateCourseFields(singleCourseFieldInfo)
    }.build()

    @Pact(consumer = "android")
    fun getSingleCoursePact(builder: PactDslWithProvider) : RequestResponsePact {
        return builder
                .given(MAIN_PROVIDER_STATE)

                .uponReceiving("A request for course 3")
                .path(singleCoursePath)
                .method("GET")
                .query(singleCourseQuery)
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(200)
                .body(singleCourseResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getSingleCoursePact")
    fun `grab course 3`() {
        val service = createService()

        val getCourseCall = service.getCourse(3L)
        val getCourseResult = getCourseCall.execute()

        //assertQueryParamsAndPath(getCourseCall, singleCourseQuery, singleCoursePath)

        assertNotNull("Expected non-null response body", getCourseResult.body())
        val course = getCourseResult.body()!!

        assertCoursePopulated(description = "returned course", course = course, fieldConfig = singleCourseFieldInfo)
    }
    //endregion

    //
    //region Test grabbing a single course with a grade
    //

    val courseWithGradeQuery = "include[]=term&include[]=permissions&include[]=license&include[]=is_public&include[]=needs_grading_count&include[]=total_scores&include[]=current_grading_period_scores&include[]=course_image&include[]=settings&include[]=grading_scheme"
    val courseWithGradePath = "/api/v1/courses/3"
    val courseWithGradeFieldInfo = PactCourseFieldConfig.fromQueryString(courseId = 3, isFavorite = true, query = courseWithGradeQuery)
    val courseWithGradeResponseBody = LambdaDsl.newJsonBody { obj ->
        obj.populateCourseFields(courseWithGradeFieldInfo)
    }.build()

    @Pact(consumer = "android")
    fun getCourseWithGradePact(builder: PactDslWithProvider) : RequestResponsePact {
        return builder
                .given(MAIN_PROVIDER_STATE)

                .uponReceiving("A request for course 3 with grade")
                .path(courseWithGradePath)
                .method("GET")
                .query(courseWithGradeQuery)
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(200)
                .body(courseWithGradeResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getCourseWithGradePact")
    fun `grab course 3 with grade`() {
        val service = createService()

        val getCourseCall = service.getCourseWithGrade(3L)
        val getCourseResult = getCourseCall.execute()

        assertQueryParamsAndPath(getCourseCall, courseWithGradeQuery, courseWithGradePath)

        assertNotNull("Expected non-null response body", getCourseResult.body())
        val course = getCourseResult.body()!!

        assertCoursePopulated(description = "returned course", course = course, fieldConfig = courseWithGradeFieldInfo)
    }
    //endregion

    // TODO: Grab course with syllabus?

    //
    //region grab dashboard cards
    //

    // Should just return id for the favorite course, right?
    val dashboardCardPath = "/api/v1/dashboard/dashboard_cards"
    val dashboardCardResponseBody = LambdaDsl.newJsonArray { array ->
        array.`object` { obj ->
            obj.id("id", 3L)
        }
    }.build()

    @Pact(consumer = "android")
    fun getDashboardCardsPact(builder: PactDslWithProvider) : RequestResponsePact {
        return builder
                .given(MAIN_PROVIDER_STATE)

                .uponReceiving("A request for user's dashboard cards")
                .path(dashboardCardPath)
                .method("GET")
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(200)
                .body(dashboardCardResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getDashboardCardsPact")
    fun `grab dashboard cards for user`() {
        val service = createService()

        val getDashboardsCall = service.dashboardCourses
        val getDashboardsResult = getDashboardsCall.execute()

        assertQueryParamsAndPath(getDashboardsCall, null, dashboardCardPath)

        assertNotNull("Expected non-null response body", getDashboardsResult.body())
        val dashboardCards = getDashboardsResult.body()!!
        assertEquals("Dashboard card count", 1, dashboardCards.size)
        assertEquals("Dashboard card 0 id", 3, dashboardCards[0].id)
    }

    //endregion
}