/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
@file:Suppress("unused")

package com.instructure.dataseeding.util

import com.instructure.dataseeding.api.*
import com.instructure.dataseeding.model.*
import java.util.*

object ApiManager {

        /*fun uploadTextFile(
            courseId: Long,
            assignmentId: Long? = null,
            token: String,
            fileUploadType: FileUploadType, file: File
        ): AttachmentApiModel {

            return FileUploadsApi.uploadFile(
                courseId,
                assignmentId,
                file.readBytes(),
                file.name,
                token,
                fileUploadType
            )
        }*/

        //Note: This method will use the adminCoursesService to create course.
        //fun createCourse(enrollmentTermId: Long? = null, publish: Boolean = true, syllabusBody: String? = null): CourseApiModel = CoursesApi.createCourse(enrollmentTermId = enrollmentTermId, publish = publish, syllabusBody = syllabusBody)

        //Note: This method will use a custom courses service to create course.
     //   fun createCourseWithCustomService(enrollmentTermId: Long? = null, publish: Boolean = true, coursesService: CoursesApi.CoursesService, syllabusBody: String? = null): CourseApiModel =  CoursesApi.createCourse(enrollmentTermId = enrollmentTermId, publish = publish, coursesService = coursesService, syllabusBody = syllabusBody)

       /* fun createAnnouncement(
            course: CourseApiModel,
            user: CanvasUserApiModel,
            lockedForUser: Boolean = false,
            locked: Boolean = false
        ): DiscussionApiModel {
            return DiscussionTopicsApi.createAnnouncement(
                course.id,
                user.token,
                lockedForUser,
                locked
            )
        }*/

        /*fun createDiscussion(
            course: CourseApiModel,
            user: CanvasUserApiModel
        ) = DiscussionTopicsApi.createDiscussion(
            courseId = course.id,
            token = user.token
        )*/

        /*fun createAssignment(
            course: CourseApiModel,
            teacher: CanvasUserApiModel,
            gradingType: GradingType = GradingType.POINTS,
            pointsPossible: Double = 15.0,
            dueAt: String = 1.days.fromNow.iso8601,
            allowedExtensions: List<String>? = null,
            assignmentGroupId: Long? = null,
            submissionType: List<SubmissionType> = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
            withDescription: Boolean = false,
            importantDate: Boolean = false
        ): AssignmentApiModel {
            return AssignmentsApi.createAssignment(
                AssignmentsApi.CreateAssignmentRequest(
                    courseId = course.id,
                    submissionTypes = submissionType,
                    gradingType = gradingType,
                    teacherToken = teacher.token,
                    pointsPossible = pointsPossible,
                    dueAt = dueAt,
                    allowedExtensions = allowedExtensions,
                    assignmentGroupId = assignmentGroupId,
                    withDescription = withDescription,
                    importantDate = importantDate
                )
            )
        }*/

       /* fun createAssignmentWithCalendar(
            course: CourseApiModel,
            user: CanvasUserApiModel,
            dateString: String,
            gradingType: GradingType,
            pointsPossible: Double
        ): AssignmentApiModel = createAssignment(course, user, gradingType, pointsPossible, dueAt = dateString)*/

        /*fun gradeSubmission(
            teacher: CanvasUserApiModel,
            course: CourseApiModel,
            assignment: AssignmentApiModel,
            student: CanvasUserApiModel,
            postedGrade: String? = null,
            excused: Boolean = false
        ) = SubmissionsApi.gradeSubmission(
            teacherToken = teacher.token,
            courseId = course.id,
            assignmentId = assignment.id,
            studentId = student.id,
            postedGrade = postedGrade,
            excused = excused
        )*/

       // fun getCourseGradingPeriods(course: CourseApiModel): GradingPeriods = GradingPeriodsApi.getGradingPeriodsOfCourse(course.id)

      /*  fun createAssignmentGroup(
            teacher: CanvasUserApiModel,
            course: CourseApiModel
        ) = AssignmentGroupsApi.createAssignmentGroup(
            token = teacher.token,
            courseId = course.id,
            name = "Discussions",
            position = null,
            groupWeight = null,
            sisSourceId = null
        )*/

