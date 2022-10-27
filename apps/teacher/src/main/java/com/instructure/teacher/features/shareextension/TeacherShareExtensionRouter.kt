package com.instructure.teacher.features.shareextension

import android.content.Context
import android.content.Intent
import com.instructure.pandautils.features.shareextension.ShareExtensionRouter
import java.util.*

class TeacherShareExtensionRouter : ShareExtensionRouter {

    override fun routeToProgressScreen(context: Context, workerId: UUID): Intent {
        return Intent()
    }
}