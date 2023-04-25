/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.student.features.offline.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.PageAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.pandautils.room.appdatabase.daos.MediaCommentDao
import com.instructure.pandautils.room.appdatabase.entities.MediaCommentEntity
import com.instructure.pandautils.room.offline.daos.*
import com.instructure.pandautils.room.offline.entities.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class OfflineSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val courseApi: CourseAPI.CoursesInterface,
    private val userApi: UserAPI.UsersInterface,
    private val pageApi: PageAPI.PagesInterface,
    private val assignmentApi: AssignmentAPI.AssignmentInterface,
    private val courseSyncSettingsDao: CourseSyncSettingsDao,
    private val userDao: UserDao,
    private val courseDao: CourseDao,
    private val enrollmentDao: EnrollmentDao,
    private val gradingPeriodDao: GradingPeriodDao,
    private val courseGradingPeriodDao: CourseGradingPeriodDao,
    private val tabDao: TabDao,
    private val termDao: TermDao,
    private val gradesDao: GradesDao,
    private val sectionDao: SectionDao,
    private val pageDao: PageDao,
    private val assignmentGroupDao: AssignmentGroupDao,
    private val assignmentDao: AssignmentDao,
    private val rubricSettingsDao: RubricSettingsDao,
    private val mediaCommentDao: MediaCommentDao,
    private val groupDao: GroupDao,
    private val submissionDao: SubmissionDao,
    private val plannerOverrideDao: PlannerOverrideDao,
    private val discussionTopicHeaderDao: DiscussionTopicHeaderDao,
    private val discussionParticipantDao: DiscussionParticipantDao
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val courses = courseSyncSettingsDao.findAll()
        courses.forEach { courseSettings ->
            fetchCourseDetails(courseSettings.courseId)
            if (courseSettings.pages) {
                fetchPages(courseSettings.courseId)
            }
            if (courseSettings.assignments || courseSettings.pages) {
                fetchAssignments(courseSettings.courseId)
            }
        }
        return Result.success()
    }

    private suspend fun fetchPages(courseId: Long) {
        val params = RestParams(isForceReadFromNetwork = true)
        val pages = pageApi.getFirstPagePages(courseId, "courses", params).depaginate { nextUrl ->
            pageApi.getNextPagePagesList(nextUrl, params)
        }.dataOrThrow

        val entities = pages.map {
            PageEntity(it, courseId)
        }

        pageDao.insert(*entities.toTypedArray())
    }

    private suspend fun fetchAssignments(courseId: Long) {
        val restParams = RestParams(isForceReadFromNetwork = true)
        val assignmentGroups = assignmentApi.getFirstPageAssignmentGroupListWithAssignments(courseId, restParams)
            .depaginate { nextUrl ->
                assignmentApi.getNextPageAssignmentGroupListWithAssignments(nextUrl, restParams)
            }.dataOrThrow

        assignmentGroups.forEach { assignmentGroup ->
            assignmentGroupDao.insert(AssignmentGroupEntity(assignmentGroup))

            assignmentGroup.assignments.forEach { assignment ->
                val rubricSettingsId =
                    assignment.rubricSettings?.let { rubricSettingsDao.insert(RubricSettingsEntity(it)) }
                val submissionId = assignment.submission?.let { submission ->
                    insertSubmission(submission)
                }
                val plannerOverrideId = assignment.plannerOverride?.let { plannerOverride ->
                    plannerOverrideDao.insert(PlannerOverrideEntity(plannerOverride))
                }

                val discussionTopicHeaderId = assignment.discussionTopicHeader?.let { insertDiscussion(it) }

                val assignmentEntity = AssignmentEntity(
                    assignment = assignment,
                    rubricSettingsId = rubricSettingsId,
                    submissionId = submissionId,
                    discussionTopicHeaderId = discussionTopicHeaderId,
                    plannerOverrideId = plannerOverrideId,
                )

                assignmentDao.insert(assignmentEntity)
            }
        }
    }

    private suspend fun insertSubmission(submission: Submission): Long {
        val groupId = submission.group?.let { group -> groupDao.insert(GroupEntity(group)) }
        submission.mediaComment?.let { mediaComment ->
            mediaCommentDao.insert(
                MediaCommentEntity(
                    mediaComment
                )
            )
        }
        if (submission.userId != 0L) {
            val user = submission.user ?: userApi.getUser(
                submission.userId,
                RestParams(isForceReadFromNetwork = true)
            ).dataOrNull

            user?.let { userDao.insert(UserEntity(it)) }
        }

        if (submission.graderId != 0L) {
            val grader = userApi.getUser(submission.graderId, RestParams(isForceReadFromNetwork = true)).dataOrNull
            grader?.let { userDao.insert(UserEntity(it)) }
        }

        submission.submissionHistory.forEach { submissionHistoryItem ->
            submissionHistoryItem?.let { insertSubmission(it) }
        }

        return submissionDao.insert(SubmissionEntity(submission, groupId, submission.mediaComment?.mediaId))
    }

    private suspend fun insertDiscussion(discussionTopicHeader: DiscussionTopicHeader): Long {
        discussionTopicHeader.author?.let { discussionParticipantDao.insert(DiscussionParticipantEntity(it)) }
        return discussionTopicHeaderDao.insert(DiscussionTopicHeaderEntity(discussionTopicHeader))
    }

    private suspend fun fetchCourseDetails(courseId: Long) {
        val params = RestParams(isForceReadFromNetwork = true)
        val course = courseApi.getFullCourseContent(courseId, params).dataOrThrow

        course.term?.let {
            termDao.insert(TermEntity(it))
        }

        courseDao.insert(CourseEntity(course))

        course.enrollments?.forEach { enrollment ->
            if (enrollment.userId != 0L) {
                val user = enrollment.user ?: userApi.getUser(
                    enrollment.userId,
                    RestParams(isForceReadFromNetwork = true)
                ).dataOrThrow
                userDao.insert(UserEntity(user))
            }

            enrollment.observedUser?.let { observedUser ->
                userDao.insert(UserEntity(observedUser))
            }

            val enrollmentId = enrollmentDao.insert(
                EnrollmentEntity(
                    enrollment,
                    courseId = courseId,
                    observedUserId = enrollment.observedUser?.id
                )
            )
            enrollment.grades?.let { grades -> gradesDao.insert(GradesEntity(grades, enrollmentId)) }
        }

        course.gradingPeriods?.forEach { gradingPeriod ->
            gradingPeriodDao.insert(GradingPeriodEntity(gradingPeriod))
            courseGradingPeriodDao.insert(CourseGradingPeriodEntity(courseId, gradingPeriod.id))
        }

        course.sections.forEach { section ->
            sectionDao.insert(SectionEntity(section))
        }

        course.tabs?.forEach { tab ->
            tabDao.insert(TabEntity(tab, courseId))
        }
    }
}