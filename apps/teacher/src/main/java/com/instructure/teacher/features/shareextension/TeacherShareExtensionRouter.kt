package com.instructure.teacher.features.shareextension

import android.content.Context
import android.content.Intent
import com.instructure.pandautils.features.shareextension.ShareExtensionRouter
import com.instructure.pandautils.features.shareextension.WORKER_ID
import java.util.*

class TeacherShareExtensionRouter : ShareExtensionRouter {

    override fun routeToProgressScreen(context: Context, workerId: UUID): Intent {
        val intent = Intent(context, TeacherShareExtensionActivity::class.java)
        intent.putExtra(WORKER_ID, workerId)
        return intent
    }
}