/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.student.features.assignments.details

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.pandautils.BR
import com.instructure.pandautils.features.assignmentdetails.AssignmentDetailsAttemptItemViewModel
import com.instructure.pandautils.features.assignmentdetails.AssignmentDetailsAttemptViewData
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsSubmissionHandler
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsViewData
import com.instructure.pandautils.utils.toFormattedString
import com.instructure.pandautils.utils.toast
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.getVideoUri
import com.instructure.student.mobius.assignmentDetails.uploadAudioRecording
import com.instructure.student.mobius.common.ui.SubmissionHelper
import com.instructure.student.room.StudentDb
import com.instructure.student.room.entities.CreateSubmissionEntity
import com.instructure.student.util.getStudioLTITool
import java.io.File
import java.util.Date

class StudentAssignmentDetailsSubmissionHandler(
    private val submissionHelper: SubmissionHelper,
    private val studentDb: StudentDb
) : AssignmentDetailsSubmissionHandler {
    override var isUploading: Boolean = false
    override var lastSubmissionAssignmentId: Long? = null
    override var lastSubmissionSubmissionType: String? = null
    override var lastSubmissionIsDraft: Boolean = false
    override var lastSubmissionEntry: String? = null

    private var submissionLiveData: LiveData<List<CreateSubmissionEntity>>? = null

    private var submissionObserver: Observer<List<CreateSubmissionEntity>>? = null

    override fun addAssignmentSubmissionObserver(
        assignmentId: Long,
        userId: Long,
        resources: Resources,
        data: MutableLiveData<AssignmentDetailsViewData>,
        refreshAssignment: () -> Unit,
    ) {
        submissionLiveData = studentDb.submissionDao().findSubmissionsByAssignmentIdLiveData(assignmentId, userId)

        setupObserver(resources, data, refreshAssignment)

        submissionObserver?.let { observer ->
            submissionLiveData?.observeForever(observer)
        }
    }

    override fun removeAssignmentSubmissionObserver() {
        submissionObserver?.let { observer ->
            submissionLiveData?.removeObserver(observer)
        }
    }

    override fun uploadAudioSubmission(context: Context?, course: Course?, assignment: Assignment?, file: File?) {
        if (context != null && file != null && assignment != null && course != null) {
            uploadAudioRecording(submissionHelper, file, assignment, course)
        } else {
            context?.let {
                context.toast(context.getString(R.string.audioRecordingError))
            }
        }
    }

    override fun getVideoUri(fragment: FragmentActivity): Uri? = fragment.getVideoUri()

    override suspend fun getStudioLTITool(assignment: Assignment, courseId: Long?): LTITool? {
        return if (assignment.getSubmissionTypes().contains(Assignment.SubmissionType.ONLINE_UPLOAD)) {
            courseId?.getStudioLTITool()?.dataOrNull
        } else null
    }

    private fun setupObserver(
        resources: Resources,
        data: MutableLiveData<AssignmentDetailsViewData>,
        refreshAssignment: () -> Unit,
    ) {
        submissionObserver = Observer<List<CreateSubmissionEntity>> { submissions ->
                val submission = submissions.lastOrNull()
                lastSubmissionAssignmentId = submission?.assignmentId
                lastSubmissionSubmissionType = submission?.submissionType
                lastSubmissionIsDraft = submission?.isDraft ?: false
                lastSubmissionEntry = submission?.submissionEntry

                val attempts = data.value?.attempts
                submission?.let { dbSubmission ->
                    val isDraft = dbSubmission.isDraft
                    data.value?.hasDraft = isDraft
                    data.value?.notifyPropertyChanged(BR.hasDraft)

                    val dateString = (dbSubmission.lastActivityDate?.toInstant()?.toEpochMilli()?.let { Date(it) } ?: Date()).toFormattedString()
                    if (!isDraft && !isUploading) {
                        isUploading = true
                        data.value?.attempts = attempts?.toMutableList()?.apply {
                            add(
                                0, AssignmentDetailsAttemptItemViewModel(
                                    AssignmentDetailsAttemptViewData(
                                        resources.getString(R.string.attempt, attempts.size + 1),
                                        dateString,
                                        isUploading = true
                                    )
                                )
                            )
                        }.orEmpty()
                        data.value?.notifyPropertyChanged(BR.attempts)
                    }
                    if (isUploading && submission.errorFlag) {
                        data.value?.attempts = attempts?.toMutableList()?.apply {
                            if (isNotEmpty()) removeFirst()
                            add(0, AssignmentDetailsAttemptItemViewModel(
                                AssignmentDetailsAttemptViewData(
                                    resources.getString(R.string.attempt, attempts.size),
                                    dateString,
                                    isFailed = true
                                )
                            )
                            )
                        }.orEmpty()
                        data.value?.notifyPropertyChanged(BR.attempts)
                    }
                } ?: run {
                    if (isUploading) {
                        isUploading = false
                        refreshAssignment()
                    }
                }
        }
    }
}