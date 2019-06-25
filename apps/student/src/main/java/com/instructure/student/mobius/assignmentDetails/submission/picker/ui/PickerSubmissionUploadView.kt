/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.mobius.assignmentDetails.submission.picker.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionUploadEvent
import com.instructure.student.mobius.common.ui.MobiusView
import com.spotify.mobius.functions.Consumer

class PickerSubmissionUploadView(inflater: LayoutInflater, parent: ViewGroup) :
    MobiusView<PickerSubmissionUploadViewState, PickerSubmissionUploadEvent>(0, inflater, parent) {
    override fun onConnect(output: Consumer<PickerSubmissionUploadEvent>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun render(state: PickerSubmissionUploadViewState) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDispose() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun applyTheme() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun launchCamera() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun launchGallery() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun launchVideoRecorder() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun launchAudioRecorder() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun launchSelectFile() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun showBadExtensionDialog(allowedExtensions: List<String>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun showFileErrorMessage(errorMessage: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun closeSubmissionView() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}