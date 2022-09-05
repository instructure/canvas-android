package com.instructure.pandautils.features.shareextension.progress

import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.hasKeyWithValueOfType
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.pandautils.BR
import com.instructure.pandautils.R
import com.instructure.pandautils.features.file.upload.worker.FileUploadWorker
import com.instructure.pandautils.features.shareextension.ShareExtensionAction
import com.instructure.pandautils.features.shareextension.progress.itemviewmodels.FileProgressItemViewModel
import com.instructure.pandautils.features.shareextension.target.ShareExtensionTargetAction
import com.instructure.pandautils.fromJson
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject
import kotlin.math.ln
import kotlin.math.pow

@HiltViewModel
class ShareExtensionProgressDialogViewModel @Inject constructor(
    private val workManager: WorkManager,
    private val resources: Resources
) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<ShareExtensionProgressViewData>
        get() = _data
    private val _data = MutableLiveData<ShareExtensionProgressViewData>()

    val events: LiveData<Event<ShareExtensionProgressAction>>
        get() = _events
    private val _events = MutableLiveData<Event<ShareExtensionProgressAction>>()

    private var viewData: ShareExtensionProgressViewData? = null

    private var itemViewData: List<FileProgressViewData> = emptyList()

    fun setUUID(uuid: UUID) {
        _state.postValue(ViewState.Loading)
        workManager.getWorkInfoByIdLiveData(uuid).observeForever {
            when (it.state) {
                WorkInfo.State.SUCCEEDED -> {
                    _events.postValue(Event((ShareExtensionProgressAction.ShowSuccessDialog)))
                }
                WorkInfo.State.RUNNING -> {
                    updateViewData(it.progress)
                }
            }
        }
    }

    private fun updateViewData(progress: Data) {
        if (allDataPresent(progress)) {
            _state.postValue(ViewState.Success)

            val maxSize = progress.getLong(FileUploadWorker.PROGRESS_DATA_FULL_SIZE, 1L)
            val currentSize = progress.getLong(FileUploadWorker.PROGRESS_DATA_UPLOADED_SIZE, 0L)
            val assignmentName =
                if (progress.hasKeyWithValueOfType<String>(FileUploadWorker.PROGRESS_DATA_ASSIGNMENT_NAME)) {
                    progress.getString(FileUploadWorker.PROGRESS_DATA_ASSIGNMENT_NAME)
                } else null

            val uploadedMap = progress.getStringArray(FileUploadWorker.PROGRESS_DATA_UPLOADED_FILES).orEmpty()
                .map { it.fromJson<FileSubmitObject>() }
                .associateBy { it.name }

            if (viewData == null) {
                itemViewData =
                    progress.getStringArray(FileUploadWorker.PROGRESS_DATA_FILES_TO_UPLOAD).orEmpty().toList()
                        .map { it.fromJson<FileSubmitObject>() }
                        .map {
                            FileProgressViewData(
                                it.name,
                                humanReadableByteCount(it.size),
                                getIconDrawable(it.contentType),
                                uploadedMap.containsKey(it.name)
                            )
                        }

                viewData = ShareExtensionProgressViewData(
                    items = itemViewData.map { FileProgressItemViewModel(it) },
                    dialogTitle = if (assignmentName == null) resources.getString(R.string.fileUpload) else resources.getString(
                        R.string.submission
                    ),
                    subtitle = if (assignmentName == null) resources.getString(R.string.fileUploadProgressSubtitle) else resources.getString(
                        R.string.submissionProgressSubtitle,
                        assignmentName
                    ),
                    maxSize = humanReadableByteCount(maxSize),
                    currentSize = humanReadableByteCount(currentSize),
                    progressInt = ((currentSize.toDouble() / maxSize.toDouble()) * 100.0).toInt(),
                    percentage = "${String.format("%.1f", currentSize.toDouble() / maxSize.toDouble() * 100.0)}%"
                )
                viewData?.let {
                    _data.postValue(it)
                }
            } else {
                viewData?.apply {
                    this.currentSize = humanReadableByteCount(currentSize)
                    this.progressInt = ((currentSize.toDouble() / maxSize.toDouble()) * 100).toInt()
                    this.percentage =
                        "${String.format("%.1f", currentSize.toDouble() / maxSize.toDouble() * 100.0)}%"
                    uploadedMap.forEach { uploadedEntry ->
                        this.items.find { itemViewModel ->
                            itemViewModel.data.name == uploadedEntry.key
                        }.apply {
                            this?.data?.uploaded = true
                            this?.data?.notifyPropertyChanged(BR.uploaded)
                        }
                    }
                    notifyPropertyChanged(BR.currentSize)
                    notifyPropertyChanged(BR.progressInt)
                    notifyPropertyChanged(BR.percentage)
                }
            }

        }
    }

    private fun allDataPresent(progress: Data): Boolean {
        return progress.hasKeyWithValueOfType<Long>(FileUploadWorker.PROGRESS_DATA_FULL_SIZE) && progress.hasKeyWithValueOfType<Array<String>>(
            FileUploadWorker.PROGRESS_DATA_FILES_TO_UPLOAD
        )
    }

    fun onCloseClicked() {

    }

    private fun getIconDrawable(contentType: String): Drawable {
        return when {
            contentType.contains("image") -> resources.getDrawable(R.drawable.ic_image)
            contentType.contains("video") -> resources.getDrawable(R.drawable.ic_media)
            contentType.contains("pdf") -> resources.getDrawable(R.drawable.ic_pdf)
            else -> resources.getDrawable(R.drawable.ic_attachment)
        }
    }

    private fun humanReadableByteCount(bytes: Long): String {
        val unit = 1024
        if (bytes < unit) return "$bytes B"
        val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
        val pre = "KMGTPE"[exp - 1].toString()
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / unit.toDouble().pow(exp.toDouble()), pre)
    }
}