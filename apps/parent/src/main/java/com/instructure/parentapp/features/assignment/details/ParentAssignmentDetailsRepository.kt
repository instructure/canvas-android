package com.instructure.parentapp.features.assignment.details

import androidx.lifecycle.LiveData
import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.QuizAPI
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Quiz
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsRepository
import com.instructure.pandautils.room.appdatabase.daos.ReminderDao
import com.instructure.pandautils.room.appdatabase.entities.ReminderEntity

class ParentAssignmentDetailsRepository(
    private val coursesApi: CourseAPI.CoursesInterface,
    private val assignmentApi: AssignmentAPI.AssignmentInterface,
    private val quizApi: QuizAPI.QuizInterface,
    private val submissionApi: SubmissionAPI.SubmissionInterface,
    private val reminderDao: ReminderDao
): AssignmentDetailsRepository {
    override suspend fun getCourseWithGrade(courseId: Long, forceNetwork: Boolean): Course {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return coursesApi.getCourseWithGrade(courseId, params).dataOrThrow
    }

    override suspend fun getAssignment(
        isObserver: Boolean,
        assignmentId: Long,
        courseId: Long,
        forceNetwork: Boolean
    ): Assignment {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return assignmentApi.getAssignmentIncludeObservees(courseId, assignmentId, params).dataOrThrow.toAssignmentForObservee() ?: throw IllegalStateException("Assignment not found")
    }

    override suspend fun getQuiz(courseId: Long, quizId: Long, forceNetwork: Boolean): Quiz {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return quizApi.getQuiz(courseId, quizId, params).dataOrThrow
    }

    override suspend fun getExternalToolLaunchUrl(
        courseId: Long,
        externalToolId: Long,
        assignmentId: Long,
        forceNetwork: Boolean
    ): LTITool? {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return assignmentApi.getExternalToolLaunchUrl(courseId, externalToolId, assignmentId, restParams = params).dataOrThrow
    }

    override suspend fun getLtiFromAuthenticationUrl(url: String, forceNetwork: Boolean): LTITool? {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return submissionApi.getLtiFromAuthenticationUrl(url, params).dataOrThrow
    }

    override fun getRemindersByAssignmentIdLiveData(
        userId: Long,
        assignmentId: Long
    ): LiveData<List<ReminderEntity>> {
        return reminderDao.findByAssignmentIdLiveData(userId, assignmentId)
    }

    override suspend fun deleteReminderById(id: Long) {
        reminderDao.deleteById(id)
    }

    override suspend fun addReminder(
        userId: Long,
        assignment: Assignment,
        text: String,
        time: Long
    ): Long {
        return reminderDao.insert(
            ReminderEntity(
                userId = userId,
                assignmentId = assignment.id,
                htmlUrl = assignment.htmlUrl.orEmpty(),
                name = assignment.name.orEmpty(),
                text = text,
                time = time
            )
        )
    }

    override fun isOnline(): Boolean = true
}