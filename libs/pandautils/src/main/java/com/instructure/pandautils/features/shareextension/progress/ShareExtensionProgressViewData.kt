package com.instructure.pandautils.features.shareextension.progress

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.instructure.pandautils.features.file.upload.FileUploadDialogViewData
import com.instructure.pandautils.features.shareextension.progress.itemviewmodels.FileProgressItemViewModel
import com.instructure.pandautils.mvvm.ItemViewModel

data class ShareExtensionProgressViewData(
    val items: List<FileProgressItemViewModel>,
    val dialogTitle: String,
    val subtitle: String,
    val maxSize: String,
    @get:Bindable var progressInt: Int,
    @get:Bindable var percentage: String,
    @get:Bindable var currentSize: String
) : BaseObservable()

data class FileProgressViewData(
    val name: String,
    val size: String,
    val icon: Drawable,
    @get:Bindable var uploaded: Boolean
) : BaseObservable()

sealed class ShareExtensionProgressAction {
    object ShowSuccessDialog : ShareExtensionProgressAction()
}