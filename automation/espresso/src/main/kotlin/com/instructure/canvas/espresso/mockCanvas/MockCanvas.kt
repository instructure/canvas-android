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

import android.util.Log
import com.github.javafaker.Faker
import com.instructure.canvas.espresso.mockCanvas.utils.Randomizer
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.models.Account
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.Alert
import com.instructure.canvasapi2.models.AlertThreshold
import com.instructure.canvasapi2.models.AlertType
import com.instructure.canvasapi2.models.AlertWorkflowState
import com.instructure.canvasapi2.models.AnnotationMetadata
import com.instructure.canvasapi2.models.AnnotationUrls
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentDueDate
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.BasicUser
import com.instructure.canvasapi2.models.Bookmark
import com.instructure.canvasapi2.models.CanvasColor
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.CanvasTheme
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.DiscussionParticipant
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.DiscussionTopicPermission
import com.instructure.canvasapi2.models.DocSession
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.Grades
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.LaunchDefinition
import com.instructure.canvasapi2.models.LockInfo
import com.instructure.canvasapi2.models.Message
import com.instructure.canvasapi2.models.ModuleContentDetails
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.QuizAnswer
import com.instructure.canvasapi2.models.QuizQuestion
import com.instructure.canvasapi2.models.QuizSubmission
import com.instructure.canvasapi2.models.QuizSubmissionAnswer
import com.instructure.canvasapi2.models.QuizSubmissionQuestion
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.models.RemoteFile
import com.instructure.canvasapi2.models.RubricCriterion
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.models.StreamItem
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.models.Term
import com.instructure.canvasapi2.models.TermsOfService
import com.instructure.canvasapi2.models.ThresholdWorkflowState
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.models.UserSettings
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import com.instructure.canvasapi2.models.canvadocs.CanvaDocCoordinate
import com.instructure.canvasapi2.models.canvadocs.CanvaDocInkList
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.toDate
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.threeten.bp.OffsetDateTime
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.UUID
import kotlin.random.Random

class MockCanvas {
    /** Fake domain */
    var domain = "mock-data.instructure.com"

    /** Only supporting one account for now */
    var account = Account(
            id = 1L,
            name = "Fake Data Account",
            effectiveLocale = "en"
    )

