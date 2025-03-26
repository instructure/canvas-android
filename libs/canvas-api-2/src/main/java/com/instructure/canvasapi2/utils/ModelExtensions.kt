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
 */
package com.instructure.canvasapi2.utils

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.instructure.canvasapi2.R
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.GradingSchemeRow
import com.instructure.canvasapi2.models.MediaComment
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.RemoteFile
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.type.EnrollmentType
import java.util.Date
import java.util.regex.Pattern

private const val WORKFLOW_STATE_DELETED = "deleted"

fun Assignment.SubmissionType.prettyPrint(context: Context): String
        = Assignment.submissionTypeToPrettyPrintString(this, context) ?: ""

/**
 * The global course name. This may be different from [Course.name] which could be the the user's
 * nickname for this course.
 * */
var Course.globalName: String
    get() = originalName?.takeUnless(String::isNullOrBlank) ?: name
    set(value) {
        if (originalName.isNullOrBlank()) name = value else originalName = value
    }

/**
 *  If the term is concluded, it can't be favorited. So if it was favorited, and then the term concluded, we don't want it favorited now.
 *  We also don't want it included in the list of favorite courses
 *
 *  BUT.....it can be overridden by a section, so we need to check that section's end date too
 */
fun Course.isValidTerm(): Boolean = term?.endDate?.after(Date()) ?: true || hasValidSection()

/* If there is no valid section, we don't want this value to override the term's end date */
fun Course.hasValidSection(): Boolean = sections.any { it.endDate?.after(Date()) ?: false }
fun Course.hasActiveEnrollment(): Boolean = enrollments?.any { it.enrollmentState == EnrollmentAPI.STATE_ACTIVE } ?: false
fun Course.isInvited(): Boolean = enrollments?.any { it.enrollmentState == EnrollmentAPI.STATE_INVITED } ?: false
fun Course.isCompleted(): Boolean = enrollments?.any { it.enrollmentState == EnrollmentAPI.STATE_COMPLETED } ?: false
fun Course.isEnrollmentDeleted(): Boolean = enrollments?.all { it.enrollmentState == EnrollmentAPI.STATE_DELETED } ?: false
fun Course.isCreationPending(): Boolean = enrollments?.any { it.enrollmentState == EnrollmentAPI.STATE_CREATION_PENDING } ?: false
fun Course.isNotDeleted(): Boolean = workflowState != Course.WorkflowState.DELETED
fun Course.isPublished(): Boolean = workflowState != Course.WorkflowState.UNPUBLISHED

fun ModuleItem.isLocked(): Boolean = moduleDetails?.lockedForUser ?: false || moduleDetails?.lockExplanation.isValid() && moduleDetails?.lockDate?.before(Date()) == true && moduleDetails.unlockDate?.after(Date()) == true

fun MediaComment.asAttachment() = Attachment().also {
    it.contentType = contentType ?: ""
    it.displayName = displayName
    it.filename = _fileName
    it.url = url
}

inline fun <reified T : Parcelable> T.parcelCopy(): T {
    val parcel = Parcel.obtain()
    parcel.writeParcelable(this, 0)
    parcel.setDataPosition(0)
    val copy = parcel.readParcelable<T>(T::class.java.classLoader)!!
    parcel.recycle()
    return copy
}

@JvmName("mapToAttachmentRemoteFile")
fun RemoteFile.mapToAttachment(): Attachment = Attachment(
        id = id,
        contentType = contentType,
        filename = fileName,
        displayName = displayName,
        createdAt = createdAt.toDate(),
        size = size,
        previewUrl = previewUrl,
        thumbnailUrl = thumbnailUrl,
        url = url
)

val EnrollmentType?.displayText: String
    get() = ContextKeeper.appContext.getText(
        when (this) {
            EnrollmentType.StudentEnrollment -> R.string.enrollmentTypeStudents
            EnrollmentType.TeacherEnrollment -> R.string.enrollmentTypeTeachers
            EnrollmentType.ObserverEnrollment -> R.string.enrollmentTypeObservers
            EnrollmentType.TaEnrollment -> R.string.enrollmentTypeTeachingAssistants
            EnrollmentType.DesignerEnrollment -> R.string.enrollmentTypeDesigners
            else -> R.string.enrollmentTypeUnknown
        }
    ).toString()

