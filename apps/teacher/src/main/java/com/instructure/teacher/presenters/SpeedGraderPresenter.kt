/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.presenters

import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.EnrollmentManager
import com.instructure.canvasapi2.managers.FeaturesManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.GradeableStudentSubmission
import com.instructure.canvasapi2.models.StudentAssignee
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.awaitApis
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.pandautils.blueprint.Presenter
import com.instructure.pandautils.utils.AssignmentUtils2
import com.instructure.pandautils.utils.orDefault
import com.instructure.teacher.events.SubmissionUpdatedEvent
import com.instructure.teacher.features.assignment.submission.AssignmentSubmissionRepository
import com.instructure.teacher.features.assignment.submission.SubmissionListFilter
import com.instructure.teacher.utils.getState
import com.instructure.teacher.utils.transformForQuizGrading
import com.instructure.teacher.viewinterface.SpeedGraderView
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SpeedGraderPresenter(
    private var courseId: Long,
    private var assignmentId: Long,
    private var submissionId: Long,
    private var discussion: DiscussionTopicHeader?,
    private val repository: AssignmentSubmissionRepository,
    private val filteredSubmissionIds: LongArray,
    private val filter: SubmissionListFilter,
    private val filterValue: Double
) : Presenter<SpeedGraderView> {

    private var submissions: List<GradeableStudentSubmission> = emptyList()

    private var mView: SpeedGraderView? = null
    private var mApiJob: Job? = null

    private var mHasAttemptedLoad = false

    lateinit var assignment: Assignment
    lateinit var course: Course

    fun setupData() {
        // Don't load again if we've already loaded
        if (mHasAttemptedLoad) return else mHasAttemptedLoad = true
        when {
        // Discussion
            discussion != null -> setupDiscussionData(discussion!!)
        // Assignment
            else -> setupAssignmentData()
        }
        mHasAttemptedLoad = true
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun setupDiscussionData(discussion: DiscussionTopicHeader) {
        mApiJob = weave {
            try {
                course = awaitApi { CourseManager.getCourse(courseId, it, false) }
                val (students, rawSubmissions, discussionAssignment) = awaitApis<List<Enrollment>, List<Submission>, Assignment>(
                        // Get all students in the course
                        { EnrollmentManager.getAllEnrollmentsForCourse(course.id, EnrollmentAPI.STUDENT_ENROLLMENT, false, it) },
                        // Get all submissions for the assignment
                        { AssignmentManager.getAllSubmissionsForAssignment(course.id, discussion.assignmentId, true, it) },
                        // Get assignment for the discussion
                        { AssignmentManager.getAssignment(discussion.assignmentId, course.id, false, it) }
                )

                // Map raw submissions to user id Map<UserId, Submission>
                val userSubmissionMap = rawSubmissions.associateBy { it.userId }
                // Create list of GradeableStudentSubmissions from List<EnrollmentApiModel> (students)
                val allSubmissions = students.map { GradeableStudentSubmission(StudentAssignee(it.user!!), userSubmissionMap[it.user!!.id]) }
                val discussionSubmissions = allSubmissions
                        .filter { it.submission?.discussionEntries?.isNotEmpty() ?: false }
                        .onEach { sub -> sub.submission?.transformForQuizGrading() }

                assignment = discussionAssignment
                submissions = discussionSubmissions
                mView?.onDataSet(discussionAssignment, discussionSubmissions)
            } catch (ignore: Throwable) {
                mView?.onErrorSettingData()
            }
        }
    }

    private fun setupAssignmentData() {
        mApiJob = tryWeave {
            val data = awaitApis<Course, Assignment, List<String>>(
                { CourseManager.getCourse(courseId, it, false) },
                { AssignmentManager.getAssignment(assignmentId, courseId, false, it) },
                { FeaturesManager.getEnabledFeaturesForCourse(courseId, true, it) }
            )
            course = data.first
            assignment = data.second
            val allSubmissions = repository.getGradeableStudentSubmissions(assignment, courseId, false)
            submissions = allSubmissions.filter {
                when (filter) {
                    SubmissionListFilter.ALL -> true
                    SubmissionListFilter.LATE -> it.submission?.let { assignment.getState(it, true) in listOf(
                        AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED_LATE, AssignmentUtils2.ASSIGNMENT_STATE_GRADED_LATE) } ?: false
                    SubmissionListFilter.NOT_GRADED -> it.submission?.let { assignment.getState(it, true) in listOf(
                        AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED, AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED_LATE) || !it.isGradeMatchesCurrentSubmission } ?: false
                    SubmissionListFilter.GRADED -> it.submission?.let { assignment.getState(it, true) in listOf(
                        AssignmentUtils2.ASSIGNMENT_STATE_GRADED, AssignmentUtils2.ASSIGNMENT_STATE_GRADED_LATE, AssignmentUtils2.ASSIGNMENT_STATE_GRADED_MISSING, AssignmentUtils2.ASSIGNMENT_STATE_EXCUSED)  && it.isGradeMatchesCurrentSubmission} ?: false
                    SubmissionListFilter.ABOVE_VALUE -> it.submission?.let { it.isGraded && it.score.orDefault() >= filterValue } ?: false
                    SubmissionListFilter.BELOW_VALUE -> it.submission?.let { it.isGraded && it.score.orDefault() < filterValue } ?: false
                    SubmissionListFilter.MISSING -> it.submission?.workflowState == "unsubmitted" || it.submission == null
                }
            }

            if (submissionId > 0 && submissions.isEmpty()) {
                // We don't have all the data we need (we came from a push notification), get all the stuffs first
                val submission = awaitApi<Submission> { SubmissionManager.getSingleSubmission(course.id, assignment.id, submissionId, it, false) }
                val user = awaitApi<User> { UserManager.getUser(submissionId, it, false) }
                submissions = listOf(GradeableStudentSubmission(StudentAssignee(user), submission))
            }

            if (filteredSubmissionIds.isNotEmpty()) {
                submissions = submissions.filter { it.id in filteredSubmissionIds }
            }

            mView?.onDataSet(assignment, submissions)
        } catch {
            mView?.onErrorSettingData()
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false)
    fun updateSubmission(event: SubmissionUpdatedEvent) {
        event.once("SpeedGrader | Assignment ${assignment.id}") { staleSubmission ->
            if (staleSubmission.assignmentId != assignment.id) return@once
            val matchingSubmission = submissions.firstOrNull { it.submission?.id == staleSubmission.id } ?: return@once
            tryWeave {
                val freshSubmission = awaitApi<Submission> { SubmissionManager.getSingleSubmission(course.id, assignment.id, staleSubmission.userId, it, true) }
                matchingSubmission.submission = freshSubmission
            } catch {}
        }
    }

    override fun onViewAttached(view: SpeedGraderView): SpeedGraderPresenter {
        mView = view
        EventBus.getDefault().register(this)
        return this
    }

    override fun onViewDetached() {
        mView = null
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroyed() {
        mView = null
        mApiJob?.cancel()
    }

}
