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
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.toApiString
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
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

    /** Map of group id to group object */
    val groups = mutableMapOf<Long, Group>()

    /** Map of course ID to tabs for the course */
    val courseTabs = mutableMapOf<Long, MutableList<Tab>>()

    /** Map of course ID to pages for the course */
    val coursePages = mutableMapOf<Long, MutableList<Page>>()

    /** Map of course ID to root folders */
    val courseRootFolders = mutableMapOf<Long, FileFolder>()

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
    val assignmentGroups = mutableMapOf<Long, List<AssignmentGroup>>()

    /** Map of course ID to a list of submissions */
    val submissions = mutableMapOf<Long, List<Submission>>()

    var ltiTool: LTITool? = null

    /** Map of course ids to discussion topic headers */
    val courseDiscussionTopicHeaders = mutableMapOf<Long, MutableList<DiscussionTopicHeader>>()

    /** Map of topic ids to DiscussionTopics */
    val discussionTopics = mutableMapOf<Long, DiscussionTopic>()

    // A few discussion-related permissions that we need to be able to tweak.
    var discussionRepliesEnabled: Boolean = true
    var discussionAttachmentsEnabled: Boolean = true
    var discussionRatingsEnabled: Boolean = true
    var discussionEntryDefaultLikes: Int = 5

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

    // Webview support -- goes through a mock server
    val webViewServer = MockWebServer()
    // The dispatcher contains all of our matching/capture logic for webview requests
    val webViewServerDispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            Log.d("WebView", "dispatch() request: $request")
            var path = request.path
            if(path.startsWith("//")) path = path.substring(1)

            // Test as to whether this is a /courses/{courseId}/files/{fileId}/preview request,
            // and if it is then service it.
            val courseFilePreviewRegex = """/courses/(\d+)/files/(\d+)/(preview|download)""".toRegex()
            courseFilePreviewRegex.matchEntire(path)?.run {
                // groupValues[0] is the entire string, I think
                Log.d("WebView", "Matched courseFilePreviewRegex: course=${groupValues[1]}, file=${groupValues[2]}")
                val fileId = groupValues[2].toLong()
                val contents = fileContents[fileId]
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

    // Perform the finishing operational touches for our web server
    data.webViewServer.dispatcher = data.webViewServerDispatcher
    data.webViewServer.start()

    return data
}

/** Creates a new Course and adds it to MockCanvas */
fun MockCanvas.addCourse(
        isFavorite: Boolean = false,
        concluded: Boolean = false): Course {
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
    val assignmentsTab = Tab(position = 0, label = "Assignments", visibility = "public")
    val quizzesTab = Tab(position = 1, label = "Quizzes", visibility = "public")
    courseTabs += course.id to mutableListOf(assignmentsTab, quizzesTab)

    return course
}

/**
 * Creates assignments for the standard groups (overdue, upcoming, undated, and past) for a course
 * and adds it to MockCanvas
 *
 */
fun MockCanvas.addAssignmentsToGroups(course: Course, assignmentCountPerGroup: Int = 1): List<AssignmentGroup> {
    val overdueAssignments = ArrayList<Assignment>()
    val upcomingAssignments = ArrayList<Assignment>()
    val undatedAssignments = ArrayList<Assignment>()
    val pastAssignments = ArrayList<Assignment>()

    val futureDueDate = OffsetDateTime.now().plusWeeks(1).toApiString()
    val pastDueDate = OffsetDateTime.now().minusWeeks(1).toApiString()

    for (i in 0 until assignmentCountPerGroup) {
        overdueAssignments.add(Assignment(
            id = i.toLong(),
            name = Randomizer.randomAssignmentName(),
            courseId = course.id,
            submission = Submission(grade = null, submissionType = Assignment.SubmissionType.ONLINE_UPLOAD.apiString),
            submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_UPLOAD.apiString),
            dueAt = pastDueDate
        ))
        upcomingAssignments.add(Assignment(
            id = (assignmentCountPerGroup * 2) + i.toLong(),
            name = Randomizer.randomAssignmentName(),
            courseId = course.id,
            submission = Submission(),
            dueAt = futureDueDate
        ))
        undatedAssignments.add(Assignment(
            id = (assignmentCountPerGroup * 3) + i.toLong(),
            name = Randomizer.randomAssignmentName(),
            courseId = course.id,
            submission = Submission(),
            dueAt = null
        ))
        pastAssignments.add(Assignment(
            id = (assignmentCountPerGroup * 4) + i.toLong(),
            name = Randomizer.randomAssignmentName(),
            courseId = course.id,
            submission = Submission(grade = "A", submissionType = Assignment.SubmissionType.ONLINE_UPLOAD.apiString),
            submissionTypesRaw = listOf(),
            dueAt = pastDueDate
        ))
    }

    val overdueAssignmentGroup = AssignmentGroup(id = 1, name = "overdue", assignments = overdueAssignments)
    val upcomingAssignmentGroup = AssignmentGroup(id = 2, name = "upcoming", assignments = upcomingAssignments)
    val undatedAssignmentGroup = AssignmentGroup(id = 3, name = "undated", assignments = undatedAssignments)
    val pastAssignmentGroup = AssignmentGroup(id = 4, name = "past", assignments = pastAssignments)

    val assignmentGroupList = listOf(overdueAssignmentGroup, upcomingAssignmentGroup, undatedAssignmentGroup, pastAssignmentGroup)

    assignmentGroups[course.id] = assignmentGroupList

    return assignmentGroupList
}

