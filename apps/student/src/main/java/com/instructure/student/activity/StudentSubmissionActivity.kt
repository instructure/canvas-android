/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
 */    package com.instructure.student.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.instructure.student.view.StudentSubmissionView
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradeableStudentSubmission
import com.instructure.canvasapi2.utils.ApiPrefs
import com.pspdfkit.preferences.PSPDFKitPreferences

class StudentSubmissionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the PDF author, but only if it hasn't been set yet
        if (!PSPDFKitPreferences.get(this).isAnnotationCreatorSet) {
            PSPDFKitPreferences.get(this).setAnnotationCreator(ApiPrefs.user?.name)
        }
        val bundle = intent.extras

        setContentView(StudentSubmissionView(this, bundle.get(COURSE) as Course, bundle.get(SUBMISSION) as GradeableStudentSubmission, bundle.get(ASSIGNMENT) as Assignment, bundle.getInt(ATTACHMENT_POSITION)))
    }

    companion object {
        private val ASSIGNMENT = "assignment"
        private val COURSE = "course"
        private val SUBMISSION = "submission"
        private val ATTACHMENT_POSITION = "attachmentPosition"
        @JvmStatic
        fun createIntent(context: Context, course: Course, assignment: Assignment, submission: GradeableStudentSubmission, attachmentPosition: Int = 0): Intent {
            val intent = Intent(context, StudentSubmissionActivity::class.java)
            val extras = Bundle()
            extras.putParcelable(ASSIGNMENT, assignment)
            extras.putParcelable(COURSE, course)
            extras.putParcelable(SUBMISSION, submission)
            extras.putInt(ATTACHMENT_POSITION, attachmentPosition)
            intent.putExtras(extras)
            return intent
        }
    }
}
