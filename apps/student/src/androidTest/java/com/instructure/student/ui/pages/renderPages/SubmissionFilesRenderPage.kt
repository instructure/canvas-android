/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.student.ui.pages.renderPages

import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.page.BasePage
import com.instructure.student.R

class SubmissionFilesRenderPage : BasePage(R.id.submissionFilesPage) {

    val emptyView by OnViewWithId(R.id.emptyView)
    val recyclerView by OnViewWithId(R.id.recyclerView)

    val icon by OnViewWithId(R.id.fileIcon)
    val thumbnail by OnViewWithId(R.id.thumbnail)
    val filename by OnViewWithId(R.id.fileName)
    val checkmark by OnViewWithId(R.id.selectedIcon)

}
