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
 *
 */
package com.instructure.student.ui.pages

import android.widget.Button
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.scrollTo
import com.instructure.espresso.scrollTo
import com.instructure.student.R
import org.hamcrest.core.AllOf.allOf

class FileUploadPage : BasePage() {
    private val cameraButton by OnViewWithId(R.id.fromCamera)
    private val galleryButton by OnViewWithId(R.id.fromGallery)
    private val deviceButton by OnViewWithId(R.id.fromDevice)

    fun chooseCamera() {
        cameraButton.scrollTo().click()
    }

    fun chooseGallery() {
        galleryButton.scrollTo().click()
    }

    fun chooseDevice() {
        deviceButton.scrollTo().click()
    }

    fun clickUpload() {
        onView(allOf(isAssignableFrom(Button::class.java),containsTextCaseInsensitive("upload"))).click()
    }
}