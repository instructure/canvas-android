package com.instructure.teacher.services

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.utils.pageview.PageViewUploadService
import com.instructure.canvasapi2.utils.pageview.PandataInfo
import com.instructure.teacher.BuildConfig

class TeacherPageViewService : PageViewUploadService() {

    override val appKey = pandataAppKey

    override fun onException(e: Throwable) = FirebaseCrashlytics.getInstance().recordException(e)


    companion object {
        val pandataAppKey = PandataInfo.AppKey(
            "CANVAS_TEACHER_ANDROID",
            "Canvas Teacher for Android - ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        )
    }
}
