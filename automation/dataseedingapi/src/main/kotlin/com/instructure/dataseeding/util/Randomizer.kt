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


package com.instructure.dataseeding.util

import com.github.javafaker.Faker
import com.instructure.dataseeding.model.CreateAssignment
import com.instructure.dataseeding.model.CreateDiscussionTopic
import com.instructure.dataseeding.model.CreateGroup
import com.instructure.dataseeding.model.CreateModule
import com.instructure.dataseeding.model.CreateQuiz
import com.instructure.dataseeding.model.CreateSubmissionComment
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.model.SubmitCourseAssignmentSubmission
import java.util.Date
import java.util.Locale
import java.util.UUID

object Randomizer {
    private val faker = Faker()

    private fun randomUUID() = UUID.randomUUID().toString()

    /** Creates a random [Name] */
    fun randomName() = Name(faker.name().firstName(), faker.name().lastName())

    /** Creates random name for a course */
    fun randomCourseName(): String = faker.educator().course() + " " + randomUUID()

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


    fun randomDiscussion(discussionTitle: String?, isAnnouncement: Boolean = false, lockedForUser: Boolean = false, locked: Boolean = false): CreateDiscussionTopic =
            CreateDiscussionTopic(
                    title = discussionTitle ?: faker.lorem().sentence(),
                    message = faker.lorem().paragraph(),
                    isAnnouncement = isAnnouncement,
                    lockedForUser = lockedForUser,
                    locked = locked
            )

    fun randomConversationSubject(): String = faker.chuckNorris().fact()
    fun randomConversationBody(): String = faker.lorem().paragraph()

    fun randomConferenceTitle(): String = faker.chuckNorris().fact()
    fun randomConferenceDescription(): String = faker.lorem().paragraph()


    fun randomEnrollmentTitle(): String = "${faker.pokemon()} Term"

    fun randomGradingPeriodSetTitle(): String = "${faker.pokemon().location()} Set"
    fun randomGradingPeriodName(): String = "${faker.pokemon().name()} Grading Period"

    fun randomAssignment(assignmentName: String?, withDescription: Boolean = false, lockAt: String, unlockAt: String, dueAt: String, submissionTypes: List<SubmissionType>, gradingType: GradingType?, groupCategoryId: Long?, pointsPossible: Double?, allowedExtensions: List<String>?, importantDate: Boolean?, assignmentGroupId: Long? = null): CreateAssignment =
            CreateAssignment(
                    name = assignmentName ?: faker.lorem().sentence(),
                    description = if (withDescription) faker.lorem().paragraph() else null,
                    lockAt = if (lockAt.isNotBlank()) lockAt else null,
                    unlockAt = if (unlockAt.isNotBlank()) unlockAt else null,
                    dueAt = if (dueAt.isNotBlank()) dueAt else null,
                    submissionTypes = if (submissionTypes.isEmpty()) null else submissionTypes.map {
                        if (it.name == "NO_TYPE") "none" else it.name.lowercase(Locale.getDefault())
                    },
                    gradingType = gradingType?.toString()?.lowercase(Locale.getDefault()) ?: "points",
                    groupCategoryId = groupCategoryId,
                    pointsPossible = pointsPossible,
                    allowedExtensions = allowedExtensions,
                    importantDate = importantDate,
                    assignmentGroupId = assignmentGroupId
            )

    fun randomAssignmentOverrideTitle(): String = faker.food().ingredient()

    fun randomSubmission(submissionType: SubmissionType, fileAttachments: List<Long>): SubmitCourseAssignmentSubmission =
            SubmitCourseAssignmentSubmission().apply {
                when (submissionType) {
                    SubmissionType.ONLINE_TEXT_ENTRY -> this.body = "<p>${faker.lorem().paragraph()}</p>"
                    SubmissionType.ONLINE_UPLOAD -> this.fileIds = if (fileAttachments.isNotEmpty()) fileAttachments else null
                    SubmissionType.ONLINE_URL -> this.url = "https://google.com"
                    else -> {
                        // TODO: Handle other types
                    }
                }
                this.submissionType =
                        if (submissionType.name == "NO_TYPE") "none"
                        else submissionType.name.lowercase(Locale.getDefault())
            }

    fun randomQuiz(withDescription: Boolean, lockAt: String, unlockAt: String, dueAt: String, published: Boolean) =
            CreateQuiz(
                    title = faker.lorem().sentence(),
                    description = if (withDescription) faker.lorem().paragraph() else null,
                    lockAt = if (lockAt.isNotBlank()) lockAt else null,
                    unlockAt = if (unlockAt.isNotBlank()) unlockAt else null,
                    dueAt = if (dueAt.isNotBlank()) dueAt else null,
                    published = published
            )

    fun randomSubmissionComment(fileIds: MutableList<Long>, attemptId: Int): CreateSubmissionComment =
            CreateSubmissionComment(
                    comment = faker.lorem().paragraph(),
                    fileIds = if (fileIds.isNotEmpty()) fileIds else null,
                    attempt = attemptId
            )

    fun randomTextFileName(dir: String) =
        faker.file().fileName(dir, faker.letterify("${faker.lorem().word()}-????????"), "txt", null)

    fun randomTextFileContents() = faker.lorem().paragraph(20)

    fun randomLargeTextFileContents() = faker.lorem().paragraph(100000)

    /** Creates a random page title with a UUID to avoid Canvas URL collisions */
    fun randomPageTitle(): String = faker.gameOfThrones().house() + " " + randomUUID()

    /** Creates a random page body */
    fun randomPageBody(): String = "<p><strong>" + faker.gameOfThrones().quote() + "</strong></p><p>" + faker.lorem().paragraph() + "</p>"

    /** Creates a random Course Group Category name */
    fun randomCourseGroupCategoryName(): String = faker.harryPotter().character()

    fun randomModuleName(): String = faker.lordOfTheRings().location()

    /** Creates random Group */
    fun randomGroup() = CreateGroup(name = faker.harryPotter().location(), description = faker.harryPotter().quote())

    /** Creates course module */
    fun createModule(unlockAt: String? = null) = CreateModule(name = faker.yoda().quote(), unlockAt = unlockAt)

    /** Creates random observer alert threshold for given alert type */
    fun randomThreshold(alertType: String): String {
        return when (alertType) {
            "assignment_grade_low" -> "${faker.number().numberBetween(1, 50)}"
            "assignment_grade_high" -> "${faker.number().numberBetween(51, 99)}"
            "course_grade_low" -> "${faker.number().numberBetween(1, 50)}"
            "course_grade_high" -> "${faker.number().numberBetween(51, 99)}"
            else -> ""
        }
    }
}

data class Name(val firstName: String, val lastName: String) {
    val fullName get() = "$firstName $lastName"
    val sortableName get() = "$lastName, $firstName"
}
