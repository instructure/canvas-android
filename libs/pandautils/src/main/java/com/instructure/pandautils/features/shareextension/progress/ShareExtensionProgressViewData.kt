package com.instructure.pandautils.features.shareextension.progress

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.instructure.pandautils.R
import com.instructure.pandautils.features.file.upload.FileUploadType
import com.instructure.pandautils.features.shareextension.progress.itemviewmodels.FileProgressItemViewModel

data class ShareExtensionProgressViewData(
    @get:Bindable var items: List<FileProgressItemViewModel>,
    val dialogTitle: String,
    @get:Bindable var subtitle: String?,
    @get:Bindable var maxSize: String,
    @get:Bindable var progressInt: Int,
    @get:Bindable var percentage: String,
    @get:Bindable var currentSize: String,
    @get:Bindable var failed: Boolean
) : BaseObservable()

enum class FileProgressStatus(@ColorRes val tint: Int, @DrawableRes val drawable: Int) {
    IN_PROGRESS(R.color.backgroundSuccess, R.drawable.ic_checkmark),
    UPLOADED(R.color.backgroundSuccess, R.drawable.ic_checkmark),
    FAILED(R.color.textDarkest, R.drawable.ic_close)
}

data class FileProgressViewData(
    val name: String,
    val size: String,
    @get:Bindable @DrawableRes var icon: Int,
    @get:Bindable var status: FileProgressStatus,
    val onStatusIconClick: (FileProgressViewData) -> Unit
) : BaseObservable() {
    @Bindable
    @ColorRes
    fun getIconTint() = if (status == FileProgressStatus.FAILED) R.color.textDanger else R.color.textDarkest
}

sealed class ShareExtensionProgressAction {
    object Close : ShareExtensionProgressAction()
    data class CancelUpload(val title: String, val message: String) : ShareExtensionProgressAction()
    data class ShowSuccessDialog(val fileUploadType: FileUploadType) : ShareExtensionProgressAction()
}