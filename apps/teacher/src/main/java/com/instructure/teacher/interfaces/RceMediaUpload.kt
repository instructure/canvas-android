package com.instructure.teacher.interfaces

import android.app.Activity
import android.net.Uri
import com.instructure.canvasapi2.utils.weave.WeaveJob

// Marks presenters that will upload content from the RCE
interface RceMediaUploadPresenter {
    var rceImageUploadJob: WeaveJob?
    fun uploadRceImage(imageUri: Uri, activity: Activity)
}

// Counterpart to the presenters
interface RceMediaUploadView {
    fun insertImageIntoRCE(text: String, alt: String)
}