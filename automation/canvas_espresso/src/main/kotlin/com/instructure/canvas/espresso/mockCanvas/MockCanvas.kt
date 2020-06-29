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
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import com.instructure.canvasapi2.models.canvadocs.CanvaDocCoordinate
import com.instructure.canvasapi2.models.canvadocs.CanvaDocInkList
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.canvasapi2.utils.APIHelper
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

    /** Map of course permissions to course ids **/
    val coursePermissions = mutableMapOf<Long, CanvasContextPermission>()

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

    /** Map of group ID to tabs for the group */
    val groupTabs = mutableMapOf<Long, MutableList<Tab>>()

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
    val assignments = mutableMapOf<Long,Assignment>()

    /** Map of assignment ID to a list of submissions */
    val submissions = mutableMapOf<Long, MutableList<Submission>>()

    var ltiTool: LTITool? = null

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

            // Test as to whether this is a /groups/{courseId}/files/{fileId}/preview request,
            // and if it is then service it.
            val courseGroupFilePreviewRegex = """/groups/(\d+)/files/(\d+)/(preview|download)""".toRegex()
            courseGroupFilePreviewRegex.matchEntire(path)?.run {
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
        accountNotificationCount: Int = 0,
        createSections: Boolean = false
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
        if(createSections) {
            val sectionId = data.newItemId()
            section = Section(
                    id = sectionId,
                    name = "Section " + sectionId,
                    courseId = courseId,
                    students = studentUsers,
                    totalStudents = studentUsers.count()
            )
        }
        val course = data.addCourse(isFavorite = it < favoriteCourseCount, id = courseId, section = section)
    }
    repeat(pastCourseCount) { data.addCourse(concluded = true) }

    // Add enrollments
    data.courses.values.forEach { course ->
        // Enroll teachers
        teacherUsers.forEach { data.addEnrollment(it, course, Enrollment.EnrollmentType.Teacher) }

        // Enroll students
        studentUsers.forEach {
            data.addEnrollment(
                    user = it,
                    course = course,
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

/**
 * Not ideal, but in order to create realistic users, we have to add enrollments to them...
 * Unfortunately, in order to create enrollments, we have to have users first, hence the
 * copy nonesense seen here.
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

/** Creates a new Course and adds it to MockCanvas */
fun MockCanvas.addCourse(
        isFavorite: Boolean = false,
        concluded: Boolean = false,
        id: Long = newItemId(),
        section: Section? = null
): Course {
    val randomCourseName = Randomizer.randomCourseName()
    val endAt = if (concluded) OffsetDateTime.now().minusWeeks(1).toApiString() else null
    val course = Course(
            id = id,
            name = randomCourseName,
            originalName = randomCourseName,
            courseCode = randomCourseName.substring(0, 2),
            term = terms.values.first(),
            endAt = endAt,
            isFavorite = isFavorite,
            sections = if(section != null) listOf(section) else listOf<Section>()
    )
    courses += course.id to course

    // For now, give all courses tabs for assignments and quizzes
    val assignmentsTab = Tab(position = 0, label = "Assignments", visibility = "public", tabId = Tab.ASSIGNMENTS_ID)
    val quizzesTab = Tab(position = 1, label = "Quizzes", visibility = "public", tabId = Tab.QUIZZES_ID)
    courseTabs += course.id to mutableListOf(assignmentsTab, quizzesTab)

    return course
}

/** Adds the provided permissions to the course */
fun MockCanvas.addCoursePermissions(courseId: Long, permissions: CanvasContextPermission) {
    coursePermissions[courseId] = permissions
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

fun MockCanvas.addConversations(conversationCount: Int = 1, userId: Long, messageBody : String = Randomizer.randomConversationBody()) {
    for (i in 0 until conversationCount) {
        val sentConversation = createBasicConversation(userId = userId, isUserAuthor = true, messageBody = messageBody)
        val archivedConversation = createBasicConversation(userId, workflowState = Conversation.WorkflowState.ARCHIVED, messageBody = messageBody)
        val starredConversation = createBasicConversation(userId, isStarred = true, messageBody = messageBody)
        val unreadConversation = createBasicConversation(userId, workflowState = Conversation.WorkflowState.UNREAD, messageBody = messageBody)
        conversations[sentConversation.id] = sentConversation
        conversations[archivedConversation.id] = archivedConversation
        conversations[starredConversation.id] = starredConversation
        conversations[unreadConversation.id] = unreadConversation
    }
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

        val overdueAssignment = addAssignment(
                courseId = course.id,
                submissionType = Assignment.SubmissionType.ONLINE_URL,
                name = Randomizer.randomAssignmentName(),
                dueAt = pastDueDate,
                assignmentGroupId = overdueAssignmentGroup.id
        )
        val upcomingAssignment = addAssignment(
                courseId = course.id,
                submissionType = Assignment.SubmissionType.ONLINE_URL,
                name = Randomizer.randomAssignmentName(),
                dueAt = futureDueDate,
                assignmentGroupId = upcomingAssignmentGroup.id
        )

        val undatedAssignment = addAssignment(
                courseId = course.id,
                submissionType = Assignment.SubmissionType.ONLINE_URL,
                name = Randomizer.randomAssignmentName(),
                dueAt = null,
                assignmentGroupId = undatedAssignmentGroup.id
        )

        val pastAssignment = addAssignment(
                courseId = course.id,
                submissionType = Assignment.SubmissionType.ONLINE_URL,
                name = Randomizer.randomAssignmentName(),
                dueAt = pastDueDate,
                assignmentGroupId = pastAssignmentGroup.id
        )
        val pastSubmission = addSubmissionForAssignment(
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
        submissionType: Assignment.SubmissionType,
        assignmentGroupId: Long = newItemId(),
        isQuizzesNext: Boolean = false,
        lockInfo : LockInfo? = null,
        userSubmitted: Boolean = false,
        dueAt: String? = null,
        name: String = Randomizer.randomCourseName(),
        pointsPossible: Int = 10,
        description: String = ""
) : Assignment {
    val assignmentId = newItemId()
    var assignment = Assignment(
        id = assignmentId,
        assignmentGroupId = assignmentGroupId,
        courseId = courseId,
        name = name,
        submissionTypesRaw = listOf(submissionType.apiString),
        lockInfo = lockInfo,
        lockedForUser = lockInfo != null,
        userSubmitted = userSubmitted,
        dueAt = dueAt,
        pointsPossible = pointsPossible.toDouble(),
        description = description
    )

    if(isQuizzesNext) {
        assignment = assignment.copy(
            url = "https://mobiledev.instructure.com/api/v1/courses/1567973/external_tools/sessionless_launch?assignment_id=24378681&launch_type=assessment"
        )
    }

    // Figure out which assignment group in which to track the assignment
    var assignmentGroupList = assignmentGroups[courseId]
    if(assignmentGroupList == null) {
        assignmentGroupList = mutableListOf<AssignmentGroup>()
        assignmentGroups[courseId]= assignmentGroupList
    }

    var group = assignmentGroupList.find {it.id == assignmentGroupId}
    if(group == null) {
        assignmentGroupList.add(AssignmentGroup(id = assignmentGroupId, assignments = listOf(assignment)))
    }
    else {
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
        comment: SubmissionComment? = null
) : Submission {
    val submission = Submission(
            id = newItemId(),
            submittedAt = Date(),
            attempt = 1,
            body = body,
            url = url,
            previewUrl = url,
            workflowState = "submitted",
            submissionType = type,
            assignmentId = assignmentId,
            userId = userId,
            late = false,
            attachments = if(attachment != null) arrayListOf(attachment) else arrayListOf<Attachment>(),
            submissionComments = if(comment != null) listOf(comment) else listOf<SubmissionComment>(),
            mediaContentType = if(attachment != null) attachment.contentType else null
    )

    // Submission details looks to pull attachments from the first item in the submission history, so we need a "root"
    // submission to hold it all together.
    val rootSubmission = Submission(
            id = newItemId(),
            submittedAt = Date(),
            attempt = 1,
            body = body,
            url = url,
            previewUrl = url,
            workflowState = "submitted",
            submissionType = type,
            assignmentId = assignmentId,
            userId = userId,
            late = false,
            submissionHistory = listOf(submission),
            attachments = if(attachment != null) arrayListOf(attachment) else arrayListOf<Attachment>(),
            submissionComments = if(comment != null) listOf(comment) else listOf<SubmissionComment>(),
            mediaContentType = if(attachment != null) attachment.contentType else null
    )

    var submissionList = submissions[assignmentId]
    if(submissionList == null) {
        submissionList = mutableListOf<Submission>()
        submissions[assignmentId] = submissionList
    }
    submissionList.add(rootSubmission)

    val assignment = assignments[assignmentId]
    assignment?.submission = rootSubmission

    return rootSubmission
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
        observedUser: User? = null,
        courseSectionId: Long = 0
): Enrollment {
    val enrollment = Enrollment(
            id = enrollments.size + 1L,
            role = type,
            type = type,
            courseId = course.id,
            enrollmentState = "active",
            userId = user.id,
            observedUser = observedUser,
            grades = Grades(currentScore = 88.1, currentGrade = "B+"),
            courseSectionId = courseSectionId
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
    var rootFolder = getRootFolder(courseId = courseId, groupId = groupId)

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
        fileId: Long = newItemId()
) : Long {
    if(courseId == null && folderId == null) {
        throw Exception("Either courseId or folderId must be non-null")
    }
    var rootFolder = getRootFolder(courseId = courseId, folderId = folderId)

    // Now create our file metadata
    val fileMetadataItem = FileFolder(
            id = fileId,
            folderId = rootFolder.id,
            size = fileContent.length.toLong(),
            displayName = displayName,
            contentType = contentType,
            url = if(url.isEmpty()) "https://mock-data.instructure.com/files/$fileId/preview" else url
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
        displayName: String,
        fileContent: String = Randomizer.randomPageBody(),
        contentType: String = "text/plain",
        groupId: Long? = null,
        url: String = "",
        fileId: Long = newItemId()
): Long {
    var rootFolder = getRootFolder(courseId = courseId, groupId = groupId)
    return addFileToFolder(
            folderId = rootFolder.id,
            displayName = displayName,
            fileContent = fileContent,
            contentType = contentType,
            url = url,
            fileId = fileId
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
        allowReplies: Boolean = true,
        allowAttachments: Boolean = true,
        attachment: RemoteFile? = null,
        isAnnouncement: Boolean = false,
        sections: List<Section> = listOf(),
        groupId: Long? = null
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
    topicHeader.permissions = DiscussionTopicPermission(attach = allowAttachments, reply = allowReplies)
    topicHeader.id = newItemId()
    topicHeader.postedDate = Calendar.getInstance().time
    if(attachment != null) {
        topicHeader.attachments = mutableListOf<RemoteFile>(attachment)
    }
    topicHeader.announcement = isAnnouncement
    topicHeader.sections = sections

    var topicHeaderList = if(groupId != null) groupDiscussionTopicHeaders[groupId] else courseDiscussionTopicHeaders[course.id]
    if(topicHeaderList == null) {
        topicHeaderList = mutableListOf<DiscussionTopicHeader>()
        if(groupId != null) {
            groupDiscussionTopicHeaders[groupId] = topicHeaderList
        }
        else {
            courseDiscussionTopicHeaders[course.id] = topicHeaderList
        }
    }

    topicHeaderList.add(topicHeader)

    val topic = DiscussionTopic(
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
        published: Boolean = true
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
            htmlUrl = itemUrl
    )

    // Copy/update/replace the module
    var newItemList = module.items.toMutableList()
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
        dueAt: String? = null
) : Quiz {
    val quizId = newItemId()
    val quizUrl = "https://mock-data.instructure.com/api/v1/courses/${course.id}/quizzes/$quizId"
    val result = Quiz(
            id = quizId,
            title = title,
            description = description,
            quizType = quizType,
            mobileUrl = quizUrl,
            htmlUrl = quizUrl,
            timeLimit = timeLimitSecs,
            dueAt = dueAt
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
        members: List<User> = listOf()
) : Group {
    var result = Group(
            id = newItemId(),
            name = name,
            description = description,
            isPublic = true,
            users = members,
            // contextType field removed from Group
            //contextType = Group.GroupContext.Course,
            courseId = course.id
    )

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