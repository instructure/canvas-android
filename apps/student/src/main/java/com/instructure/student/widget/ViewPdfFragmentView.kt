package com.instructure.student.widget

import android.net.Uri
import instructure.androidblueprint.FragmentViewInterface

interface ViewPdfFragmentView : FragmentViewInterface {
    fun onLoadingStarted()
    fun onLoadingProgress(progress: Float)
    fun onLoadingFinished(fileUri: Uri)
    fun onLoadingError()
}
