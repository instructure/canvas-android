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
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.pages.BasePage
import com.instructure.student.R

class MediaSubmissionViewRenderPage : BasePage(R.id.mediaSubmissionView) {
    val mediaProgressBar by OnViewWithId(R.id.mediaProgressBar)
    val prepareMediaButton by WaitForViewWithId(R.id.prepareMediaButton)
    val mediaPlaybackErrorView by WaitForViewWithId(R.id.mediaPlaybackErrorView)
    val submissionMediaPlayerView by OnViewWithId(R.id.submissionMediaPlayerView)
}