/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 *
 */
package com.instructure.teacher.ui.pages

import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.page.BasePage
import com.instructure.teacher.R

class RCEditorPage: BasePage() {
    private val webView by OnViewWithId(R.id.rce_webView)
    private val saveButton by OnViewWithId(R.id.rce_save)

    private val actionUndo by OnViewWithId(R.id.action_undo)
    private val actionRedo by OnViewWithId(R.id.action_redo)
    private val actionBold by OnViewWithId(R.id.action_bold)
    private val actionItalic by OnViewWithId(R.id.action_italic)
    private val actionUnderline by OnViewWithId(R.id.action_underline)

    //These items may be off screen on phones, likely on screen for tablets.
    private val actionTextColor by OnViewWithId(R.id.action_txt_color, autoAssert = false)
    private val actionBulletList by OnViewWithId(R.id.action_insert_bullets, autoAssert = false)
    private val actionUploadImage by OnViewWithId(R.id.actionUploadImage, autoAssert = false)
    private val actionInsertLink by OnViewWithId(R.id.action_insert_link, autoAssert = false)
}