    var accountTermsOfService = TermsOfService(
            id = 1L,
            termsType = "default",
            passive = false,
            accountId = account.id,
            content = "hodor"
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

    var currentUser: User? = null
        get() {
            return field ?: users.values.first()
        }

    /** Map of term id to term object */
    val terms = mutableMapOf<Long, Term>()

    /** Map of course id to course object */
    val courses = mutableMapOf<Long, Course>()

    /** Map of course permissions to course ids **/
    val coursePermissions = mutableMapOf<Long, CanvasContextPermission>()

    /** Map of course ids to course calendar events */
    val courseCalendarEvents = mutableMapOf<Long, MutableList<ScheduleItem>>()

    /** Map of user ids to user calendar events */
    val userCalendarEvents = mutableMapOf<Long, MutableList<ScheduleItem>>()

    /** Map of enrollment id to enrollment object */
    val enrollments = mutableMapOf<Long, Enrollment>()

    /** Map of user id to user's canvas colors */
    val userColors = mutableMapOf<Long, CanvasColor>()
            .withDefault { CanvasColor() }

    /** Map of user id to user settings object */
    val userSettings = mutableMapOf<Long, UserSettings>()

    /** Map of account notification id to object */
    val accountNotifications = mutableMapOf<Long, AccountNotification>()

    /** Map of group id to group object */
    val groups = mutableMapOf<Long, Group>()

    /** Map of course ID to tabs for the course */
    val courseTabs = mutableMapOf<Long, MutableList<Tab>>()

    /** Map of course ID to course settings */
    val courseSettings = mutableMapOf<Long, CourseSettings>()

    /** Map of group ID to tabs for the group */
    val groupTabs = mutableMapOf<Long, MutableList<Tab>>()

    /** Map of course id to grading period list */
    val courseGradingPeriods = mutableMapOf<Long, MutableList<GradingPeriod>>()

    /** Map of course ID to pages for the course */
    val coursePages = mutableMapOf<Long, MutableList<Page>>()

    /** Map of group ID to pages for the group */
    val groupPages = mutableMapOf<Long, MutableList<Page>>()

    /** Map of course ID to root folders */
    val courseRootFolders = mutableMapOf<Long, FileFolder>()

    /** Map of group ID to root folders */
    val groupRootFolders = mutableMapOf<Long, FileFolder>()

    /** Map of folder IDs to folders */
    val fileFolders = mutableMapOf<Long, FileFolder>()

    /** folderId -> list of subfolders */
    val folderSubFolders = mutableMapOf<Long, MutableList<FileFolder>>()

    /** folderId -> list of files */
    val folderFiles = mutableMapOf<Long, MutableList<FileFolder>>()

    var nextItemId = 1L
    fun newItemId(): Long {
        return nextItemId++
    }

    /** Map of file contents, fileId -> String.  Just supporting string contents for now. */
    val fileContents = mutableMapOf<Long, String>()

    /** Map of course ID to assignment groups */
    val assignmentGroups = mutableMapOf<Long, MutableList<AssignmentGroup>>()

    /** Map of assignment ID to assignment */
    val assignments = mutableMapOf<Long, Assignment>()

    /** Map of todos */
    val todos = mutableListOf<PlannerItem>()

    /** Map of assignment ID to a list of submissions */
    val submissions = mutableMapOf<Long, MutableList<Submission>>()

    val ltiTools = mutableListOf<LTITool>()

    /** Map of course ids to discussion topic headers */
    val courseDiscussionTopicHeaders = mutableMapOf<Long, MutableList<DiscussionTopicHeader>>()

    /** Map of group ids to discussion topic headers */
    val groupDiscussionTopicHeaders = mutableMapOf<Long, MutableList<DiscussionTopicHeader>>()

    /** Map of topic ids to DiscussionTopics */
    val discussionTopics = mutableMapOf<Long, DiscussionTopic>()

    // A few discussion-related permissions that we need to be able to tweak.
    // These are analogous to the allowReplies, allowAttachments and allowRating
    // parameters in addDiscussionTopicToCourse() below, except that these are
    // used when creating attachments via the UI and mocked network calls.
    var discussionRepliesEnabled: Boolean = true
    var discussionAttachmentsEnabled: Boolean = true
    var discussionRatingsEnabled: Boolean = true

    /** Map of course id to module list */
    val courseModules = mutableMapOf<Long, MutableList<ModuleObject>>()

    /** Map of course id to quiz list */
    val courseQuizzes = mutableMapOf<Long, MutableList<Quiz>>()

    /** Map of quiz id to question list */
    val quizQuestions = mutableMapOf<Long, MutableList<QuizQuestion>>()

    /** Map of quiz id to quiz submission list */
    val quizSubmissions = mutableMapOf<Long, MutableList<QuizSubmission>>()

    /** Maps of course id to recipient list */
    val studentRecipients = mutableMapOf<Long, List<Recipient>>()
    val teacherRecipients = mutableMapOf<Long, List<Recipient>>()
    val recipientGroups = mutableMapOf<Long, List<Recipient>>()

    /** One off conversation for handling creating new conversations, see ConversationEndpoint POST */
    var sentConversation: Conversation? = null

    /** Map of course id to list of conversation for inbox filters */
    val conversationCourseMap = mutableMapOf<Long, List<Conversation>>()

    /** Map of conversation id to conversation object */
    val conversations = mutableMapOf<Long, Conversation>()

    /** Map of quiz submission id to quiz submission questions */
    val quizSubmissionQuestions = mutableMapOf<Long, MutableList<QuizSubmissionQuestion>>()

    /** Map of doc session id to DocSession */
    val docSessions = mutableMapOf<String, DocSession>()

    /** Map of doc session id to Annotation lists */
    val annotations = mutableMapOf<String, List<CanvaDocAnnotation>>()

    /** One off conversation for handling creating new conversations, see ConversationEndpoint POST */
    var sentAnnotationComment: CanvaDocAnnotation? = null

    /** This is configured by addAnnotation and should not be modified directly */
    lateinit var canvadocRedirectUrl: String

    /** Map of user id to stream items */
    val streamItems = mutableMapOf<Long, MutableList<StreamItem>>()

    /** Map of user id to bookmarks */
    val bookmarks = mutableMapOf<Long, MutableList<Bookmark>>()

    /** Map of courseId to the courses latest announcement */
    val latestAnnouncements = mutableMapOf<Long, DiscussionTopicHeader>()

    val commentLibraryItems = mutableMapOf<Long, List<String>>()

    /** Map of userId to alerts */
    var observerAlerts = mutableMapOf<Long, List<Alert>>()
    val observerAlertThresholds = mutableMapOf<Long, MutableList<AlertThreshold>>()

    val pairingCodes = mutableMapOf<String, User>()

    var inboxSignature = ""
    var signatureEnabled = true

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

    /** Sets whether dashboard_cards returns true or false for isK5Subject field. */
    var elementarySubjectPages: Boolean = false

    /** Returns the current auth token for the specified user. Returns null if no such token exists. */
    fun tokenFor(user: User): String? {
        tokens.forEach { (token, userId) ->
            if (userId == user.id) return token
        }
        return null
    }

    //endregion

    // Webview support -- goes through a mock server
    val webViewServer = MockWebServer()
    // The dispatcher contains all of our matching/capture logic for webview requests
    val webViewServerDispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            Log.d("WebView", "dispatch() request: $request")
            var path = request.path
            if(path?.startsWith("//") == true) path = path.substring(1)

            // Test as to whether this is a /courses/{courseId}/files/{fileId}/preview request,
            // and if it is then service it.
            val courseFilePreviewRegex = """/courses/(\d+)/files/(\d+)/(preview|download)""".toRegex()
            courseFilePreviewRegex.matchEntire(path ?: "")?.run {
                // groupValues[0] is the entire string, I think
                Log.d("WebView", "Matched courseFilePreviewRegex: course=${groupValues[1]}, file=${groupValues[2]}")
                val fileId = groupValues[2].toLong()
                val contents = fileContents[fileId] ?: ""
                return@dispatch MockResponse().setResponseCode(200).setBody(contents)
            }

            // Test as to whether this is a /groups/{courseId}/files/{fileId}/preview request,
            // and if it is then service it.
            val courseGroupFilePreviewRegex = """/groups/(\d+)/files/(\d+)/(preview|download)""".toRegex()
            courseGroupFilePreviewRegex.matchEntire(path ?: "")?.run {
                // groupValues[0] is the entire string, I think
                Log.d("WebView", "Matched courseFilePreviewRegex: course=${groupValues[1]}, file=${groupValues[2]}")
                val fileId = groupValues[2].toLong()
                val contents = fileContents[fileId] ?: ""
                return@dispatch MockResponse().setResponseCode(200).setBody(contents)
            }

            // Handle some known constant-path requests
            when(path) {
                "/favicon.ico" -> {return MockResponse()} // defaults to status 200, empty response
            }

            // If you get to here, you will crash due to an unhandled request
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    var offlineModeEnabled = false

    var assignmentEnhancementsEnabled = true

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
    invitedCourseCount: Int = 0,
    pastCourseCount: Int = 0,
    favoriteCourseCount: Int = 0,
    studentCount: Int = 0,
    teacherCount: Int = 0,
    parentCount: Int = 0,
    accountNotificationCount: Int = 0,
    createSections: Boolean = false,
    publishCourses: Boolean = true,
    withGradingPeriods: Boolean = false,
    homeroomCourseCount: Int = 0
): MockCanvas {
    data = MockCanvas()

    // Add a default term
    data.addTerm("Default Term")

    // Add users
    val studentUsers = List(studentCount) { data.addUser() }
    val teacherUsers = List(teacherCount) { data.addUser() }
    val parentUsers = List(parentCount) { data.addUser() }

    // Add courseCount
    repeat(courseCount) {
        val courseId = data.newItemId()
        var section: Section? = null
        if (createSections) {
            val sectionId = data.newItemId()
            section = Section(
                    id = sectionId,
                    name = "Section " + sectionId,
                    courseId = courseId,
                    students = studentUsers,
                    totalStudents = studentUsers.count()
            )
        }
        data.addCourse(
                isFavorite = it < favoriteCourseCount,
                id = courseId,
                section = section,
                isPublic = publishCourses,
                withGradingPeriod = withGradingPeriods)
    }

    repeat(pastCourseCount) { data.addCourse(concluded = true) }

    repeat(homeroomCourseCount) {
        data.addCourse(isHomeroom = true)
    }

    // Add enrollments
    data.courses.values.forEachIndexed { index, course ->
        // Enroll teachers
        teacherUsers.forEach { data.addEnrollment(it, course, Enrollment.EnrollmentType.Teacher) }

        // Enroll students
        studentUsers.forEach {
            data.addEnrollment(
                    user = it,
                    course = course,
                    enrollmentState = if (index < invitedCourseCount) EnrollmentAPI.STATE_INVITED else EnrollmentAPI.STATE_ACTIVE,
                    type = Enrollment.EnrollmentType.Student,
                    courseSectionId = if(course.sections.count() > 0) course.sections.get(0).id else 0
            )
        }

        // Enroll parents
        parentUsers.forEach { parent ->
            studentUsers.forEach { student ->
                data.addEnrollment(parent, course, Enrollment.EnrollmentType.Observer, student)
            }
        }
    }

    data.updateUserEnrollments()

    repeat(accountNotificationCount) { data.addAccountNotification() }

    // Perform the finishing operational touches for our web server
    data.webViewServer.dispatcher = data.webViewServerDispatcher
    data.webViewServer.start()

    return data
}

fun MockCanvas.addStudent(courses: List<Course>): User {
    val user = addUser()
    courses.forEach { course ->
        addEnrollment(
            user = user,
            course = course,
            enrollmentState = EnrollmentAPI.STATE_ACTIVE,
            type = Enrollment.EnrollmentType.Student,
            courseSectionId = if (course.sections.isNotEmpty()) course.sections[0].id else 0
        )
    }
    return user
}

/** Create a bookmark associated with an assignment */
fun MockCanvas.addBookmark(user: User, assignment: Assignment, name: String) : Bookmark {
    val bookmark = Bookmark(
            id = newItemId(),
            name = name,
            url = "https://${domain}/api/v1/courses/${assignment.courseId}/assignments/${assignment.id}"
    )

    var list = bookmarks[user.id]
    if(list == null) {
        list = mutableListOf<Bookmark>()
        bookmarks[user.id] = list
    }

    list.add(bookmark)

    return bookmark
}

/**
 * Not ideal, but in order to create realistic users, we have to add enrollments to them...
 * Unfortunately, in order to create enrollments, we have to have users first, hence the
 * copy nonsense seen here.
 */
fun MockCanvas.updateUserEnrollments() {
    users.values.forEach { user ->
        val enrollmentList = mutableListOf<Enrollment>()

        enrollments.values.forEach { enrollment ->
            if(enrollment.userId == user.id) enrollmentList.add(enrollment)
        }

        val userCopy = user.copy(enrollments = enrollmentList)
        users[user.id] = userCopy
    }
}

fun MockCanvas.addCourseWithEnrollment(
    user: User,
    enrollmentType: Enrollment.EnrollmentType,
    score: Double? = 0.0,
    grade: String? = "",
    isHomeroom: Boolean = false,
    restrictQuantitativeData: Boolean = false
): Course {
    val course = addCourse(isHomeroom = isHomeroom, restrictQuantitativeData = restrictQuantitativeData)

    addEnrollment(
        user = user,
        course = course,
        type = enrollmentType,
        courseSectionId = if(course.sections.count() > 0) course.sections.get(0).id else 0,
        currentScore = score,
        currentGrade = grade
    )

    return course
}

/** Creates a new Course and adds it to MockCanvas */
fun MockCanvas.addCourse(
    isFavorite: Boolean = false,
    concluded: Boolean = false,
    id: Long = newItemId(),
    section: Section? = null,
    isPublic: Boolean = true,
    withGradingPeriod: Boolean = false,
    isHomeroom: Boolean = false,
    restrictQuantitativeData: Boolean = false
): Course {
    val randomCourseName = Randomizer.randomCourseName()
    val endAt = if (concluded) OffsetDateTime.now().minusWeeks(1).toApiString() else null

    val gradingPeriodList = if (withGradingPeriod) {
        val gradingPeriodId = this.newItemId()
        val gradingPeriod = GradingPeriod(gradingPeriodId, "Grading Period $gradingPeriodId")
        addGradingPeriod(id, gradingPeriod)
    } else {
        emptyList()
    }

    val course = Course(
        id = id,
        name = randomCourseName,
        originalName = randomCourseName,
        courseCode = randomCourseName.substring(0, 2),
        term = terms.values.first(),
        endAt = endAt,
        isFavorite = isFavorite,
        sections = if (section != null) listOf(section) else listOf<Section>(),
        isPublic = isPublic,
        homeroomCourse = isHomeroom,
        gradingPeriods = gradingPeriodList,
        courseColor = "#008EE2",
        restrictEnrollmentsToCourseDate = concluded,
        settings = CourseSettings(restrictQuantitativeData = restrictQuantitativeData)
    )
    courses += course.id to course

    // For now, give all courses tabs for assignments and quizzes
    val assignmentsTab = Tab(position = 0, label = "Assignments", visibility = "public", tabId = Tab.ASSIGNMENTS_ID)
    val quizzesTab = Tab(position = 1, label = "Quizzes", visibility = "public", tabId = Tab.QUIZZES_ID)
    courseTabs += course.id to mutableListOf(assignmentsTab, quizzesTab)

    return course
}

fun MockCanvas.addGradingPeriod(courseId : Long, gradingPeriod: GradingPeriod): List<GradingPeriod> {
    var gpList = courseGradingPeriods[courseId]
    if(gpList == null) {
        gpList = mutableListOf<GradingPeriod>()
        courseGradingPeriods[courseId] = gpList
    }
    gpList.add(gradingPeriod)
    return gpList
}

/** Adds the provided permissions to the course */
fun MockCanvas.addCoursePermissions(courseId: Long, permissions: CanvasContextPermission) {
    coursePermissions[courseId] = permissions

    // Let's go ahead and attach these permissions to the course in question
    val course = courses[courseId]
    course?.permissions = permissions
}

/** Adds the provided settings to the course specified course */
fun MockCanvas.addCourseSettings(courseId: Long, settings: CourseSettings) {
    courseSettings[courseId] = settings
}

fun MockCanvas.addUserPermissions(userId: Long, canUpdateName: Boolean, canUpdateAvatar: Boolean) {
    val user = users[userId]
    user?.permissions = CanvasContextPermission(canUpdateAvatar = canUpdateAvatar, canUpdateName = canUpdateName)
}

fun MockCanvas.addCourseCalendarEvent(
    course: Course,
    startDate: String,
    title: String,
    description: String,
    isImportantDate: Boolean = false,
    endDate: String? = null,
    rrule: String? = null,
    location: String? = null,
    address: String? = null
): ScheduleItem {
    val newScheduleItem = ScheduleItem(
        itemId = newItemId().toString(),
        title = title,
        description = description,
        itemType = ScheduleItem.Type.TYPE_CALENDAR,
        isAllDay = true,
        allDayAt = if (endDate != null) null else startDate,
        startAt = startDate,
        endAt = endDate ?: startDate,
        contextCode = "course_${course.id}",
        contextName = course.name,
        importantDates = isImportantDate,
        rrule = rrule,
        seriesNaturalLanguage = rrule,
        locationName = location,
        locationAddress = address,
        workflowState = "active"
    )

    var calendarEventList = courseCalendarEvents[course.id]
    if (calendarEventList == null) {
        calendarEventList = mutableListOf()
        courseCalendarEvents[course.id] = calendarEventList
    }
    calendarEventList.add(newScheduleItem)

    return newScheduleItem
}

fun MockCanvas.addUserCalendarEvent(
    userId: Long,
    date: String,
    title: String,
    description: String,
    isImportantDate: Boolean = false,
    rrule: String? = null,
    location: String? = null,
    address: String? = null
): ScheduleItem {
    val newScheduleItem = ScheduleItem(
        itemId = newItemId().toString(),
        title = title,
        description = description,
        itemType = ScheduleItem.Type.TYPE_CALENDAR,
        isAllDay = true,
        allDayAt = date,
        startAt = date,
        endAt = date,
        contextCode = "user_$userId",
        contextName = "User $userId",
        importantDates = isImportantDate,
        rrule = rrule,
        seriesNaturalLanguage = rrule,
        locationName = location,
        locationAddress = address,
        workflowState = "active"
    )

    var eventList = userCalendarEvents[userId]
    if (eventList == null) {
        eventList = mutableListOf()
        userCalendarEvents[userId] = eventList
    }
    eventList.add(newScheduleItem)

    return newScheduleItem
}

fun MockCanvas.addAssignmentCalendarEvent(courseId: Long, date: String, title: String, description: String, isImportantDate: Boolean = false, assignment: Assignment): ScheduleItem {
    val newScheduleItem = ScheduleItem(
            itemId = newItemId().toString(),
            title = title,
            description = description,
            itemType = ScheduleItem.Type.TYPE_ASSIGNMENT,
            isAllDay = true,
            allDayAt = date,
            startAt = date,
            contextCode = "course_$courseId",
            importantDates = isImportantDate,
            assignment = assignment
    )

    var calendarEventList = courseCalendarEvents[courseId]
    if(calendarEventList == null) {
        calendarEventList = mutableListOf<ScheduleItem>()
        courseCalendarEvents[courseId] = calendarEventList
    }
    calendarEventList.add(newScheduleItem)

    return newScheduleItem
}

/**
 * Adds the provided list of users (converted to basic users) to the provided course as recipients.
 * This results in the role specific recipient maps being populated as well as the recipient groups.
 */
fun MockCanvas.addRecipientsToCourse(course: Course, students: List<User>, teachers: List<User>) {
    studentRecipients[course.id] = students.map {
        Recipient(
                stringId = it.id.toString(),
                name = it.shortName,
                avatarURL = it.avatarUrl,
                commonCourses = hashMapOf(Pair(course.id.toString(), arrayOf(EnrollmentType.STUDENTENROLLMENT.rawValue())))
        )
    }

    teacherRecipients[course.id] = teachers.map {
        Recipient(
                stringId = it.id.toString(),
                name = it.shortName,
                avatarURL = it.avatarUrl,
                commonCourses = hashMapOf(Pair(course.id.toString(), arrayOf(EnrollmentType.TEACHERENROLLMENT.rawValue())))
        )
    }

    recipientGroups[course.id] = listOf(
        Recipient(
            avatarURL = Randomizer.randomAvatarUrl(),
            stringId = "${course.contextId}_teachers",
            name = "Teachers",
            userCount = teacherRecipients.values.size
        ),
        Recipient(
            avatarURL = Randomizer.randomAvatarUrl(),
            stringId = "${course.contextId}_students",
            name = "Students",
            userCount = studentRecipients.values.size
        )
    )
}

/**
 * Creates a conversation with a single message using the userId provided as a non-author participant and creating a
 * new BasicUser as the author.
 *
 * [isUserAuthor] -> Will set the user as the author, and create a new BasicUser as the other participant
 */
fun MockCanvas.createBasicConversation(
        userId: Long,
        subject: String = Randomizer.randomConversationSubject(),
        isUserAuthor: Boolean = false,
        isStarred: Boolean = false,
        workflowState: Conversation.WorkflowState = Conversation.WorkflowState.UNREAD,
        contextCode: String? = null,
        contextName: String? = null,
        messageBody: String = Randomizer.randomConversationBody()
): Conversation {
    val basicUser = BasicUser(
            id = if(isUserAuthor) newItemId() else userId,
            name = Randomizer.randomName().fullName,
            avatarUrl = Randomizer.randomAvatarUrl()
    )

    val basicAuthorUser = BasicUser(
            id = if(isUserAuthor) userId else newItemId(),
            name = Randomizer.randomName().fullName,
            avatarUrl = Randomizer.randomAvatarUrl()
    )

    val basicMessage = Message(
            id = newItemId(),
            createdAt = APIHelper.dateToString(GregorianCalendar()),
            body = messageBody,
            authorId = basicAuthorUser.id,
            participatingUserIds = listOf(basicUser.id, basicAuthorUser.id)
    )

    return Conversation(
        id = newItemId(),
        subject = subject,
        workflowState = workflowState,
        lastMessage = Randomizer.randomConversationBody(),
        lastAuthoredMessageAt = APIHelper.dateToString(GregorianCalendar()),
        messageCount = 1,
        messages = listOf(basicMessage),
        avatarUrl = Randomizer.randomAvatarUrl(),
        isStarred = isStarred,
        contextName = contextName,
        contextCode = contextCode,
        participants = mutableListOf(basicUser, basicAuthorUser)
    )
}

/**
 * This function adds a correctly constructed "sent conversation" with the included userId as the author of the
 * conversation. It will be added to the MockCanvas sentConversation field to be used along with a POST request to
 * create a conversation.
 *
 */
fun MockCanvas.addSentConversation(subject: String, userId: Long, messageBody : String = Randomizer.randomConversationBody()){
    sentConversation = createBasicConversation(userId = userId, subject = subject, isUserAuthor = true, messageBody = messageBody)
}

/**
 *  Adds [conversationCount] of each conversation type (sent/archived/starred/unread) to the MockCanvas conversations map.
 *
 *  Each conversation will be properly configured to match to those filters, however the sent conversation will not be
 *  placed into the sentConversation field. Use addSentConversation for conversation POSTs to create new conversations.
 *
 *  [userId] -> The user you are performing these requests with. Will be used as the author for sent, and a participant
 *  for all other conversations.
 *  */

fun MockCanvas.addConversations(conversationCount: Int = 1, userId: Long, messageBody : String = Randomizer.randomConversationBody(), contextName: String? = null, contextCode: String? = null) {
    for (i in 0 until conversationCount) {
        val sentConversation = createBasicConversation(userId = userId, isUserAuthor = true, messageBody = messageBody, contextCode = contextCode, contextName = contextName)
        val archivedConversation = createBasicConversation(userId, workflowState = Conversation.WorkflowState.ARCHIVED, messageBody = messageBody, contextCode = contextCode, contextName = contextName)
        val starredConversation = createBasicConversation(userId, isStarred = true, messageBody = messageBody, contextCode = contextCode, contextName = contextName)
        val unreadConversation = createBasicConversation(userId, workflowState = Conversation.WorkflowState.UNREAD, messageBody = messageBody, contextCode = contextCode, contextName = contextName)
        conversations[sentConversation.id] = sentConversation
        conversations[archivedConversation.id] = archivedConversation
        conversations[starredConversation.id] = starredConversation
        conversations[unreadConversation.id] = unreadConversation
    }
}

/**
 * Adds a single conversation, with sender [senderId] and receivers [receiverIds].  It will not
 * be associated with any course.
 */
fun MockCanvas.addConversationWithMultipleMessages(
    senderId: Long,
    receiverIds: List<Long>,
    messageCount: Int = 1,
) : Conversation {
    val messageSubject = Randomizer.randomConversationSubject()
    val sender = this.users[senderId]!!
    val senderBasic = BasicUser(
        id = sender.id,
        name = sender.shortName,
        pronouns = sender.pronouns,
        avatarUrl = sender.avatarUrl
    )

    val participants = mutableListOf(senderBasic)
    receiverIds.forEach {id ->
        val receiver = this.users[id]!!
        participants.add(
            BasicUser(
                id = receiver.id,
                name = receiver.shortName,
                pronouns = receiver.pronouns,
                avatarUrl = receiver.avatarUrl
            )
        )
    }

    val basicMessages = MutableList(messageCount) {
        Message(
            id = newItemId(),
            createdAt = APIHelper.dateToString(GregorianCalendar()),
            body = Randomizer.randomConversationBody(),
            authorId = sender.id,
            participatingUserIds = receiverIds.toMutableList().plus(senderId)
        )
    }

    val result = Conversation(
        id = newItemId(),
        subject = messageSubject,
        workflowState = Conversation.WorkflowState.UNREAD,
        lastMessage = basicMessages.last().body,
        lastAuthoredMessageAt = APIHelper.dateToString(GregorianCalendar()),
        messageCount = basicMessages.size,
        messages = basicMessages,
        avatarUrl = Randomizer.randomAvatarUrl(),
        participants = participants,
        audience = null // Prevents "Monologue"
    )

    this.conversations[result.id] = result

    return result
}

/**
 * Adds a single conversation, with sender [senderId] and receivers [receiverIds].  It will not
 * be associated with any course.
 */
fun MockCanvas.addConversation(
        senderId: Long,
        receiverIds: List<Long>,
        messageBody : String = Randomizer.randomConversationBody(),
        messageSubject : String = Randomizer.randomConversationSubject(),
        cannotReply: Boolean = false) : Conversation {

    val sender = this.users[senderId]!!
    val senderBasic = BasicUser(
            id = sender.id,
            name = sender.shortName,
            pronouns = sender.pronouns,
            avatarUrl = sender.avatarUrl
    )

    val participants = mutableListOf(senderBasic)
    receiverIds.forEach {id ->
        val receiver = this.users[id]!!
        participants.add(
                BasicUser(
                        id = receiver.id,
                        name = receiver.shortName,
                        pronouns = receiver.pronouns,
                        avatarUrl = receiver.avatarUrl
                )
        )
    }

    val basicMessage = Message(
            id = newItemId(),
            createdAt = APIHelper.dateToString(GregorianCalendar()),
            body = messageBody,
            authorId = sender.id,
            participatingUserIds = receiverIds.toMutableList().plus(senderId)
    )

    val result = Conversation(
            id = newItemId(),
            subject = messageSubject,
            workflowState = Conversation.WorkflowState.UNREAD,
            lastMessage = messageBody,
            lastAuthoredMessageAt = APIHelper.dateToString(GregorianCalendar()),
            messageCount = 1,
            messages = listOf(basicMessage),
            avatarUrl = Randomizer.randomAvatarUrl(),
            participants = participants,
            audience = null, // Prevents "Monologue"
            cannotReply = cannotReply
    )

    this.conversations[result.id] = result

    return result
}

/**
 *  Adds [conversationCount] of conversations to the MockCanvas conversationCourseMap.
 *
 *  Currently all of these messages are default conversations with the appropriate user name and context codes/names.
 *  */
fun MockCanvas.addConversationsToCourseMap(
        userId: Long,
        courseList: List<Course>,
        conversationCount: Int = 1,
        messageBody : String = Randomizer.randomConversationBody()
) {
    courseList.forEach {
        val conversations= mutableListOf<Conversation>()
        for(i in 0 until conversationCount) {
            conversations.add(createBasicConversation(userId = userId, contextCode = it.contextId, contextName = it.name, messageBody = messageBody))
        }
        conversationCourseMap[it.id] = conversations
    }
}


/**
 * Creates assignments for the standard groups (overdue, upcoming, undated, and past) for a course
 * and adds it to MockCanvas
 *
 */
fun MockCanvas.addAssignmentsToGroups(course: Course, assignmentCountPerGroup: Int = 1): List<AssignmentGroup> {
    val futureDueDate = OffsetDateTime.now().plusWeeks(1).toApiString()
    val pastDueDate = OffsetDateTime.now().minusWeeks(1).toApiString()

    // Set up our assignment groups, then add to them dynamically via addAssignment()
    val overdueAssignmentGroup = AssignmentGroup(id = 1, name = "overdue")
    val upcomingAssignmentGroup = AssignmentGroup(id = 2, name = "upcoming")
    val undatedAssignmentGroup = AssignmentGroup(id = 3, name = "undated")
    val pastAssignmentGroup = AssignmentGroup(id = 4, name = "past")
    assignmentGroups[course.id] = mutableListOf(overdueAssignmentGroup,upcomingAssignmentGroup,undatedAssignmentGroup,pastAssignmentGroup)

    for (i in 0 until assignmentCountPerGroup) {

        addAssignment(
                courseId = course.id,
                submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_URL),
                name = Randomizer.randomAssignmentName(),
                dueAt = pastDueDate,
                assignmentGroupId = overdueAssignmentGroup.id
        )
        addAssignment(
                courseId = course.id,
                submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_URL),
                name = Randomizer.randomAssignmentName(),
                dueAt = futureDueDate,
                assignmentGroupId = upcomingAssignmentGroup.id
        )

