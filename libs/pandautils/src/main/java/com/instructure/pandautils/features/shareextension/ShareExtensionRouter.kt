package com.instructure.pandautils.features.shareextension

import android.content.Context
import android.content.Intent
import java.util.*

interface ShareExtensionRouter {

    fun routeToProgressScreen(context: Context, workerId: UUID): Intent
}