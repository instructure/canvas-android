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

import com.instructure.canvasapi2.models.Recipient

import org.junit.Test

import org.junit.Assert.assertEquals


class RecipientTest {

    @Test
    fun getIdAsLongTest_Group() {
        val recipient = Recipient(stringId = "group_12345")

        assertEquals(12345, recipient.idAsLong)
    }

    @Test
    fun getIdAsLongTest_Course() {
        val recipient = Recipient(stringId = "course_456789")

        assertEquals(456789, recipient.idAsLong)
    }

    @Test
    fun getIdAsLongTest_Number() {
        val recipient = Recipient(stringId = "123456789")

        assertEquals(123456789, recipient.idAsLong)
    }

    @Test
    fun getIdAsLongTest_Null() {
        val recipient = Recipient(stringId = "invalid_id")

        assertEquals(0, recipient.idAsLong)
    }

    @Test
    fun getRecipientTypeTest_Person() {
        val recipient = Recipient(stringId = "1234567")

        assertEquals(Recipient.Type.Person, recipient.recipientType)
    }

    @Test
    fun getRecipientTypeTest_Group() {
        val recipient = Recipient(stringId = "not_a_person", userCount = 2)

        assertEquals(Recipient.Type.Group, recipient.recipientType)
    }

    @Test
    fun getRecipientTypeTest_MetaGroup() {
        val recipient = Recipient(stringId = "not_a_person_or_group", userCount = 0)

        assertEquals(Recipient.Type.Metagroup, recipient.recipientType)
    }

    @Test
    fun recipientTypeToIntTest_Group() {
        assertEquals(0, Recipient.recipientTypeToInt(Recipient.Type.Group))
    }

    @Test
    fun recipientTypeToIntTest_MetaGroup() {
        assertEquals(1, Recipient.recipientTypeToInt(Recipient.Type.Metagroup))
    }

    @Test
    fun recipientTypeToIntTest_Person() {
        assertEquals(2, Recipient.recipientTypeToInt(Recipient.Type.Person))
    }

    @Test
    fun intToRecipientTypeTest_Group() {
        assertEquals(Recipient.Type.Group, Recipient.intToRecipientType(0))
    }

    @Test
    fun intToRecipientTypeTest_MetaGroup() {
        assertEquals(Recipient.Type.Metagroup, Recipient.intToRecipientType(1))
    }

    @Test
    fun intToRecipientTypeTest_Person() {
        assertEquals(Recipient.Type.Person, Recipient.intToRecipientType(2))
    }

    @Test
    fun intToRecipientTypeTest_Null() {
        assertEquals(null, Recipient.intToRecipientType(3))
    }
}