val Enrollment.displayType: CharSequence
    get() = ContextKeeper.appContext.getText(
        when {
            isStudent -> R.string.enrollmentTypeStudent
            isTeacher -> R.string.enrollmentTypeTeacher
            isObserver -> R.string.enrollmentTypeObserver
            isTA -> R.string.enrollmentTypeTeachingAssistant
            isDesigner -> R.string.enrollmentTypeDesigner
            else -> R.string.enrollmentTypeUnknown
        }
    )

/**
 * For finding img tags in Discussion entries
 *
 * WARNING: This makes an api call synchronously - Only use this inside a background thread!
 */
fun getImageReplacementList(msg: String): List<Pair<String, IntRange>> {
    val imageTagPattern = Pattern.compile("""<img.*>""")
    val imageTagMatcher = imageTagPattern.matcher(msg)
    val imageSrcPattern = Pattern.compile("""src\s*=\s*"[^"]+"""")

// Replacements need to be applied in reverse order so the start/end indices remain accurate
    val mappedReplacements = mutableListOf<Pair<String, IntRange>>()

    while (imageTagMatcher.find()) {
        val imageTag = imageTagMatcher.group()
        if (imageTag.contains(ApiPrefs.domain)) {
            // This is a canvas-based image
            if (!imageTag.contains("verifier")) {
                // This image doesn't have a verifier param, so we need to make this an authenticated url
                val imageSourceMatcher = imageSrcPattern.matcher(imageTag)
                if (imageSourceMatcher.find()) {
                    // Found the source tag
                    val src = imageSourceMatcher.group()
                    val url = src.subSequence("src=\"".length, src.length-1).toString()
                    // Get an authenticated URL
                    val authenticatedSessionURL = OAuthManager.getAuthenticatedSessionSynchronous(url) ?: url

                    // Replace the source in the image tag
                    val imageReplacement = imageTag.replaceRange(imageSourceMatcher.start(), imageSourceMatcher.end(), """src="$authenticatedSessionURL"""")

                    // Create a mapping for the img tag replacement
                    mappedReplacements.add(Pair(imageReplacement, IntRange(imageTagMatcher.start(), imageTagMatcher.end())))
                }
            }
        }
    }

    return mappedReplacements
}

/**
 * Replaces the text within the IntRange with the String passed in (Pair parameters)
 */
fun replaceImgTags(replacementList: List<Pair<String, IntRange>>, msg: String): String {
    var newMsg = msg
    for (replacement in replacementList.asReversed()) {
        // Loop through the replacements in reverse
        newMsg = newMsg.replaceRange(replacement.second.start, replacement.second.last, replacement.first)
    }

    return newMsg
}

inline fun <T> List<T>.filterWithQuery(query: String, predicate: (T) -> String?): List<T> {
    return if (query.isBlank()) this else filter { predicate(it)?.contains(query, true) == true }
}

fun Assignment.toScheduleItem() : ScheduleItem {
    val id = this.courseId
    return ScheduleItem(
            itemId = id.toString(),
            title = name,
            startAt = dueAt,
            itemType = ScheduleItem.Type.TYPE_ASSIGNMENT,
            description = description,
            submissionTypes = getSubmissionTypes(),
            pointsPossible = pointsPossible,
            htmlUrl = htmlUrl,
            quizId = quizId,
            discussionTopicHeader = discussionTopicHeader,
            assignment = this,
            lockedModuleName = lockInfo?.lockedModuleName
    ).apply { courseId = id }
}

fun convertScoreToLetterGrade(score: Double, maxScore: Double, gradingScheme: List<GradingSchemeRow>): String {
    if (maxScore == 0.0) return ""
    val percent = (score / maxScore)
    return convertPercentScoreToLetterGrade(percent, gradingScheme)
}

fun convertPercentScoreToLetterGrade(percentScore: Double, gradingScheme: List<GradingSchemeRow>): String {
    if (gradingScheme.isEmpty()) return ""
    val grade = gradingScheme.firstOrNull { percentScore >= it.value } ?: gradingScheme.last()
    return grade.name
}

fun convertPercentToPointBased(percentScore: Double, scalingFactor: Double): String {
    return String.format("%.2f / %.2f", scalingFactor * (percentScore / 100.0), scalingFactor)
}