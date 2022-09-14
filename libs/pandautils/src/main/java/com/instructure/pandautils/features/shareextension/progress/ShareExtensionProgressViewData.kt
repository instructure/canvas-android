package com.instructure.pandautils.features.shareextension.progress

import androidx.annotation.DrawableRes
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.instructure.pandautils.features.file.upload.FileUploadType
import com.instructure.pandautils.features.shareextension.progress.itemviewmodels.FileProgressItemViewModel

data class ShareExtensionProgressViewData(
    val items: List<FileProgressItemViewModel>,
    val dialogTitle: String,
    val subtitle: String?,
    val maxSize: String,
    @get:Bindable var progressInt: Int,
    @get:Bindable var percentage: String,
    @get:Bindable var currentSize: String
) : BaseObservable()

data class FileProgressViewData(
    val name: String,
    val size: String,
    @DrawableRes val icon: Int,
    @get:Bindable var uploaded: Boolean
) : BaseObservable()

sealed class ShareExtensionProgressAction {
    object Close : ShareExtensionProgressAction()
    data class CancelUpload(val title: String, val message: String) : ShareExtensionProgressAction()
    data class ShowSuccessDialog(val fileUploadType: FileUploadType) : ShareExtensionProgressAction()
    data class ShowErrorDialog(val fileUploadType: FileUploadType) : ShareExtensionProgressAction()
}