/**
 * Adds a single assignment to the course assignment group map. This function does not currently take into
 * account existing assignments. Use either addAssignment or addAssignmentsToGroups.
 */
fun MockCanvas.addAssignment(
        courseId: Long,
        groupType: AssignmentGroupType,
        submissionType: Assignment.SubmissionType,
        isQuizzesNext: Boolean = false) : Assignment {
    val assignmentId = 123L
    val assignmentGroupId = 123L
    var assignment = Assignment(
        id = assignmentId,
        assignmentGroupId = assignmentGroupId,
        courseId = courseId,
        name = Randomizer.randomAssignmentName(),
        submissionTypesRaw = listOf(submissionType.apiString)
    )

    val futureDueDate = OffsetDateTime.now().plusWeeks(1).toApiString()
    val pastDueDate = OffsetDateTime.now().minusWeeks(1).toApiString()

    if(isQuizzesNext) {
        assignment = assignment.copy(
            url = "https://mobiledev.instructure.com/api/v1/courses/1567973/external_tools/sessionless_launch?assignment_id=24378681&launch_type=assessment"
        )
    }

    when(groupType) {
        AssignmentGroupType.OVERDUE -> {
            assignment = assignment.copy(
                submission = Submission(grade = null, submissionType = submissionType.apiString),
                dueAt = pastDueDate
            )
        }
        AssignmentGroupType.UPCOMING -> {
            assignment = assignment.copy(
                submission = Submission(),
                dueAt = futureDueDate
            )
        }
        AssignmentGroupType.UNDATED -> {
            assignment = assignment.copy(
                submission = Submission(),
                dueAt = null
            )
        }
        AssignmentGroupType.PAST -> {
            assignment = assignment.copy(
                courseId = courseId,
                submission = Submission(grade = "A", submissionType = submissionType.apiString),
                submissionTypesRaw = listOf(submissionType.apiString),
                dueAt = pastDueDate
            )
        }
    }

    val assignmentGroup = AssignmentGroup(id = assignmentGroupId, assignments = listOf(assignment))

    assignmentGroups[courseId] = listOf(assignmentGroup)

    return assignment
}
/**
 * Adds a submission to the course submission map.
 *
 * NOTE - This function does not add the submission to the assignment groups map, that happens in the POST end point for submissions.
 * */
fun MockCanvas.addSubmission(courseId: Long, submission: Submission, assignmentId: Long?) {
    submissions[courseId] = if (assignmentId != null) listOf(submission.copy(assignmentId = assignmentId)) else listOf(submission)
}

