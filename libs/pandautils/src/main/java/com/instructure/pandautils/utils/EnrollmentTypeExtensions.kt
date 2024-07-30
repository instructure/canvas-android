package com.instructure.pandautils.utils

import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R

val EnrollmentType?.displayText: String
    get() = ContextKeeper.appContext.getText(
        when (this) {
            EnrollmentType.STUDENTENROLLMENT -> R.string.enrollment_type_students
            EnrollmentType.TEACHERENROLLMENT -> R.string.enrollment_type_teachers
            EnrollmentType.OBSERVERENROLLMENT -> R.string.enrollment_type_observers
            EnrollmentType.TAENROLLMENT -> R.string.enrollment_type_tas
            EnrollmentType.DESIGNERENROLLMENT -> R.string.enrollment_type_course_designer
            else -> R.string.enrollment_type_others
        }
    ).toString()