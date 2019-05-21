/*
 * Original source code obtained from:
 *      https://github.com/aosp-mirror/platform_frameworks_support/blob/237c8946756af4b0fe9d0fa3965593e247d53698/appcompat/src/main/java/androidx/appcompat/widget/TooltipCompatHandler.java
 *
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Modifications Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.ui

import android.annotation.SuppressLint
import android.view.View
import androidx.core.view.ViewCompat

internal class SubmissionRubricTooltipHandler private constructor(
    private val anchor: View,
    private val text: CharSequence,
    private val customDuration: Long?
) : View.OnAttachStateChangeListener {

    private val hideRunnable = Runnable { hide() }

    private var popup: SubmissionRubricTooltipPopup? = null

    init {
        anchor.setOnClickListener { show() }
    }

    override fun onViewAttachedToWindow(v: View) = Unit

    override fun onViewDetachedFromWindow(v: View) = hide()

    fun show() {
        if (!ViewCompat.isAttachedToWindow(anchor)) return
        currentHandler?.hide()
        currentHandler = this

        popup = SubmissionRubricTooltipPopup(anchor.context)
        val suggestedDuration = popup!!.show(anchor, text)

        anchor.addOnAttachStateChangeListener(this)
        anchor.removeCallbacks(hideRunnable)
        anchor.postDelayed(hideRunnable, customDuration ?: suggestedDuration)
    }

    fun hide() {
        if (currentHandler === this) {
            currentHandler = null
            popup?.hide()
            popup = null
            anchor.removeOnAttachStateChangeListener(this)
        }
        anchor.removeCallbacks(hideRunnable)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var currentHandler: SubmissionRubricTooltipHandler? = null

        fun setTooltipText(view: View, tooltipText: CharSequence, customDuration: Long? = null) {
            if (tooltipText.isEmpty()) {
                currentHandler?.let { if (it.anchor === view) it.hide() }
                view.setOnClickListener(null)
                view.isClickable = false
            } else {
                SubmissionRubricTooltipHandler(view, tooltipText, customDuration)
            }
        }
    }
}