        addAssignment(
                courseId = course.id,
                submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_URL),
                name = Randomizer.randomAssignmentName(),
                dueAt = null,
                assignmentGroupId = undatedAssignmentGroup.id
        )

        val pastAssignment = addAssignment(
                courseId = course.id,
                submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_URL),
                name = Randomizer.randomAssignmentName(),
                dueAt = pastDueDate,
                assignmentGroupId = pastAssignmentGroup.id
        )
        addSubmissionForAssignment(
                assignmentId = pastAssignment.id,
                userId = users.values.first().id,
                type = Assignment.SubmissionType.ONLINE_URL.apiString,
                url = "https://google.com"
        )
    }

    return assignmentGroups[course.id]!!.toList()
}

/**
 * Adds a single assignment to the course assignment group map. This function does not currently take into
 * account existing assignments. Use either addAssignment or addAssignmentsToGroups.
 */
fun MockCanvas.addAssignment(
    courseId: Long,
    submissionTypeList: List<Assignment.SubmissionType> = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY),
    assignmentGroupId: Long = newItemId(),
    isQuizzesNext: Boolean = false,
    lockInfo : LockInfo? = null,
    userSubmitted: Boolean = false,
    dueAt: String? = null,
    name: String = Randomizer.randomAssignmentName(),
    pointsPossible: Int = 10,
    description: String = "",
    lockAt: String? = null,
    unlockAt: String? = null,
    withDescription: Boolean = false,
    gradingType: String = "percent",
    discussionTopicHeader: DiscussionTopicHeader? = null,
    htmlUrl: String? = ""
) : Assignment {
    val assignmentId = newItemId()
    val submissionTypeListRawStrings = submissionTypeList.map { it.apiString }
    var assignment = Assignment(
            id = assignmentId,
            assignmentGroupId = assignmentGroupId,
            courseId = courseId,
            name = name,
            submissionTypesRaw = submissionTypeListRawStrings,
            lockInfo = lockInfo,
            lockedForUser = lockInfo != null,
            userSubmitted = userSubmitted,
            dueAt = dueAt,
            pointsPossible = pointsPossible.toDouble(),
            description = description,
            lockAt = lockAt,
            unlockAt = unlockAt,
            published = true,
            allDates = listOf(AssignmentDueDate(id = newItemId(), dueAt = dueAt, lockAt = lockAt, unlockAt = unlockAt)),
            gradingType = gradingType,
            discussionTopicHeader = discussionTopicHeader,
            htmlUrl = htmlUrl
    )

    if (isQuizzesNext) {
        assignment = assignment.copy(
                url = "https://mobiledev.instructure.com/api/v1/courses/1567973/external_tools/sessionless_launch?assignment_id=24378681&launch_type=assessment"
        )
    }

    // Figure out which assignment group in which to track the assignment
    var assignmentGroupList = assignmentGroups[courseId]
    if (assignmentGroupList == null) {
        assignmentGroupList = mutableListOf<AssignmentGroup>()
        assignmentGroups[courseId] = assignmentGroupList
    }

    val group = assignmentGroupList.find { it.id == assignmentGroupId }
    if (group == null) {
        assignmentGroupList.add(AssignmentGroup(id = assignmentGroupId, assignments = listOf(assignment)))
    } else {
        val newList = group.assignments.toMutableList()
        newList.add(assignment)
        val newGroup = group.copy(
                assignments = newList
        )
        assignmentGroupList.remove(group)
        assignmentGroupList.add(newGroup)
    }

    // Track assignment.id -> assignment
    assignments[assignment.id] = assignment

    // return the new assignment
    return assignment
}

