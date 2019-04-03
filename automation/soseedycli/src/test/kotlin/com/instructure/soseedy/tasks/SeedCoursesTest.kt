//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//


package com.instructure.soseedy.tasks

import com.instructure.soseedy.SoSeedy
import com.instructure.soseedy.utils.CommandLineTest
import org.junit.Assert.assertTrue
import org.junit.Test

class SeedCoursesTest : CommandLineTest() {

    // If the class is ever refactored, this will still return the correct name of the class
    private val commandName = SeedCourses::class.javaObjectType.toString().split(".").last()

    @Test
    fun missingRequiredOptions() {
        SoSeedy.main(arrayOf(commandName))
        assertTrue(stdOut.toString().contains(missingRequiredOptions))
    }

    @Test
    fun missingRequiredNumber() {
        SoSeedy.main(arrayOf(commandName, "-r", "student"))
        assertTrue(stdOut.toString().contains(missingRequiredOption))
    }

    @Test
    fun missingRequiredRole() {
        SoSeedy.main(arrayOf(commandName, "-n", "1"))
        assertTrue(stdOut.toString().contains(missingRequiredOption))
    }

    @Test
    fun run() {
        SoSeedy.main(arrayOf(commandName, "-n", "1", "-r", "student"))
        assertTrue(testCsvFile().readText().isNotEmpty())
    }
}
