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
import com.instructure.canvasapi2.managers.*
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.weave.*
import com.instructure.teacher.events.SubmissionUpdatedEvent
import com.instructure.teacher.utils.transformForQuizGrading
import com.instructure.teacher.viewinterface.SpeedGraderView
import instructure.androidblueprint.Presenter
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SpeedGraderPresenter(
        private var courseId: Long,
        private var assignmentId: Long,
        private var submissions: List<GradeableStudentSubmission>,
        private var submissionId: Long,
        private var discussion: DiscussionTopicHeader?
) : Presenter<SpeedGraderView> {

    private var mView: SpeedGraderView? = null
    private var mApiJob: Job? = null

    private var mHasAttemptedLoad = false
    private var newGradebookEnabled = false

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
                newGradebookEnabled = awaitApi<List<String>> {
                    FeaturesManager.getEnabledFeaturesForCourse(courseId, true, it)
                }.contains(FeaturesManager.NEW_GRADEBOOK)

                // Map raw submissions to user id Map<UserId, Submission>
                val userSubmissionMap = rawSubmissions.associateBy { it.userId }
                // Create list of GradeableStudentSubmissions from List<EnrollmentApiModel> (students)
                val allSubmissions = students.map { GradeableStudentSubmission(StudentAssignee(it.user!!), userSubmissionMap[it.user!!.id]) }
                val discussionSubmissions = allSubmissions
                        .filter { it.submission?.discussionEntries?.isNotEmpty() ?: false }
                        .onEach { sub -> sub.submission?.transformForQuizGrading() }

                assignment = discussionAssignment
                submissions = discussionSubmissions
                mView?.onDataSet(discussionAssignment, discussionSubmissions, newGradebookEnabled)
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
            newGradebookEnabled = data.third.contains(FeaturesManager.NEW_GRADEBOOK)

            if (submissionId > 0 && submissions.isEmpty()) {
                // We don't have all the data we need (we came from a push notification), get all the stuffs first
                val submission = awaitApi<Submission> { SubmissionManager.getSingleSubmission(course.id, assignment.id, submissionId, it, false) }
                val user = awaitApi<User> { UserManager.getUser(submissionId, it, false) }
                submissions = listOf(GradeableStudentSubmission(StudentAssignee(user), submission))
            }

            mView?.onDataSet(assignment, submissions, newGradebookEnabled)
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

    override fun onViewAttached(view: SpeedGraderView): Presenter<*> {
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
