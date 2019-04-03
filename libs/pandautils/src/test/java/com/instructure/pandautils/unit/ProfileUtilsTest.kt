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

package com.instructure.pandautils.unit

import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.utils.ProfileUtils

import org.junit.Assert
import org.junit.Test

class ProfileUtilsTest : Assert() {

    @Test
    @Throws(Exception::class)
    fun getUserInitials_string1() {
        val user = User(shortName = "Billy Bob")
        val testValue = ProfileUtils.getUserInitials(user.shortName)
        Assert.assertEquals("BB", testValue)
    }

    @Test
    @Throws(Exception::class)
    fun getUserInitials_string2() {
        val user = User(shortName = "Billy Joel")
        val testValue = ProfileUtils.getUserInitials(user.shortName)
        Assert.assertEquals("BJ", testValue)
    }

    @Test
    @Throws(Exception::class)
    fun getUserInitials_string3() {
        val user = User(shortName = "Billy Bob Thorton")
        val testValue = ProfileUtils.getUserInitials(user.shortName)
        Assert.assertEquals("B", testValue)
    }

    @Test
    @Throws(Exception::class)
    fun getUserInitials_various() {
        Assert.assertEquals("BB", ProfileUtils.getUserInitials("Billy Bob"))
        Assert.assertEquals("BB", ProfileUtils.getUserInitials("billy bob"))
        Assert.assertEquals("BB", ProfileUtils.getUserInitials("Billy    Bob"))
        Assert.assertEquals("BB", ProfileUtils.getUserInitials("  Billy Bob"))
        Assert.assertEquals("BB", ProfileUtils.getUserInitials("Billy Bob   "))

        Assert.assertEquals("B", ProfileUtils.getUserInitials("Billy Bob Joe"))
        Assert.assertEquals("B", ProfileUtils.getUserInitials("Billy"))
        Assert.assertEquals("B", ProfileUtils.getUserInitials("   Billy"))
        Assert.assertEquals("B", ProfileUtils.getUserInitials("Billy   "))
        Assert.assertEquals("B", ProfileUtils.getUserInitials("billy"))

        Assert.assertEquals("?", ProfileUtils.getUserInitials(""))
        Assert.assertEquals("?", ProfileUtils.getUserInitials(" "))
        Assert.assertEquals("?", ProfileUtils.getUserInitials("\t"))
        Assert.assertEquals("?", ProfileUtils.getUserInitials(null))
    }

}
