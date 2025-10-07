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
package com.instructure.student.ui.rendertests.renderpages

import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.page.BasePage
import com.instructure.student.R

class TextSubmissionUploadRenderPage : BasePage(R.id.textSubmissionUpload) {

    val submitButton by OnViewWithId(R.id.menuSubmit)
    val rce by OnViewWithId(R.id.rce)
    val rceWebView by OnViewWithId(R.id.rce_webView)
    val errorMessage by OnViewWithId(R.id.errorMsg)
}
