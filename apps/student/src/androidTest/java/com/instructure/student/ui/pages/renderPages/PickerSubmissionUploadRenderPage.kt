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
package com.instructure.student.ui.pages.renderPages

import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.pages.BasePage
import com.instructure.student.R

class PickerSubmissionUploadRenderPage : BasePage(R.id.pickerSubmissionUploadPage) {
    val toolbar by OnViewWithId(R.id.toolbar)
    val emptyView by OnViewWithId(R.id.pickerEmptyView)
    val recycler by OnViewWithId(R.id.filePickerRecycler)
    val submitButton by OnViewWithId(R.id.menuSubmit)
    val loading by OnViewWithId(R.id.fileLoading)

    val sourcesContainer by OnViewWithId(R.id.sourcesContainer)
    val sourcesDivider by OnViewWithId(R.id.sourcesDivider)
    val deviceButton by OnViewWithId(R.id.sourceDevice)
    val cameraButton by OnViewWithId(R.id.sourceCamera)
    val galleryButton by OnViewWithId(R.id.sourceGallery)

    val emptyMessage by OnViewWithId(R.id.message)

    fun assertHasTitle(stringResId: Int) {
        toolbar.check(matches(hasDescendant(withText(stringResId))))
    }
}