      /*  fun assignmentMultipleSubmissions(
            assignment: AssignmentApiModel,
            course: CourseApiModel,
            student: CanvasUserApiModel,
            amount: Int = 1,
            submissionType: SubmissionType = SubmissionType.ONLINE_TEXT_ENTRY
        ): List<SubmissionApiModel> {
            return SubmissionsApi.seedAssignmentSubmission(
                SubmissionsApi.SubmissionSeedRequest(
                    assignmentId = assignment.id,
                    courseId = course.id,
                    studentToken = student.token,
                    submissionSeedsList = listOf(
                        SubmissionsApi.SubmissionSeedInfo(
                            amount = amount,
                            submissionType = submissionType
                        )
                    )
                )
            )
        }*/

       /* fun assignmentSingleSubmission(
            course: CourseApiModel,
            testAssignment: AssignmentApiModel,
            student: CanvasUserApiModel,
            fileIds: MutableList<Long> = mutableListOf(),
            submissionType: SubmissionType = SubmissionType.ONLINE_TEXT_ENTRY

        ) {
            SubmissionsApi.submitCourseAssignment(
                submissionType = submissionType,
                courseId = course.id,
                assignmentId = testAssignment.id,
                studentToken = student.token,
                fileIds = fileIds
            )
        }
*/
        /*fun submitCourseAssignment(
            course: CourseApiModel,
            percentageFileAssignment: AssignmentApiModel,
            uploadInfo: AttachmentApiModel,
            student: CanvasUserApiModel
        ) {
            SubmissionsApi.submitCourseAssignment(
                submissionType = SubmissionType.ONLINE_UPLOAD,
                courseId = course.id,
                assignmentId = percentageFileAssignment.id,
                fileIds = mutableListOf(uploadInfo.id),
                studentToken = student.token
            )
        }*/

       /* fun commentOnSubmission(
            user: CanvasUserApiModel,
            course: CourseApiModel,
            assignment: AssignmentApiModel,
            commentUploadInfo: AttachmentApiModel
        ) {
            SubmissionsApi.commentOnSubmission(
                studentToken = user.token,
                courseId = course.id,
                assignmentId = assignment.id,
                fileIds = mutableListOf(commentUploadInfo.id)
            )
        }
*/
       /* fun createConference(
            token: String,
            conferenceTitle: String,
            conferenceDescription: String,
            conferenceType: String,
            longRunning: Boolean,
            duration: Int,
            userIds: List<Long>,
            course: CourseApiModel
        ) =
            ConferencesApi.createCourseConference(
                token,
                conferenceTitle,
                conferenceDescription,
                conferenceType,
                longRunning,
                duration,
                userIds,
                course.id
            )
*/
        /*fun createConversation(
            token: String,
            recipients: List<String>,
            subject: String = Randomizer.randomConversationSubject(),
            body: String = Randomizer.randomConversationBody()
        )
                : List<ConversationApiModel> {
            return ConversationsApi.createConversation(
                token = token,
                recipients = recipients,
                subject = subject,
                body = body
            )
        }
*/
      /*  fun createCourseGroupCategory(
            course: CourseApiModel,
            token: String
        ): GroupCategoryApiModel = GroupsApi.createCourseGroupCategory(course.id, token)

        fun createGroup(groupCategory: GroupCategoryApiModel, token: String): GroupApiModel =
            GroupsApi.createGroup(groupCategory.id, token)

        fun createGroupMembership(
            group: GroupApiModel,
            user: CanvasUserApiModel,
            token: String
        ): GroupMembershipApiModel = GroupsApi.createGroupMembership(group.id, user.id, token)
*/
        /*fun createQuiz(
            course: CourseApiModel,
            teacher: CanvasUserApiModel,
            withDescription: Boolean = true,
            published: Boolean = true,
            dueAt: String = 1.days.fromNow.iso8601,
        ) = QuizzesApi.createQuiz(
            QuizzesApi.CreateQuizRequest(
                courseId = course.id,
                withDescription = withDescription,
                published = published,
                token = teacher.token,
                dueAt = dueAt
            )
        )*/

