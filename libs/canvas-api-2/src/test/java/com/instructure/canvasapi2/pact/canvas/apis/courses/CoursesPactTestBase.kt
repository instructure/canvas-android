package com.instructure.canvasapi2.pact.canvas.apis.courses

import au.com.dius.pact.consumer.Pact
import au.com.dius.pact.consumer.PactProviderRuleMk2
import au.com.dius.pact.consumer.PactVerification
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.PactSpecVersion
import au.com.dius.pact.model.RequestResponsePact
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.models.Course
import io.pactfoundation.consumer.dsl.LambdaDsl
import io.pactfoundation.consumer.dsl.LambdaDslObject
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

    fun LambdaDslObject.populateEnrollmentFields(id: Long, withEnrollment: Boolean = false) : LambdaDslObject {
        this
                .numberValue("id", id)
                .numberType("course_id")
                .numberType("course_section_id")
                .stringType("enrollment_state")
                .numberType("user_id")


        if(withEnrollment) {
            this
                    .decimalType("computed_current_score")
                    .decimalType("computed_final_scpre")
                    .stringType("computed_current_grade")
                    .stringType("computed_final_grade")
        }

        // tough to unravel what else to include...

        return this
//        val role: EnrollmentType? = EnrollmentType.NoEnrollment, // The enrollment role, for course-level permissions - this field will match `type` if the enrollment role has not been customized
//        val type: EnrollmentType? = EnrollmentType.NoEnrollment,
//        // Only included when we get enrollments using the user's url: /users/self/enrollments
//        val grades: Grades? = null,
//        @SerializedName("multiple_grading_periods_enabled")
//        val multipleGradingPeriodsEnabled: Boolean = false,
//        @SerializedName("totals_for_all_grading_periods_option")
//        val totalsForAllGradingPeriodsOption: Boolean = false,
//        @SerializedName("current_period_computed_current_score")
//        val currentPeriodComputedCurrentScore: Double? = null,
//        @SerializedName("current_period_computed_final_score")
//        val currentPeriodComputedFinalScore: Double? = null,
//        @SerializedName("current_period_computed_current_grade")
//        val currentPeriodComputedCurrentGrade: String? = null,
//        @SerializedName("current_period_computed_final_grade")
//        val currentPeriodComputedFinalGrade: String? = null,
//        @SerializedName("current_grading_period_id")
//        val currentGradingPeriodId: Long = 0,
//        @SerializedName("current_grading_period_title")
//        val currentGradingPeriodTitle: String? = null,
//        // The unique id of the associated user. Will be null unless type is ObserverEnrollment.
//        @SerializedName("associated_user_id")
//        val associatedUserId: Long = 0,
//        @SerializedName("last_activity_at")
//        val lastActivityAt: Date? = null,
//        @SerializedName("limit_privileges_to_course_section")
//        val limitPrivilegesToCourseSection: Boolean = false,
//        @SerializedName("observed_user")
//        val observedUser: User? = null,
//        var user: User? = null


    }
    fun LambdaDslObject.populateCourseFields(id: Long, favorite: Boolean? = null) : LambdaDslObject {
        this
                .numberValue("id", id)
                .stringType("original_name")
                .stringType("course_code")
                .stringType("start_at")
                .stringType("end_at")
                .stringType("syllabus_body")
                .booleanType("hide_final_grades", true)

        if(favorite != null) {
            this.booleanValue("is_favorite", favorite)
        }
        else {
            this.booleanType("is_favorite", true)
        }

        return this
    }

    val favoriteCourseId1 = 1L
    val favoriteCourseId2 = 2L

    val favoriteCoursesBody = LambdaDsl.newJsonArray { array ->
        array.`object` { obj ->
            obj.populateCourseFields(id = favoriteCourseId1, favorite = true)
        }
        array.`object` { obj ->
            obj.populateCourseFields(id = favoriteCourseId2, favorite = true)
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

        assertCoursePopulated(desc = "first course", course = course0, isFavorite = true)
        assertCoursePopulated(desc = "second course", course = course1, isFavorite = true)
    }

    private fun assertCoursePopulated(desc: String, course: Course, isFavorite: Boolean? = null) {
        assertNotNull("$desc + id", course.id)
        assertNotNull("$desc + originalName", course.originalName)
        assertNotNull("$desc + courseCod", course.courseCode)
        assertNotNull("$desc + startAt", course.startAt)
        assertNotNull("$desc + endAt", course.endAt)
        assertNotNull("$desc + syllabusBody", course.syllabusBody)
        assertNotNull("$desc + hideFinalGrades", course.hideFinalGrades)
        assertNotNull("$desc + isFavorite", course.isFavorite)

        if(isFavorite != null) {
            assertEquals("$desc isFavorite", isFavorite, course.isFavorite)
        }
    }
}