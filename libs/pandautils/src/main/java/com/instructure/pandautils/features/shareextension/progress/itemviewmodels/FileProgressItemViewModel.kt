package com.instructure.pandautils.features.shareextension.progress.itemviewmodels

import com.instructure.pandautils.R
import com.instructure.pandautils.features.shareextension.progress.FileProgressViewData
import com.instructure.pandautils.mvvm.ItemViewModel

class FileProgressItemViewModel(
    val data: FileProgressViewData
) : ItemViewModel {
    override val layoutId: Int = R.layout.item_file_progress
}