fun MockCanvas.addLTITool(name: String, url: String): LTITool {
    val ltiTool = LTITool(id = 123L, name = name, url = url)

    this.ltiTool = ltiTool

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

/** Creates a new page for a given course, and adds it to MockCanvas */
fun MockCanvas.addPageToCourse(
        courseId: Long,
        pageId: Long = 0,
        url: String? = null,
        title: String = Randomizer.randomPageTitle(),
        body: String = Randomizer.randomPageBody(),
        published: Boolean = false
): Page {

    val page = Page(
            id = pageId,
            url = url,
            title = title,
            body = body,
            published = published
    )

    var list = coursePages[courseId]
    if (list == null) {
        list = mutableListOf<Page>()
        coursePages.put(courseId, list)
    }
    list.add(page)

    return page
}

/** Creates a new file for the specified course, and records it properly in MockCanvas */
fun MockCanvas.addFileToCourse(
        courseId: Long,
        displayName: String,
        fileContent: String = Randomizer.randomPageBody(),
        contentType: String = "text/plain"): Long {
    var courseRootFolder = courseRootFolders[courseId]
    if (courseRootFolder == null) {
        val folderId = newItemId()
        courseRootFolder = FileFolder(
                id = folderId,
                contextType = "Course",
                contextId = courseId,
                name = "course files",
                fullName = "course files",
                filesUrl = "https://mock-data.instructure.com/api/v1/folders/$folderId/files",
                foldersUrl = "https://mock-data.instructure.com/api/v1/folders/$folderId/folders"
        )
        courseRootFolders[courseId] = courseRootFolder
        fileFolders[courseRootFolder.id] = courseRootFolder
    }

    // Now create our file metadata
    val fileId = newItemId()
    val fileMetadataItem = FileFolder(
            id = fileId,
            folderId = courseRootFolder.id,
            size = fileContent.length.toLong(),
            displayName = displayName,
            contentType = contentType,
            url = "https://mock-data.instructure.com/files/$fileId/preview" // Unused, I think
    )

    // And record it for the folder
    var fileList = folderFiles[courseRootFolder.id]
    if (fileList == null) {
        fileList = mutableListOf<FileFolder>()
        folderFiles[courseRootFolder.id] = fileList
    }
    //  TODO: Update courseRootFolder.filesCount
    fileList.add(fileMetadataItem)

    // Now record our file contents (just text for now)
    fileContents[fileId] = fileContent
    //Log.d("<--", "file($fileId) contents = \"$fileContent\"")

    return fileId
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
        allowReplies: Boolean = true,
        allowAttachments: Boolean = true,
        attachment: RemoteFile? = null
) : DiscussionTopicHeader {

    var topicHeader = prePopulatedTopicHeader
    if(topicHeader == null) {
        topicHeader = DiscussionTopicHeader(
                title = topicTitle,
                //id = newItemId(),
                //published = true,
                discussionType = "side_comment",
                message = topicDescription
                //allowRating = allowRating,
                //author = DiscussionParticipant(id = user.id, displayName = user.name),
                //permissions = DiscussionTopicPermission(attach = allowAttachments, reply = allowReplies)
        )
    }

    topicHeader.author = DiscussionParticipant(id = user.id, displayName = user.name)
    topicHeader.published = true
    topicHeader.allowRating = allowRating
    topicHeader.permissions = DiscussionTopicPermission(attach = allowAttachments, reply = allowReplies)
    topicHeader.id = newItemId()
    topicHeader.postedDate = Calendar.getInstance().time
    if(attachment != null) {
        topicHeader.attachments = mutableListOf<RemoteFile>(attachment)
    }

    var courseTopicHeaderList = courseDiscussionTopicHeaders[course.id]
    if(courseTopicHeaderList == null) {
        courseTopicHeaderList = mutableListOf<DiscussionTopicHeader>()
        courseDiscussionTopicHeaders[course.id] = courseTopicHeaderList
    }
    courseTopicHeaderList.add(topicHeader)

    val topic = DiscussionTopic(
            isForbidden = false,
            participants = mutableListOf<DiscussionParticipant>(
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
        attachment: RemoteFile? = null
) : DiscussionEntry {
    val topic = discussionTopics[topicHeader.id]
    val entry = DiscussionEntry(
            id = newItemId(),
            message = replyMessage,
            unread = true,
            userName = user.name,
            author = DiscussionParticipant(
                    id = user.id,
                    displayName = user.name
            ),
            createdAt = Calendar.getInstance().time.toString()
    )
    if(attachment != null) {
        entry.attachments = mutableListOf(attachment)
    }
    topic!!.views.add(entry)
    topic.unreadEntries.add(entry.id)
    topicHeader.unreadCount += 1
    return entry
}

