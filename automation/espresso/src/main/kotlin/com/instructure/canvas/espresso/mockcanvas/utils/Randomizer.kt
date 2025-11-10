/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
@file:Suppress("unused")

package com.instructure.canvas.espresso.mockcanvas.utils

import com.github.javafaker.Faker
import java.util.Date
import java.util.UUID

/**
 * Provides access to numerous convenience functions for generating random data
 *
 * It is expected that [Randomizer] will be used to generate only scalar values or very lightweight structures
 * such as simple data classes. This class is not intended to be used for generating complex items like entire
 * Courses or Assignments; high levels of randomization in such items can easily lead to unintended flakiness in tests.
 * Instead, generation of these items should generally be implemented in extension functions on [MockCanvas] where
 * random data is used only where necessary.
 */
object Randomizer {
    private val faker = Faker()

    /** Creates a random UUID */
    private fun randomUUID() = UUID.randomUUID().toString()

    fun randomPairingCode() = faker.number().digits(6)

    /** Creates a random [FakeName] */
    fun randomName() = FakeName(faker.name().firstName(), faker.name().lastName())

    /** Creates random name for a course */
    fun randomCourseName(): String = faker.educator().course() + " " + randomUUID()

    /** Creates a random string from Lorem from the first 'wordCount' words of it. */
    fun getLoremWords(wordCount: Int): String = faker.lorem().sentence(wordCount - 1)

    /** Creates a random description for a course */
    fun randomCourseDescription() : String = faker.lorem().sentence()

    /** Creates random name for a course section */
    fun randomSectionName(): String = faker.pokemon().name() + " Section"

    /** Creates a random password */
    fun randomPassword(): String = randomUUID()

    /** Creates a random email using a timestamp and a UUID */
    fun randomEmail(): String = "${Date().time}@${randomUUID()}.com"

    /** Creates a random avatar URL */
    fun randomAvatarUrl(): String = faker.avatar().image()

    /** Creates a Url to a small image */
    fun randomImageUrlSmall(): String = faker.internet().image(64, 64, false, null)

    /** Create random text for a conversation subject */
    fun randomConversationSubject(): String = faker.chuckNorris().fact()

    /** Creates random text for a conversation body */
    fun randomConversationBody(): String = faker.lorem().paragraph()

    /** Creates a random Term name */
    fun randomEnrollmentTitle(): String = "${faker.pokemon()} Term"

    /** Creates a random title for a grading period set */
    fun randomGradingPeriodSetTitle(): String = "${faker.pokemon().location()} Set"

    /** Creates a random grading period name */
    fun randomGradingPeriodName(): String = "${faker.pokemon().name()} Grading Period"

    /** Creates a random page title with a UUID to avoid Canvas URL collisions */
    fun randomPageTitle(): String = faker.gameOfThrones().house() + " " + randomUUID()

    /** Creates a random page body */
    fun randomPageBody(): String = "<p><strong>" + faker.gameOfThrones().quote() + "</strong></p><p>" + faker.lorem().paragraph() + "</p>"

    /** Creates a random Course Group Category name */
    fun randomCourseGroupCategoryName(): String = faker.harryPotter().character()

    /** Creates random name for an assignment */
    fun randomAssignmentName(): String = "${faker.starTrek().character()} ${UUID.randomUUID()}"

    fun randomAlertTitle(): String = faker.starTrek().location()

}

/** Represents a fake user name */
data class FakeName(val firstName: String, val lastName: String) {
    val fullName get() = "$firstName $lastName"
    val sortableName get() = "$lastName, $firstName"
}
