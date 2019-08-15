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

package com.instructure.canvas.espresso.mockCanvas

import com.instructure.canvas.espresso.mockCanvas.utils.Randomizer
import com.instructure.canvasapi2.apis.DiscussionAPI
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.toApiString
import org.threeten.bp.OffsetDateTime
import java.util.*

class MockCanvas {
    /** Fake domain */
    var domain = "mock-data.instructure.com"

    /** Only supporting one account for now */
    var account = Account(
        id = 1L,
        name = "Fake Data Account",
        effectiveLocale = "en"
    )

    /** Canvas brand variables */
    var brandVariables = CanvasTheme(
        brand = "#25d6ab",
        fontColorDark = "#2D3B45",
        button = "#008ee2",
        buttonText = "#ffffff",
        primary = "#394B58",
        primaryText = "#ffffff",
        accent = "#7a25cb",
        logoUrl = Randomizer.randomImageUrlSmall()
    )

    /** Map of auth token to associated user id */
    val tokens = mutableMapOf<String, Long>()

    /** Map of LaunchDefinition id to object */
    val launchDefinitions = mutableMapOf<Long, LaunchDefinition>()

    /** Map of user id to user object */
    val users = mutableMapOf<Long, User>()

    /** Map of term id to term object */
    val terms = mutableMapOf<Long, Term>()

    /** Map of course id to course object */
    val courses = mutableMapOf<Long, Course>()

    /** Map of enrollment id to enrollment object */
    val enrollments = mutableMapOf<Long, Enrollment>()

    /** Map of user id to user's canvas colors */
    val userColors = mutableMapOf<Long, CanvasColor>()
        .withDefault { CanvasColor() }

    /** Map of user id to user settings object */
    val userSettings = mutableMapOf<Long, UserSettings>()

    /** Map of account notification id to object */
    val accountNotifications = mutableMapOf<Long, AccountNotification>()

    /** Map of conversation id to conversation object */
    val conversations = mutableMapOf<Long, Conversation>()

    /** Map of announcement id to DiscussionTopic */
    val announcements = mutableMapOf<Long, DiscussionTopic>()

    /** Map of group id to group object */
    val groups = mutableMapOf<Long, Group>()

    /** Map of course ID to tabs for the course */
    val courseTabs = mutableMapOf<Long, List<Tab>>()

    //region Convenience functionality

    /** A list of users with at least one Student enrollment */
    val students: List<User>
        get() = enrollments
            .values
            .filter { it.role == Enrollment.EnrollmentType.Student }
            .distinctBy { it.userId }
            .map { users[it.userId]!! }

    /** A list of users with at least one Teacher enrollment */
    val teachers: List<User>
        get() = enrollments
            .values
            .filter { it.role == Enrollment.EnrollmentType.Teacher }
            .distinctBy { it.userId }
            .map { users[it.userId]!! }

    /** A list of users with at least one Parent (i.e. Observer) enrollment */
    val parents: List<User>
        get() = enrollments
            .values
            .filter { it.role == Enrollment.EnrollmentType.Observer }
            .distinctBy { it.userId }
            .map { users[it.userId]!! }

    /** Returns the current auth token for the specified user. Returns null if no such token exists. */
    fun tokenFor(user: User): String? {
        tokens.forEach { (token, userId) ->
            if (userId == user.id) return token
        }
        return null
    }

    //endregion

    companion object {
        /** Whether the mock Canvas data has been initialized for the current test run */
        val isInitialized: Boolean get() = ::data.isInitialized

        /** The mock Canvas data for the current test run */
        lateinit var data: MockCanvas
    }

}

/**
 * Initializes an instance of [MockCanvas] for the current test run and populates some core data needed by most tests.
 *
 * Generally you will want to specify a non-zero value for either [courseCount] or [pastCourseCount]. Past courses have
 * an end-at date in the past but are otherwise identical to normal courses. [favoriteCourseCount] determines how many
 * of the generated courses are marked as favorite; this number does not apply to past courses and should not exceed
 * the specified [courseCount].
 *
 * Specifying non-zero values for [studentCount], [teacherCount], or [parentCount] requires that *at least one* course
 * or past course is specified as well, otherwise the role-specific course enrollments for these users cannot be created.
 *
 * Non-zero values for [studentCount] or [teacherCount] will create users with associated enrollments in all courses.
 * A non-zero [parentCount] will create users with observer enrollments that observe all of the generated students. As
 * such if [parentCount] is non-zero it is also required that [studentCount] be non-zero for correct behavior.
 */
