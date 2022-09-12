package com.instructure.pandautils.features.shareextension.progress

import android.content.res.Resources
import androidx.annotation.DrawableRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.hasKeyWithValueOfType
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.pandautils.BR
import com.instructure.pandautils.R
import com.instructure.pandautils.features.file.upload.worker.FileUploadWorker
import com.instructure.pandautils.features.shareextension.progress.itemviewmodels.FileProgressItemViewModel
import com.instructure.pandautils.utils.fromJson
import com.instructure.pandautils.utils.humanReadableByteCount
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

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

    private var workerId: UUID? = null

    private val observer = Observer<WorkInfo> {
        when (it.state) {
            WorkInfo.State.SUCCEEDED -> {
                _events.postValue(Event((ShareExtensionProgressAction.ShowSuccessDialog)))
            }
            WorkInfo.State.RUNNING -> {
                updateViewData(it.progress)
            }
            WorkInfo.State.FAILED -> {
                _events.postValue(Event(ShareExtensionProgressAction.ShowErrorDialog))
            }
        }
    }

    fun setUUID(uuid: UUID) {
        this.workerId = uuid
        _state.postValue(ViewState.Loading)
        workManager.getWorkInfoByIdLiveData(uuid).observeForever(observer)
    }

    override fun onCleared() {
        super.onCleared()
        workerId?.let {
            workManager.getWorkInfoByIdLiveData(it).removeObserver(observer)
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
                                it.size.humanReadableByteCount(),
                                getIconDrawableRes(it.contentType),
                                uploadedMap.containsKey(it.name)
                            )
                        }

                viewData = ShareExtensionProgressViewData(
                    items = itemViewData.map { FileProgressItemViewModel(it) },
                    dialogTitle = if (assignmentName == null) resources.getString(R.string.fileUpload) else resources.getString(
                        R.string.submission
                    ),
                    subtitle = if (assignmentName.isNullOrEmpty()) resources.getString(R.string.fileUploadProgressSubtitle) else resources.getString(
                        R.string.submissionProgressSubtitle,
                        assignmentName
                    ),
                    maxSize = maxSize.humanReadableByteCount(),
                    currentSize = currentSize.humanReadableByteCount(),
                    progressInt = ((currentSize.toDouble() / maxSize.toDouble()) * 100.0).toInt(),
                    percentage = "${String.format("%.1f", currentSize.toDouble() / maxSize.toDouble() * 100.0)}%"
                )
                viewData?.let {
                    _data.postValue(it)
                }
            } else {
                viewData?.apply {
                    this.currentSize = currentSize.humanReadableByteCount()
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
        _events.postValue(Event(ShareExtensionProgressAction.Close))
    }

    @DrawableRes private fun getIconDrawableRes(contentType: String): Int {
        return when {
            contentType.contains("image") -> R.drawable.ic_image
            contentType.contains("video") -> R.drawable.ic_media
            contentType.contains("pdf") -> R.drawable.ic_pdf
            else -> R.drawable.ic_attachment
        }
    }

    fun cancelUpload(workerId: UUID) {
        workManager.cancelWorkById(workerId)
    }

    fun cancelClicked() {
        _events.postValue(Event(ShareExtensionProgressAction.CancelUpload))
    }
}