       // fun createAndPublishQuiz(course: CourseApiModel, user: CanvasUserApiModel, questions: List<QuizQuestion>): QuizApiModel = QuizzesApi.createAndPublishQuiz(course.id, user.token, questions)

       /* fun createPage(
            course: CourseApiModel,
            user: CanvasUserApiModel,
            published: Boolean = true,
            frontPage: Boolean = false,
            editingRoles: String? = null,
            body: String = Randomizer.randomPageBody()
        ) = PagesApi.createCoursePage(
            courseId = course.id,
            published = published,
            frontPage = frontPage,
            editingRoles = editingRoles,
            token = user.token,
            body = body
        )*/

      /*  fun createModule(
            course: CourseApiModel,
            teacher: CanvasUserApiModel,
            unlockAt: String? = null
        ) = ModulesApi.createModule(
            courseId = course.id,
            teacherToken = teacher.token,
            unlockAt = unlockAt
        )

        fun publishModule(
            course: CourseApiModel,
            module: ModuleApiModel,
            user: CanvasUserApiModel)
          {
            ModulesApi.updateModule(
                courseId = course.id,
                moduleId = module.id,
                published = true,
                teacherToken = user.token
            )
        }

        fun unPublishModule(
            course: CourseApiModel,
            module: ModuleApiModel,
            user: CanvasUserApiModel)
        {
            ModulesApi.updateModule(
                courseId = course.id,
                moduleId = module.id,
                published = false,
                teacherToken = user.token
            )
        }

        fun createModuleItem(
            courseId: Long,
            moduleId: Long,
            user: CanvasUserApiModel,
            moduleItemTitle: String,
            moduleItemType: String,
            contentId: String? = null,
            pageUrl: String? = null
        ) {
            ModulesApi.createModuleItem(
                courseId = courseId,
                moduleId = moduleId,
                teacherToken = user.token,
                moduleItemTitle = moduleItemTitle,
                moduleItemType = moduleItemType,
                contentId = contentId,
                pageUrl = pageUrl
            )
        }*/

        //With this method not only Student user can be created, but any role Canvas user! Note that this method will use userAdminService to create user.
    //    fun createUser(userDomain: String = CanvasNetworkAdapter.canvasDomain): CanvasUserApiModel = UserApi.createCanvasUser(userDomain = userDomain)

        //With this method not only Student user can be created, but any role Canvas user! Note that this method will use a custom user service to create user.
     //   fun createUserWithCustomService(customUserService: UserApi.UserService, userDomain: String = CanvasNetworkAdapter.canvasDomain): CanvasUserApiModel = UserApi.createCanvasUser(customUserService, userDomain)

        //Note that this method will use admin service to enroll a user to a course.
       /* fun enrollUser(
            course: CourseApiModel,
            user: CanvasUserApiModel,
            enrollmentType: String
        ): EnrollmentApiModel = EnrollmentsApi.enrollUser(courseId = course.id, userId = user.id, enrollmentType = enrollmentType)

        //Note that this method will use a custom service to enroll a user to a course.
        fun enrollUserWithCustomService(
            course: CourseApiModel,
            user: CanvasUserApiModel,
            enrollmentType: String,
            enrollmentsService: EnrollmentsApi.EnrollmentsService
        ): EnrollmentApiModel = EnrollmentsApi.enrollUser(courseId = course.id, userId = user.id, enrollmentType = enrollmentType, enrollmentService = enrollmentsService)*/

     //   fun getCourseRootFolder(course: CourseApiModel, user: CanvasUserApiModel) = FileFolderApi.getCourseRootFolder(course.id, user.token)

     //   fun createFolder(rootFolderId: Long, folderName: String, locked: Boolean, user: CanvasUserApiModel) = FileFolderApi.createCourseFolder(rootFolderId, folderName, locked, user.token)
}