fun MockCanvas.addDiscussionTopicToAssignment(
    assignment: Assignment,
    discussionTopicHeader: DiscussionTopicHeader
) {
    val assignmentWithDiscussion = assignment.copy(
        discussionTopicHeader = discussionTopicHeader
    )

    assignments[assignment.id] = assignmentWithDiscussion
}

/**
 * Adds multiple (type) submissions to the assignment submission map.
 */
fun MockCanvas.addSubmissionsForAssignment(
    assignmentId: Long,
    userId: Long,
    types: List<String>,
    body: String? = null,
    url: String? = null,
    attachment: Attachment? = null,
    comment: SubmissionComment? = null,
    state: String = "submitted",
    grade: String? = null,
    attempt: Long = 1,
    score: Double? = null,
    excused: Boolean = false): MutableList<Submission> {

    val submissionList = mutableListOf<Submission>()
    types.forEach { type ->
        val submission = addSubmissionForAssignment(
            assignmentId,
            userId,
            type,
            body,
            url,
            attachment,
            comment,
            state,
            grade,
            attempt,
            score,
            excused
        )
        submissionList.add(submission)
    }

    return submissionList

}
/**
 * Adds a submission to the assignment submission map.
 */
fun MockCanvas.addSubmissionForAssignment(
        assignmentId: Long,
        userId: Long,
        type: String,
        body: String? = null,
        url: String? = null,
        attachment: Attachment? = null,
        comment: SubmissionComment? = null,
        state: String = "submitted",
        grade: String? = null,
        attempt: Long = 1,
        score: Double? = null,
        excused: Boolean = false
) : Submission {
    val assignment = assignments[assignmentId]!!
    val assignmentDueDate = assignment.dueAt?.toDate()
    val isLate = (assignmentDueDate != null) && assignmentDueDate.before(Calendar.getInstance().time)

    // Create the new submission object
    val submission = Submission(
            id = newItemId(),
            submittedAt = Date(),
            attempt = attempt,
            body = body,
            url = url,
            previewUrl = url,
            workflowState = state,
            submissionType = type,
            assignmentId = assignmentId,
            userId = userId,
            late = isLate,
            attachments = if(attachment != null) arrayListOf(attachment) else arrayListOf(),
            submissionComments = if(comment != null) listOf(comment) else listOf(),
            mediaContentType = attachment?.contentType,
            grade = grade,
            score = score ?: 0.0,
            postedAt = Date(),
            excused = excused,
            enteredScore = score ?: 0.0,
    )

    // Get the submission list for the assignment, creating it if necessary
    var submissionList = submissions[assignmentId]
    if(submissionList == null) {
        submissionList = mutableListOf()
        submissions[assignmentId] = submissionList
    }

    // Get the user's root submission for the assignment, creating it if necessary
    var userRootSubmission = submissionList.firstOrNull {sub -> sub.userId == userId}
    if(userRootSubmission == null) {
        userRootSubmission = Submission(
                id = newItemId(), // Most of these are probably unnecessary in the root submission object
                submittedAt = Date(),
                attempt = 1,
                body = body,
                url = url,
                previewUrl = url,
                workflowState = state,
                submissionType = type,
                assignmentId = assignmentId,
                userId = userId,
                late = isLate,
                attachments = if(attachment != null) arrayListOf(attachment) else arrayListOf(),
                submissionComments = if(comment != null) listOf(comment) else listOf(),
                mediaContentType = attachment?.contentType,
                grade = grade,
                score = score ?: 0.0,
                postedAt = Date(),
                excused = excused,
                enteredScore = score ?: 0.0,
        )
        submissionList.add(userRootSubmission)
    }
    else if(grade != null && grade != userRootSubmission.grade)
    {
        // We need to replace the grade in the root submission.  SPECULATIVE, NOT REALLY TESTED.
        val newRootSubmission = userRootSubmission.copy(
                grade = grade
        )
        submissionList.remove(userRootSubmission)
        submissionList.add(newRootSubmission)
        userRootSubmission = newRootSubmission
    }


    assignment.submission = userRootSubmission // May have already been set

    // Now add the new submission to the user's root submission's history
    (userRootSubmission.submissionHistory as MutableList<Submission?>).add(submission)
    return userRootSubmission
}