fun MockCanvas.Companion.init(
    courseCount: Int = 0,
    pastCourseCount: Int = 0,
    favoriteCourseCount: Int = 0,
    studentCount: Int = 0,
    teacherCount: Int = 0,
    parentCount: Int = 0,
    accountNotificationCount: Int = 0
): MockCanvas {
    data = MockCanvas()

    // Add a default term
    data.addTerm("Default Term")

    // Add courseCount
    repeat(courseCount) { data.addCourse(isFavorite = it < favoriteCourseCount) }
    repeat(pastCourseCount) { data.addCourse(concluded = true) }

    // Add users
    val studentUsers = List(studentCount) { data.addUser() }
    val teacherUsers = List(teacherCount) { data.addUser() }
    val parentUsers = List(parentCount) { data.addUser() }

    // Add enrollments
    data.courses.values.forEach { course ->
        // Enroll teachers
        teacherUsers.forEach { data.addEnrollment(it, course, Enrollment.EnrollmentType.Teacher) }

        // Enroll students
        studentUsers.forEach { data.addEnrollment(it, course, Enrollment.EnrollmentType.Student) }

        // Enroll parents
        parentUsers.forEach { parent ->
            studentUsers.forEach { student ->
                data.addEnrollment(parent, course, Enrollment.EnrollmentType.Observer, student)
            }
        }
    }

    repeat(accountNotificationCount) { data.addAccountNotification() }

    return data
}

/** Creates a new Course and adds it to MockCanvas */
fun MockCanvas.addCourse(isFavorite: Boolean = false, concluded: Boolean = false): Course {
    val randomCourseName = Randomizer.randomCourseName()
    val endAt = if (concluded) OffsetDateTime.now().minusWeeks(1).toApiString() else null
    val course = Course(
        id = courses.size + 1L,
        name = randomCourseName,
        originalName = randomCourseName,
        courseCode = randomCourseName.substring(0, 2),
        term = terms.values.first(),
        endAt = endAt,
        isFavorite = isFavorite
    )
    courses += course.id to course

    // For now, give all courses tabs for assignments and quizzes
    val assignmentsTab = Tab(position = 0,label = "Assignments",visibility = "public")
    val quizzesTab = Tab(position = 1,label = "Quizzes",visibility = "public")
    courseTabs += course.id to listOf(assignmentsTab,quizzesTab)

    return course
}

/** Creates a new Term and adds it to MockCanvas */
fun MockCanvas.addTerm(name: String = Randomizer.randomEnrollmentTitle()): Term {
    val term = Term(
        terms.size + 1L,
        name
    )
    terms += term.id to term
    return term
}

/** Creates a new Enrollment and adds it to MockCanvas */
fun MockCanvas.addEnrollment(
    user: User,
    course: Course,
    type: Enrollment.EnrollmentType,
    observedUser: User? = null
): Enrollment {
    val enrollment = Enrollment(
        id = enrollments.size + 1L,
        role = type,
        type = type,
        courseId = course.id,
        enrollmentState = "active",
        userId = user.id,
        observedUser = observedUser,
        grades = Grades(currentScore = 88.1, currentGrade = "B+")
    )
    enrollments += enrollment.id to enrollment
    course.enrollments?.add(enrollment) // You won't see grades in the dashboard unless the course has enrollments
    return enrollment
}

/** Creates a new AccountNotification and adds it to MockCanvas */
fun MockCanvas.addAccountNotification(): AccountNotification {
    val notification = AccountNotification(
        accountNotifications.size + 1L,
        Randomizer.randomConversationSubject(),
        Randomizer.randomPageBody(),
        OffsetDateTime.now().minusDays(1).toApiString()!!,
        OffsetDateTime.now().plusDays(1).toApiString()!!,
        AccountNotification.ACCOUNT_NOTIFICATION_QUESTION
    )
    accountNotifications += notification.id to notification
    return notification
}

/** Creates a new User, auth token, and UserSettings and adds them to MockCanvas */
fun MockCanvas.addUser(): User {
    val name = Randomizer.randomName()
    val email = Randomizer.randomEmail()
    val user = User(
        id = users.size + 1L,
        name = name.fullName,
        shortName = name.firstName,
        loginId = email,
        avatarUrl = null,
        primaryEmail = email,
        email = email,
        sortableName = name.sortableName,
        bio = "This is user '${name.fullName}'",
        effective_locale = "en"
    )
    users += user.id to user
    tokens += UUID.randomUUID().toString() to user.id
    userSettings += user.id to UserSettings()
    return user
}

