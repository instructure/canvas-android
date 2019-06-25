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

import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.page.BasePage
import com.instructure.student.R

class PickerSubmissionUploadRenderPage : BasePage(R.id.pickerSubmissionUploadPage) {
    val emptyView by OnViewWithId(R.id.pickerEmptyView)
    val recycler by OnViewWithId(R.id.filePickerRecycler)
    val submitButton by OnViewWithId(R.id.menuSubmit)

    val fabPick by OnViewWithId(R.id.pickFab)
    val fabFile by OnViewWithId(R.id.pickFabFile)
    val fabCamera by OnViewWithId(R.id.pickFabCamera)
    val fabGallery by OnViewWithId(R.id.pickFabGallery)
}