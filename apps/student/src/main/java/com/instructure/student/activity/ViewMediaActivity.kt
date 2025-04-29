package com.instructure.student.activity

import android.content.Context
import android.content.Intent
import com.instructure.interactions.router.Route
import com.instructure.pandautils.activities.BaseViewMediaActivity
import com.instructure.pandautils.analytics.SCREEN_VIEW_VIEW_MEDIA
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.models.EditableFile

@ScreenView(SCREEN_VIEW_VIEW_MEDIA)
class ViewMediaActivity : BaseViewMediaActivity() {
    override fun allowCopyingUrl() = false
    override fun allowEditing() = false
    override fun handleEditing(editableFile: EditableFile) {}

    companion object {
        fun createIntent(context: Context, route: Route): Intent {
            val intent = Intent(context, ViewMediaActivity::class.java)
            intent.putExtras(route.arguments)
            return intent
        }
    }
}
