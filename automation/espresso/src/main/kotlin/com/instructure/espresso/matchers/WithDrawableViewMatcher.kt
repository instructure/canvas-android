/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 *
 */

package com.instructure.espresso.matchers

import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import org.hamcrest.TypeSafeMatcher

class WithDrawableViewMatcher(@DrawableRes private val drawable: Int, @ColorRes private val tint: Int? = null) : TypeSafeMatcher<ImageView>() {

    override fun matchesSafely(view: ImageView): Boolean {
        val drawableMatcher = view.drawable != null && view.drawable.constantState?.newDrawable()?.constantState?.hashCode() == AppCompatResources.getDrawable(
            view.context,
            drawable
        )?.constantState?.hashCode()

        val tintMatcher = tint != null && view.imageTintList?.defaultColor == AppCompatResources.getColorStateList(
            view.context,
            tint
        )?.defaultColor

        return drawableMatcher && (tintMatcher || tint == null)
    }

    override fun describeTo(description: org.hamcrest.Description) {
        description.appendText("with drawable and tint")
    }
}