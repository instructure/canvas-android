package com.instructure.dataseeding.soseedy

import com.instructure.dataseeding.api.CoursesApi
import com.instructure.dataseeding.api.EnrollmentsApi
import com.instructure.dataseeding.api.ObserverApi
import com.instructure.dataseeding.api.UserApi
import com.instructure.dataseeding.model.ObserveeApiModel
import com.instructure.dataseeding.model.ObserverAlertThresholdApiModel
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class ObserversTest {
    private val parent = UserApi.createCanvasUser()
    private val student = UserApi.createCanvasUser()
    private val course = CoursesApi.createCourse()

    @Before
    fun setUp() {
        EnrollmentsApi.enrollUserAsStudent(course.id, student.id)
    }

    @Test
    fun addObserveeWithCredentials() {
        val observee = ObserverApi.addObserverWithCredentials(student.loginId, student.password, parent.token)
        assertThat(observee, instanceOf(ObserveeApiModel::class.java))
        assertEquals("Expected student/observee name to be equal", student.name, observee.name)
        assertEquals("Expected student/observee short name to be equal", student.shortName, observee.shortName)
        assertEquals("Expected student/observee id to be equal", student.id, observee.id)
        assertEquals("Expected student/observee sortable name to be equal", student.sortableName, observee.sortableName)
    }

    @Test
    fun getObserverAlertThresholds_whenEmpty() {
        ObserverApi.addObserverWithCredentials(
                loginId = student.loginId,
                password = student.password,
                token = parent.token
        )

        val thresholds = ObserverApi.getObserverAlertThresholds(token=parent.token)
        assertNotNull(thresholds)
        assertTrue(thresholds.size == 0)
    }

    @Test
    fun addObserverAlertThreshold_assignmentGradeLow() {
        testThreshold("assignment_grade_low")
    }

    fun addObserverAlertThreshold_assignmentGradeHigh() {
        testThreshold("assignment_grade_high")
    }

    @Test
    fun addObserverAlertThreshold_courseGradeLow() {
        testThreshold("course_grade_low")
    }

    @Test
    fun addObserverAlertThreshold_courseGradeHigh() {
        testThreshold("course_grade_high")
    }

    // Common logic for addObserverAlertThreshold_xxx tests above
    private fun testThreshold(alertType: String)
    {
        ObserverApi.addObserverWithCredentials(
                loginId = student.loginId,
                password = student.password,
                token = parent.token
        )
        val threshold = ObserverApi.addObserverAlertThreshold(
                alertType = alertType,
                userId = student.id,
                observerId = parent.id,
                token = parent.token
        )
        assertThat(threshold, instanceOf(ObserverAlertThresholdApiModel::class.java))
        assertEquals(alertType, threshold.alertType)
        assertTrue(threshold.threshold.toDouble() >= 0)
        assertTrue(threshold.threshold.toDouble() <= 100)
        assertEquals("active", threshold.workflowState)
        assertEquals(student.id, threshold.userId)
        assertEquals(parent.id, threshold.observerId)
    }
}