fun MockCanvas.addLTITool(name: String, url: String, course: Course, id: Long = newItemId()): LTITool {
    val ltiTool = LTITool(id = id, name = name, url = url, contextId = course.id, contextName = course.name)

    this.ltiTools.add(ltiTool)

    return ltiTool
}

enum class AssignmentGroupType {
    OVERDUE, UPCOMING, UNDATED, PAST
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
    observedUser: User? = null,
    courseSectionId: Long = 0,
    currentScore: Double? = 88.1,
    currentGrade: String? = "B+",
    enrollmentState: String = EnrollmentAPI.STATE_ACTIVE
): Enrollment {
    val enrollment = Enrollment(
        id = enrollments.size + 1L,
        role = type,
        type = type,
        courseId = course.id,
        enrollmentState = enrollmentState,
        userId = user.id,
        observedUser = observedUser,
        grades = Grades(currentScore = currentScore, currentGrade = currentGrade),
        courseSectionId = courseSectionId,
        user = user,
        computedCurrentScore = currentScore,
        computedCurrentGrade = currentGrade
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
            id = Random.nextLong(),
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

    // Let's add an empty root file folder for the user
    // Now create our folder metadata
    val newFolderId = newItemId()
    val folderMetadataItem = FileFolder(
            contextType = "user",
            contextId = user.id,
            filesUrl = "https://mock-data.instructure.com/api/v1/folders/$newFolderId/files",
            foldersUrl = "https://mock-data.instructure.com/api/v1/folders/$newFolderId/folders",
            id = newFolderId,
            //folderId = rootFolder.id,
            displayName = "Files",
            url = "https://mock-data.instructure.com/api/v1/folders/$newFolderId",
            name = "Files",
            fullName = "Files"
    )
    fileFolders[newFolderId] = folderMetadataItem
    return user
}

/** Creates a new page for a given course, and adds it to MockCanvas.
 * If [groupId] is non-null, then the page will be added to the group
 * page list, as opposed to the course page list.
 */
fun MockCanvas.addPageToCourse(
        courseId: Long,
        pageId: Long = 0,
        url: String? = null,
        title: String = Randomizer.randomPageTitle(),
        body: String = Randomizer.randomPageBody(),
        published: Boolean = false,
        groupId: Long? = null
): Page {

    val page = Page(
            id = pageId,
            url = url,
            title = title,
            body = body,
            published = published
    )

    var list : MutableList<Page>? = null
    if(groupId != null) {
        list = groupPages[groupId]
        if(list == null) {
            list = mutableListOf<Page>()
            groupPages[groupId] = list
        }
    }
    else {
        list = coursePages[courseId]
        if (list == null) {
            list = mutableListOf<Page>()
            coursePages.put(courseId, list)
        }
    }
    list.add(page)

    return page
}

/**
 * Utility method to obtain a root folder, possibly creating it if it doesn't exist.
 *
 * If [folderId] is non-null, grabs the folder associated with that id
 * If [groupId] is non-null, grabs the root folder associated with that group, creating it if necessary
 * Otherwise, grabs the root folder for [courseId], creating it if necessary
 */
private fun MockCanvas.getRootFolder(courseId: Long? = null, groupId: Long? = null, folderId: Long? = null) : FileFolder {
    var rootFolder: FileFolder? = null

    if(folderId != null) {
        rootFolder = fileFolders[folderId]
    }
    else if(groupId != null) {
        if(courseId == null) {
            throw Exception("Must provide non-null courseId with non-null groupId")
        }
        rootFolder = groupRootFolders[groupId]
        if(rootFolder == null) {
            val folderId = newItemId()
            rootFolder = FileFolder(
                    id = folderId,
                    contextType = "Course",
                    contextId = courseId,
                    name = "course group files",
                    fullName = "course group files",
                    filesUrl = "https://mock-data.instructure.com/api/v1/folders/$folderId/files",
                    foldersUrl = "https://mock-data.instructure.com/api/v1/folders/$folderId/folders"
            )
            groupRootFolders[groupId] = rootFolder
            fileFolders[rootFolder.id] = rootFolder
        }

    }
    else if(courseId != null) {
        rootFolder = courseRootFolders[courseId]
        if (rootFolder == null) {
            val folderId = newItemId()
            rootFolder = FileFolder(
                    id = folderId,
                    contextType = "Course",
                    contextId = courseId,
                    name = "course files",
                    fullName = "course files",
                    filesUrl = "https://mock-data.instructure.com/api/v1/folders/$folderId/files",
                    foldersUrl = "https://mock-data.instructure.com/api/v1/folders/$folderId/folders"
            )
            courseRootFolders[courseId] = rootFolder
            fileFolders[rootFolder.id] = rootFolder
        }
    }
    else {
        throw Exception("At least one of folderId, groupId or courseId must be non-null")
    }

    return rootFolder!!
}

/**
 * Create a new folder for the specified course.
 * If [groupId] is non-null, the folder will be associated with that group.
 * Otherwise, the folder will be associated with course [courseId].
 **/
fun MockCanvas.addFolderToCourse(
        courseId: Long,
        displayName: String,
        groupId: Long? = null
) : Long {
    // Find your root folder
    val rootFolder = getRootFolder(courseId = courseId, groupId = groupId)

    // Now create our folder metadata
    val newFolderId = newItemId()
    val folderMetadataItem = FileFolder(
            contextType = if(groupId == null) "course" else "group",
            contextId = if(groupId == null) courseId else groupId,
            filesUrl = "https://mock-data.instructure.com/api/v1/folders/$newFolderId/files",
            foldersUrl = "https://mock-data.instructure.com/api/v1/folders/$newFolderId/folders",
            id = newFolderId,
            folderId = rootFolder.id,
            displayName = displayName,
            url = "https://mock-data.instructure.com/api/v1/folders/$newFolderId",
            name = displayName,
            fullName = displayName
    )

    // And record it in the root folder
    var fileList = folderFiles[rootFolder.id]
    if (fileList == null) {
        fileList = mutableListOf<FileFolder>()
        folderFiles[rootFolder.id] = fileList
    }
    //  TODO: Update rootFolder.filesCount
    fileList.add(folderMetadataItem)
    fileFolders[newFolderId] = folderMetadataItem

    return newFolderId
}

/**
 * Add a file to a folder.
 * If [folderId] is non-null, the file will be added to the folder associated with [folderId].
 * Otherwise, it will be added to the root folder associated with course [courseId].
 *
 * [url] -> Custom urls to be configured to bypass file download, be sure to cache the file first, see the
 * PdfInteractionTest for an example.
 */
fun MockCanvas.addFileToFolder(
        courseId: Long? = null,
        folderId: Long? = null,
        displayName: String,
        fileContent: String = Randomizer.randomPageBody(),
        contentType: String = "text/plain",
        url: String = "",
        fileId: Long = newItemId(),
        visibilityLevel: String = "inherit"
) : Long {
    if(courseId == null && folderId == null) {
        throw Exception("Either courseId or folderId must be non-null")
    }
    val rootFolder = getRootFolder(courseId = courseId, folderId = folderId)

    // Now create our file metadata
    val fileMetadataItem = FileFolder(
            id = fileId,
            folderId = rootFolder.id,
            size = fileContent.length.toLong(),
            displayName = displayName,
            contentType = contentType,
            url = if(url.isEmpty()) "https://mock-data.instructure.com/files/$fileId/preview" else url,
            visibilityLevel = visibilityLevel
    )

    // And record it for the folder
    var fileList = folderFiles[rootFolder.id]
    if (fileList == null) {
        fileList = mutableListOf<FileFolder>()
        folderFiles[rootFolder.id] = fileList
    }
    //  TODO: Update courseRootFolder.filesCount
    fileList.add(fileMetadataItem)

    // Now record our file contents (just text for now)
    fileContents[fileId] = fileContent

    return fileId
}
/**
 * Creates a new file for the specified course, and records it properly in MockCanvas.
 * If [groupId] is non-null, it will be added as a group course file.  Otherwise,
 * is till be added as a file associated with course [courseId].
 */
fun MockCanvas.addFileToCourse(
        courseId: Long,
        displayName: String = Randomizer.randomPageTitle(),
        fileContent: String = Randomizer.randomPageBody(),
        contentType: String = "text/plain",
        groupId: Long? = null,
        url: String = "",
        fileId: Long = newItemId(),
        visibilityLevel: String = "inherit"
): Long {
    val rootFolder = getRootFolder(courseId = courseId, groupId = groupId)
    return addFileToFolder(
            folderId = rootFolder.id,
            displayName = displayName,
            fileContent = fileContent,
            contentType = contentType,
            url = url,
            fileId = fileId,
            visibilityLevel = visibilityLevel
    )
}

/** Creates a new discussion topic header and adds it to a specified course.
 *
 *  Since this might be created via the endpoint handler, we'll allow for the
 *  passing in of a pre-created and partially populated DiscussionTopicHeader
 *  in [prePopulatedTopicHeader]
 */
fun MockCanvas.addDiscussionTopicToCourse(
        course: Course,
        user: User,
        prePopulatedTopicHeader: DiscussionTopicHeader? = null,
        topicTitle: String = Randomizer.randomConversationSubject(),
        topicDescription: String = Randomizer.randomPageTitle(),
        allowRating: Boolean = true,
        onlyGradersCanRate: Boolean = false,
        allowReplies: Boolean = true,
        allowAttachments: Boolean = true,
        attachment: RemoteFile? = null,
        isAnnouncement: Boolean = false,
        sections: List<Section> = listOf(),
        groupId: Long? = null,
        assignment: Assignment? = null
) : DiscussionTopicHeader {

    var topicHeader = prePopulatedTopicHeader
    if(topicHeader == null) {
        topicHeader = DiscussionTopicHeader(
            title = topicTitle,
            discussionType = "side_comment",
            message = topicDescription
        )
    }

    topicHeader.author = DiscussionParticipant(id = user.id, displayName = user.name)
    topicHeader.published = true
    topicHeader.allowRating = allowRating
    topicHeader.onlyGradersCanRate = onlyGradersCanRate
    topicHeader.permissions = DiscussionTopicPermission(attach = allowAttachments, reply = allowReplies)
    topicHeader.id = newItemId()
    topicHeader.postedDate = Calendar.getInstance().time
    if(attachment != null) {
        topicHeader.attachments = mutableListOf(attachment)
    }
    topicHeader.announcement = isAnnouncement
    topicHeader.sections = sections
    topicHeader.assignment = assignment
    topicHeader.assignmentId = assignment?.id ?: 0L

    var topicHeaderList = if(groupId != null) groupDiscussionTopicHeaders[groupId] else courseDiscussionTopicHeaders[course.id]
    if(topicHeaderList == null) {
        topicHeaderList = mutableListOf()
        if(groupId != null) {
            groupDiscussionTopicHeaders[groupId] = topicHeaderList
        }
        else {
            courseDiscussionTopicHeaders[course.id] = topicHeaderList
        }
    }

    topicHeaderList.add(topicHeader)

    val topic = DiscussionTopic(
        participants = mutableListOf(
            DiscussionParticipant(id = user.id, displayName = user.name)
        )
    )
    discussionTopics[topicHeader.id] = topic

    return topicHeader
}

/** Adds a reply to a discussion. */
fun MockCanvas.addReplyToDiscussion(
        topicHeader: DiscussionTopicHeader,
        user: User,
        replyMessage: String = Faker.instance().chuckNorris().fact(),
        attachment: RemoteFile? = null,
        ratingSum: Int = 0
) : DiscussionEntry {
    val topic = discussionTopics[topicHeader.id]
    val entry = DiscussionEntry(
            id = newItemId(),
            message = replyMessage,
            unread = true,
            author = DiscussionParticipant(
                    id = user.id,
                    displayName = user.name
            ),
            createdAt = Calendar.getInstance().time.toString(),
            ratingSum = ratingSum
    )
    if(attachment != null) {
        entry.attachments = mutableListOf(attachment)
    }
    topic!!.views.add(entry)
    topic.unreadEntries.add(entry.id)
    topicHeader.unreadCount += 1
    return entry
}

/** Adds a module to a course, initially unpopulated. */
fun MockCanvas.addModuleToCourse(
        course: Course,
        moduleName: String,
        sequential: Boolean = false,
        published: Boolean = true,
        unlockAt: String? = null,
        prerequisiteIds: LongArray? = null,
        state: String? = null
) : ModuleObject {

    val newModulePosition = courseModules[course.id]?.count() ?: 0
    // Create a new module
    val result = ModuleObject(
            id = newItemId(),
            position = newModulePosition,
            name = moduleName,
            sequentialProgress = sequential,
            published = published,
            unlockAt = unlockAt,
            prerequisiteIds = prerequisiteIds,
            state = state
    )

    // Record in modules for course
    var courseModuleList = courseModules[course.id]
    if(courseModuleList == null) {
        courseModuleList = mutableListOf<ModuleObject>()
        courseModules[course.id] = courseModuleList
    }
    courseModuleList.add(result)

    return result
}

/** Adds an item to a module */
fun MockCanvas.addItemToModule(
        course: Course,
        moduleId: Long,
        item: Any,
        contentId: Long = 0,
        published: Boolean = true,
        moduleContentDetails: ModuleContentDetails? = null,
        unpublishable: Boolean = true
) : ModuleItem {

    // Placeholders for itemType and itemTitle values that we will compute below
    var itemType: ModuleItem.Type? = null
    var itemTitle: String? = null
    var itemUrl: String? = null
    when(item) {
        is Assignment -> {
            itemType = ModuleItem.Type.Assignment
            itemTitle = item.name
            itemUrl = "https://mock-data.instructure.com/api/v1/courses/${course.id}/assignments/${item.id}"
        }
        is DiscussionTopicHeader -> {
            itemType = ModuleItem.Type.Discussion
            itemTitle = item.title
            itemUrl = "https://mock-data.instructure.com/api/v1/courses/${course.id}/discussion_topics/${item.id}"
        }
        is Quiz -> {
            itemType = ModuleItem.Type.Quiz
            itemTitle = item.title
            itemUrl = "https://mock-data.instructure.com/api/v1/courses/${course.id}/quizzes/${item.id}"
        }
        is FileFolder -> {
            itemType = ModuleItem.Type.File
            itemTitle = item.displayName
            itemUrl = "https://mock-data.instructure.com/api/v1/courses/${course.id}/files/${item.id}"
        }
        is Page -> {
            itemType = ModuleItem.Type.Page
            itemTitle = item.title
            itemUrl = "https://mock-data.instructure.com/api/v1/courses/${course.id}/pages/${item.id}"
        }
        is String -> {
            itemType = ModuleItem.Type.ExternalUrl
            itemTitle = item
            itemUrl = item
        }
        is LTITool -> {
            itemType = ModuleItem.Type.ExternalTool
            itemTitle = item.name
            itemUrl = item.url
        }
        else -> {
            throw Exception("Unknown item type: ${item::class.java.simpleName}")
        }
    }

    // Retrieve the current incarnation of our module from the module id
    // (Modules get altered and replaced by this operation.)
    val module = courseModules[course.id]?.find {it.id == moduleId}!!

    val result = ModuleItem(
            id = newItemId(),
            moduleId = module.id,
            title = itemTitle,
            type = itemType.toString(),
            position = module.itemCount,
            published = published,
            // I don't really know if these two should be the same, but I needed
            // htmlUrl populated in order to get external url module items to work.
            url = itemUrl,
            htmlUrl = itemUrl,
            contentId = contentId,
            moduleDetails = moduleContentDetails,
            unpublishable = unpublishable
    )

    // Copy/update/replace the module
    val newItemList = module.items.toMutableList()
    newItemList.add(result)
    val changedModule = module.copy(
            items = newItemList
    )

    val courseModuleList = courseModules[course.id]!!
    courseModuleList.remove(module)
    courseModuleList.add(changedModule)

    // Return our ModuleItem
    return result
}

// Create a Quiz and add it to the specified course
fun MockCanvas.addQuizToCourse(
        course: Course,
        title: String = Faker.instance().hitchhikersGuideToTheGalaxy().character(),
        description: String = Faker.instance().hitchhikersGuideToTheGalaxy().marvinQuote(),
        quizType: String = Quiz.TYPE_PRACTICE,
        timeLimitSecs: Int = 300,
        dueAt: String? = null,
        published: Boolean = true,
        lockAt: String? = null,
        unlockAt: String? = null,
        pointsPossible: Int? = null
) : Quiz {
    val quizId = newItemId()
    val quizUrl = "https://mock-data.instructure.com/api/v1/courses/${course.id}/quizzes/$quizId"
    var assignment : Assignment? = null

    // For quizzes that are actual assignments, create an associated Assignment object
    if(quizType == Quiz.TYPE_ASSIGNMENT) {
        assignment = Assignment(
                id = newItemId(),
                name = title,
                description = description,
                dueAt = dueAt,
                submissionTypesRaw = listOf("online_quiz"),
                quizId = quizId,
                courseId = course.id,
                lockAt = lockAt,
                unlockAt = unlockAt,
                allDates = listOf(AssignmentDueDate(id = newItemId(), dueAt = dueAt, lockAt = lockAt, unlockAt = unlockAt))
                )

        assignments.put(assignment.id, assignment)
    }

    val result = Quiz(
        id = quizId,
        title = title,
        description = description,
        quizType = quizType,
        mobileUrl = quizUrl,
        htmlUrl = quizUrl,
        timeLimit = timeLimitSecs,
        dueAt = dueAt,
        published = published,
        assignmentId = assignment?.id ?: 0,
        lockAt = lockAt,
        unlockAt = unlockAt,
        allDates = listOf(AssignmentDueDate(id = newItemId(), dueAt = dueAt, lockAt = lockAt, unlockAt = unlockAt)),
        pointsPossible = pointsPossible?.toString()
    )

    var quizList = courseQuizzes[course.id]
    if(quizList == null) {
        quizList = mutableListOf<Quiz>()
        courseQuizzes[course.id] = quizList
    }

    // Add to the quiz list for the course
    quizList.add(result)

    // Return our created quiz
    return result
}

// Add question to quiz
fun MockCanvas.addQuestionToQuiz(
        course: Course,
        quizId: Long,
        questionName: String?,
        questionText: String,
        questionType: String = "multiple_choice_question",
        pointsPossible: Int = 5,
        answers: Array<QuizAnswer>? = null
) : QuizQuestion {

    val quiz = courseQuizzes[course.id]!!.find {it.id == quizId}!!
    val result = QuizQuestion(
            id = newItemId(),
            quizId = quizId,
            position = quiz.questionCount,
            questionName = questionName,
            questionTypeString = questionType,
            questionText = questionText,
            pointsPossible = pointsPossible,
            answers = answers
    )

    // Make the necessary changes to the stored Quiz object by copy/changing/replacing it
    val newPointsPossible = ((quiz.pointsPossible?.toInt() ?: 0) + pointsPossible).toString()
    val newQuestionCount = quiz.questionCount + 1
    val newQuestionTypes = mutableListOf<String>()
    if(quiz.questionTypes != null) {
        newQuestionTypes.addAll(quiz.questionTypes)
    }
    newQuestionTypes.add(questionType)
    val newQuiz = quiz.copy(
            pointsPossible = newPointsPossible,
            questionCount = newQuestionCount,
            questionTypes = newQuestionTypes
    )
    val quizList = courseQuizzes[course.id]!!
    quizList.remove(quiz)
    quizList.add(newQuiz)

    // Add the newly created question to the question list for the quiz
    var questionList = quizQuestions[quizId]
    if(questionList == null) {
        questionList = mutableListOf<QuizQuestion>()
        quizQuestions[quizId] = questionList
    }
    questionList.add(result)

    // return the quiz question
    return result
}

fun MockCanvas.addQuizSubmission(quiz: Quiz, user: User, state: String = "untaken", grade: String? = null) : QuizSubmission {

    val now = Calendar.getInstance().time.time // ms
    val quizSubmission = QuizSubmission(
            id = newItemId(),
            quizId = quiz.id,
            userId = user.id,
            submissionId = newItemId(),
            startedAt = Date(now).toApiString(),
            endAt = Date(now + quiz.timeLimit * 1000).toApiString(), // ??
            workflowState = state,
            validationToken = "abcd" // just so it's not null??
    )

    // Don't forget to add some sort of submission to the related assignment
    if(quiz.assignmentId != 0L) {
        assignments[quiz.assignmentId]
        this.addSubmissionForAssignment(assignmentId = quiz.assignmentId, userId = user.id, type = "online_quiz", state = state, grade = grade)
    }

    var submissionList = quizSubmissions[quiz.id]
    if(submissionList == null) {
        submissionList = mutableListOf<QuizSubmission>()
        quizSubmissions[quiz.id] = submissionList
    }
    submissionList.add(quizSubmission)

    // It seems like we will need to populate a list of QuizSubmissionQuestions
    // for this submission, to match the QuizQuestions for the quiz.  We'll do that
    // right now.
    val submissionQuestionList = mutableListOf<QuizSubmissionQuestion>()
    val questionList = quizQuestions[quiz.id] ?: listOf<QuizQuestion>()
    for(q in questionList) {
        val submissionAnswers = mutableListOf<QuizSubmissionAnswer>()
        if(q.answers != null) {
            for (a in q.answers!!) {
                submissionAnswers.add(QuizSubmissionAnswer(
                        text = a.answerText,
                        weight = a.answerWeight
                ))
            }
        }
        val sq = QuizSubmissionQuestion(
                id = q.id,
                quizId = quiz.id,
                questionName = q.questionName,
                questionType = q.questionTypeString,
                questionText = q.questionText,
                answers = submissionAnswers.toTypedArray()
        )
        submissionQuestionList.add(sq)
    }
    quizSubmissionQuestions[quizSubmission.id] = submissionQuestionList
    return quizSubmission
}


// Add rubric criteria to an assignment
fun MockCanvas.addRubricToAssignment(assignmentId: Long, criteria : List<RubricCriterion>) {
    // Find the assignment
    val assignment = assignments[assignmentId]!!
    val courseId = assignment.courseId

    // Create a modified assignment with rubric info
    val newAssignment = assignment.copy(rubric = criteria, isUseRubricForGrading = true)

    // Replace the old assignment with the modified assignment in assignments hash
    assignments[assignmentId] = newAssignment

    // Replace the old assignment with the modified assignment in the assignment group hash
    val groupList = assignmentGroups[courseId]
    if (groupList != null) {
        groupList.forEach { group ->
            if (group.assignments.contains(assignment)) {
                val newList = group.assignments.toMutableList()
                newList.remove(assignment)
                newList.add(newAssignment)
                val newGroup = group.copy(assignments = newList)
                groupList.remove(group)
                groupList.add(newGroup)

                // A "break" here would be wonderful, but is apparently not allowed in a forEach
            }
        }
    }
}

/** Add a group, associated with a course. */
fun MockCanvas.addGroupToCourse(
        course: Course,
        name: String = Faker.instance().country().capital(),
        description: String = Faker.instance().lordOfTheRings().character(),
        members: List<User> = listOf(),
        isFavorite: Boolean = false
) : Group {
    val result = Group(
            id = newItemId(),
            name = name,
            description = description,
            isPublic = true,
            users = members,
            // contextType field removed from Group
            //contextType = Group.GroupContext.Course,
            courseId = course.id,
            isFavorite = isFavorite
    )

    result.permissions = CanvasContextPermission(canCreateAnnouncement = true)

    groups[result.id] = result

    return result
}

/**
 * Creates a full screen pen annotation
 *
 * [hasComment] -> Will result in a second annotation "in reply to" the first full coverage pen annotation
 * [author] -> Used for the original annotation, in the case of the student app, this would be the teacher
 * [replyingAuthor] -> Used for the follow up reply annotation, in the case of the student app, this would be the student
 */
fun createFullCoverageAnnotation(
        author: User,
        replyingAuthor: User,
        docId: String,
        data: MockCanvas,
        hasComment: Boolean = false,
        commentContents: String? = null
): List<CanvaDocAnnotation> {
    val annotation = CanvaDocAnnotation(
        createdAt = APIHelper.dateToString(GregorianCalendar()),
        rect = arrayListOf(arrayListOf(45.285324f, 80.672485f), arrayListOf(565.24457f,745.6419f)),
        page = 0,
        userId = author.id.toString(),
        userName = author.name,
        context = "default",
        width = 10f,
        documentId = docId,
        isEditable = false,
        annotationId = data.newItemId().toString(),
        color = "#008EE2",
        annotationType = CanvaDocAnnotation.AnnotationType.INK,
        inklist = canvaDocInk
    )
    if(hasComment) {
        val commentAnnotation = CanvaDocAnnotation(
            page = 0,
            createdAt = APIHelper.dateToString(GregorianCalendar()),
            contents = commentContents!!,
            userId = replyingAuthor.id.toString(),
            userName = replyingAuthor.name,
            context = "default",
            documentId = docId,
            isEditable = false,
            annotationId = data.newItemId().toString(),
            annotationType = CanvaDocAnnotation.AnnotationType.COMMENT_REPLY,
            inReplyTo = annotation.annotationId
        )

        return listOf(annotation, commentAnnotation)
    } else {
        return listOf(annotation)
    }
}

/**
 * Similar to the sentConversation, this configures the "to be sent" annotation for the annotation comment list
 *
 * [author] -> The author of this annotation should be the signed in user
 */
fun MockCanvas.addSentAnnotation(targetAnnotationId: String, commentContents: String, author: User, docId: String) {
    sentAnnotationComment = CanvaDocAnnotation(
        page = 0,
        createdAt = APIHelper.dateToString(GregorianCalendar()),
        contents = commentContents,
        userId = author.id.toString(),
        userName = author.name,
        context = "default",
        documentId = docId,
        isEditable = false,
        annotationId = newItemId().toString(),
        annotationType = CanvaDocAnnotation.AnnotationType.COMMENT_REPLY,
        inReplyTo = targetAnnotationId
    )
}

/**
 * This function configures all necessary values for making requests to a submission's previewUrl to start a doc viewer
 * session.
 *
 * This includes the docid, docSession, pdfDownloadUrl, canvadocRedirectUrl, and the annotation list for future
 * requests.
 *
 * This also includes the optional params to set up a reply to comment annotation as well as a pending sent comment
 * annotation.
 */
fun MockCanvas.addAnnotation(
        signedInUser: User,
        annotationAuthor: User,
        hasComment: Boolean = false,
        hasSentComment: Boolean = false,
        commentContents: String? = null,
        sentCommentContents: String? = null
): DocSession {
    val docSessionId = newItemId().toString()
    val docId = newItemId().toString()
    val pdfDownloadUrl = """/1/sessions/$docSessionId/file/file.pdf"""
    val docSession = DocSession(
        documentId = newItemId().toString(),
        annotationUrls = AnnotationUrls(
            pdfDownload = pdfDownloadUrl,
            annotatedPdfDownload = pdfDownloadUrl
        ),
        annotationMetadata = AnnotationMetadata(
            enabled = true,
            userName = signedInUser.name,
            userId = signedInUser.id.toString(),
            permissions = "read"
        )
    )
    val annotation = createFullCoverageAnnotation(annotationAuthor, signedInUser, docId, this, hasComment, commentContents)
    if(hasSentComment) addSentAnnotation(annotation.first().annotationId, sentCommentContents!!, signedInUser, docId)
    annotations[docSessionId] = annotation
    docSessions[docSessionId] = docSession
    canvadocRedirectUrl = "https://mock-data.instructure.com/1/sessions/$docSessionId"

    return docSession
}

private val canvaDocInk = CanvaDocInkList(
    arrayListOf(arrayListOf(
        CanvaDocCoordinate(46f, 740f),
        CanvaDocCoordinate(80f, 740f),
        CanvaDocCoordinate(120f, 740f),
        CanvaDocCoordinate(160f, 740f),
        CanvaDocCoordinate(200f, 740f),
        CanvaDocCoordinate(240f, 740f),
        CanvaDocCoordinate(280f, 740f),
        CanvaDocCoordinate(320f, 740f),
        CanvaDocCoordinate(360f, 740f),
        CanvaDocCoordinate(400f, 740f),
        CanvaDocCoordinate(440f, 740f),
        CanvaDocCoordinate(480f, 740f),
        CanvaDocCoordinate(550f, 740f),
        CanvaDocCoordinate(46f, 540f),
        CanvaDocCoordinate(80f, 540f),
        CanvaDocCoordinate(120f, 540f),
        CanvaDocCoordinate(160f, 540f),
        CanvaDocCoordinate(200f, 540f),
        CanvaDocCoordinate(240f, 540f),
        CanvaDocCoordinate(280f, 540f),
        CanvaDocCoordinate(320f, 540f),
        CanvaDocCoordinate(360f, 540f),
        CanvaDocCoordinate(400f, 540f),
        CanvaDocCoordinate(440f, 540f),
        CanvaDocCoordinate(480f, 540f),
        CanvaDocCoordinate(550f, 540f),
        CanvaDocCoordinate(46f, 320f),
        CanvaDocCoordinate(80f, 320f),
        CanvaDocCoordinate(120f, 320f),
        CanvaDocCoordinate(160f, 320f),
        CanvaDocCoordinate(200f, 320f),
        CanvaDocCoordinate(240f, 320f),
        CanvaDocCoordinate(280f, 320f),
        CanvaDocCoordinate(320f, 320f),
        CanvaDocCoordinate(360f, 320f),
        CanvaDocCoordinate(400f, 320f),
        CanvaDocCoordinate(440f, 320f),
        CanvaDocCoordinate(480f, 320f),
        CanvaDocCoordinate(550f, 320f),
        CanvaDocCoordinate(46f, 100f),
        CanvaDocCoordinate(80f, 100f),
        CanvaDocCoordinate(120f, 100f),
        CanvaDocCoordinate(160f, 100f),
        CanvaDocCoordinate(200f, 100f),
        CanvaDocCoordinate(240f, 100f),
        CanvaDocCoordinate(280f, 100f),
        CanvaDocCoordinate(320f, 100f),
        CanvaDocCoordinate(360f, 100f),
        CanvaDocCoordinate(400f, 100f),
        CanvaDocCoordinate(440f, 100f),
        CanvaDocCoordinate(480f, 100f),
        CanvaDocCoordinate(550f, 100f)
    ))
)

/**
 * Adds a stream item for a submission, which should then show up in our notification list.
 * Consider doing this automatically whenever a submission is processed?
 */
fun MockCanvas.addSubmissionStreamItem(
    user: User,
    course: Course,
    assignment: Assignment,
    submission: Submission,
    submittedAt: String? = null,
    message: String = Faker.instance().lorem().sentence(),
    type: String = "submission",
    score: Double = -1.0,
    grade: String? = null,
    excused: Boolean = false
): StreamItem {
    // Create the StreamItem
    val item = StreamItem(
        id = newItemId(),
        course_id = course.id,
        assignment_id = assignment.id,
        title = assignment.name,
        message = message,
        assignment = assignment,
        type = type,
        submittedAt = submittedAt,
        userId = user.id,
        user = user,
        updatedAt = submittedAt ?: "",
        htmlUrl = "https://$domain/courses/${course.id}/assignments/${assignment.id}/submissions/${submission.id}",
        context_type = CanvasContext.Type.USER.apiString,
        score = score,
        grade = grade,
        excused = excused
        //canvasContext = user // This seems to break the notifications page so that it does not load
    )

    // Record the StreamItem
    var list = streamItems[user.id]
    if (list == null) {
        list = mutableListOf()
        streamItems[user.id] = list
    }
    list.add(item)

    // Return the StreamItem
    return item
}

fun MockCanvas.addTodo(name: String, userId: Long, courseId: Long? = null, date: Date? = null, details: String? = null): PlannerItem {
    val todo = PlannerItem(
        courseId,
        null,
        userId,
        null,
        null,
        PlannableType.TODO,
        Plannable(newItemId(), name, courseId, null, userId, null, date, null, date.toApiString(), null, null, details, null),
        date ?: Date(),
        null,
        null,
        null
    )

    todos.add(todo)
    return todo
}

fun MockCanvas.addPlannable(name: String, userId: Long, course: Course? = null, date: Date? = null, details: String? = null, type: PlannableType): PlannerItem {
    val todo = PlannerItem(
        course?.id,
        null,
        userId,
        if (course != null) CanvasContext.Type.COURSE.apiString else CanvasContext.Type.USER.apiString,
        course?.name,
        plannableType = type,
        Plannable(newItemId(), name, course?.id, null, userId, null, date, null, date.toApiString(), null, null, details, null),
        date ?: Date(),
        null,
        null,
        null
    )

    todos.add(todo)
    return todo
}

fun MockCanvas.addObserverAlert(
    observer: User,
    student: User,
    canvasContext: CanvasContext,
    alertType: AlertType,
    workflowState: AlertWorkflowState,
    actionDate: Date,
    htmlUrl: String?,
    lockedForUser: Boolean,
    threshold: String? = null,
    observerAlertThresholdId: Long? = null
): Alert {

    val alerts = observerAlerts[student.id] ?: mutableListOf()

    val thresholdId: Long = observerAlertThresholdId ?: newItemId()
    if (!observerAlertThresholds.containsKey(thresholdId)) {
        addObserverAlertThreshold(thresholdId, alertType, observer, student, threshold)
    }

    val alert = Alert(
        id = newItemId(),
        observerId = observer.id,
        userId = student.id,
        observerAlertThresholdId = thresholdId,
        contextType = canvasContext.type.apiString,
        contextId = canvasContext.id,
        alertType = alertType,
        workflowState = workflowState,
        actionDate = actionDate,
        title = Randomizer.randomAlertTitle(),
        htmlUrl = htmlUrl,
        lockedForUser = lockedForUser
    )

    val updatedList = alerts.toMutableList().apply {
        add(alert)
    }

    observerAlerts[student.id] = updatedList

    return alert
}

fun MockCanvas.addObserverAlertThreshold(id: Long, alertType: AlertType, observer: User, student: User, threshold: String?) {
    val thresholds = observerAlertThresholds[student.id]?.toMutableList() ?: mutableListOf()

    thresholds.add(
        AlertThreshold(
            id = id,
            observerId = observer.id,
            userId = student.id,
            threshold = threshold,
            alertType = alertType,
            workflowState = ThresholdWorkflowState.ACTIVE
        )
    )

    observerAlertThresholds[student.id] = thresholds
}

fun MockCanvas.addPairingCode(student: User): String {
    val pairingCode = Randomizer.randomPairingCode()
    pairingCodes[pairingCode] = student
    return pairingCode
}