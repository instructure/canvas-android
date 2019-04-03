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

import com.instructure.canvasapi2.models.Alert

import org.junit.Test

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals


class AlertTest {

    @Test
    fun getAlertTypeFromString_COURSE_ANNOUNCEMENT() {
        assertEquals(Alert.AlertType.COURSE_ANNOUNCEMENT, Alert.getAlertTypeFromString("course_announcement"))
    }

    @Test
    fun getAlertTypeFromString_INSTITUTION_ANNOUNCEMENT() {
        assertEquals(Alert.AlertType.INSTITUTION_ANNOUNCEMENT, Alert.getAlertTypeFromString("institution_announcement"))
    }

    @Test
    fun getAlertTypeFromString_ASSIGNMENT_GRADE_HIGH() {
        assertEquals(Alert.AlertType.ASSIGNMENT_GRADE_HIGH, Alert.getAlertTypeFromString("assignment_grade_high"))
    }

    @Test
    fun getAlertTypeFromString_ASSIGNMENT_GRADE_LOW() {
        assertEquals(Alert.AlertType.ASSIGNMENT_GRADE_LOW, Alert.getAlertTypeFromString("assignment_grade_low"))
    }

    @Test
    fun getAlertTypeFromString_ASSIGNMENT_MISSING() {
        assertEquals(Alert.AlertType.ASSIGNMENT_MISSING, Alert.getAlertTypeFromString("assignment_missing"))
    }

    @Test
    fun getAlertTypeFromString_COURSE_GRADE_HIGH() {
        assertEquals(Alert.AlertType.COURSE_GRADE_HIGH, Alert.getAlertTypeFromString("course_grade_high"))
    }

    @Test
    fun getAlertTypeFromString_COURSE_GRADE_LOW() {
        assertEquals(Alert.AlertType.COURSE_GRADE_LOW, Alert.getAlertTypeFromString("course_grade_low"))
    }

    @Test
    fun getAlertTypeFromString_empty() {
        assertEquals(null, Alert.getAlertTypeFromString(""))
    }

    @Test
    fun getAlertTypeFromString_null() {
        assertEquals(null, Alert.getAlertTypeFromString(null))
    }

    @Test
    fun alertTypeToAPIString_COURSE_ANNOUNCEMENT() {
        assertEquals("course_announcement", Alert.alertTypeToAPIString(Alert.AlertType.COURSE_ANNOUNCEMENT))
    }

    @Test
    fun alertTypeToAPIString_INSTITUTION_ANNOUNCEMENT() {
        assertEquals("institution_announcement", Alert.alertTypeToAPIString(Alert.AlertType.INSTITUTION_ANNOUNCEMENT))
    }

    @Test
    fun alertTypeToAPIString_ASSIGNMENT_GRADE_HIGH() {
        assertEquals("assignment_grade_high", Alert.alertTypeToAPIString(Alert.AlertType.ASSIGNMENT_GRADE_HIGH))
    }

    @Test
    fun alertTypeToAPIString_ASSIGNMENT_GRADE_LOW() {
        assertEquals("assignment_grade_low", Alert.alertTypeToAPIString(Alert.AlertType.ASSIGNMENT_GRADE_LOW))
    }

    @Test
    fun alertTypeToAPIString_ASSIGNMENT_MISSING() {
        assertEquals("assignment_missing", Alert.alertTypeToAPIString(Alert.AlertType.ASSIGNMENT_MISSING))
    }

    @Test
    fun alertTypeToAPIString_COURSE_GRADE_HIGH() {
        assertEquals("course_grade_high", Alert.alertTypeToAPIString(Alert.AlertType.COURSE_GRADE_HIGH))
    }


    @Test
    fun alertTypeToAPIString_COURSE_GRADE_LOW() {
        assertEquals("course_grade_low", Alert.alertTypeToAPIString(Alert.AlertType.COURSE_GRADE_LOW))
    }

    @Test
    fun alertTypeToAPIString_null() {
        assertEquals(null, Alert.alertTypeToAPIString(null))
    }

    @Test
    fun alertTypeToAPIString_all() {
        // NONE is for null api values
        Alert.AlertType.values().filter { it != Alert.AlertType.NONE }.forEach {type ->
            assertNotEquals("Expected non-null API string value for AlertType." + type.name, null, Alert.alertTypeToAPIString(type))
        }
    }

    @Test
    fun getAlertTypeFromString_all() {
        // NONE is for null api values
        Alert.AlertType.values().filter { it != Alert.AlertType.NONE }.forEach {type ->
            val apiString = Alert.alertTypeToAPIString(type)
            assertEquals("Expected AlertType." + type.name + " for apiString '" + apiString + "'", type, Alert.getAlertTypeFromString(apiString))
        }
    }

}
