package com.instructure.pandautils.features.assignments.details

import androidx.lifecycle.LiveData
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Quiz
import com.instructure.pandautils.room.appdatabase.entities.ReminderEntity

interface AssignmentDetailsRepository {
    suspend fun getCourseWithGrade(courseId: Long, forceNetwork: Boolean): Course

    suspend fun getAssignment(
        isObserver: Boolean,
        assignmentId: Long,
        courseId: Long,
        forceNetwork: Boolean
    ): Assignment

    suspend fun getQuiz(courseId: Long, quizId: Long, forceNetwork: Boolean): Quiz

    suspend fun getExternalToolLaunchUrl(
        courseId: Long,
        externalToolId: Long,
        assignmentId: Long,
        forceNetwork: Boolean
    ): LTITool?

    suspend fun getLtiFromAuthenticationUrl(url: String, forceNetwork: Boolean): LTITool?

    fun getRemindersByAssignmentIdLiveData(
        userId: Long,
        assignmentId: Long
    ): LiveData<List<ReminderEntity>>

    suspend fun deleteReminderById(id: Long)

    suspend fun addReminder(userId: Long, assignment: Assignment, text: String, time: Long): Long

    fun isOnline(): Boolean
}