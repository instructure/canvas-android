package com.instructure.pandautils.utils

import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R

val EnrollmentType?.displayText: String
    get() = ContextKeeper.appContext.getText(
        when (this) {
            EnrollmentType.STUDENTENROLLMENT -> R.string.enrollmentTypeStudent
            EnrollmentType.TEACHERENROLLMENT -> R.string.enrollmentTypeTeacher
            EnrollmentType.OBSERVERENROLLMENT -> R.string.enrollmentTypeObserver
            EnrollmentType.TAENROLLMENT -> R.string.enrollmentTypeTeachingAssistant
            EnrollmentType.DESIGNERENROLLMENT -> R.string.enrollmentTypeDesigner
            else -> R.string.enrollmentTypeUnknown
        }
    ).toString()