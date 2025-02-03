/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
@file:Suppress("unused")

package com.instructure.pandautils.utils

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import com.instructure.pandautils.R

val Context.a11yManager get() = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

fun AccessibilityManager.isServiceEnabled(service: Int) = getEnabledAccessibilityServiceList(service).isNotEmpty()

val AccessibilityManager.hasAudibleFeedback get() = isServiceEnabled(AccessibilityServiceInfo.FEEDBACK_AUDIBLE)

val AccessibilityManager.hasBrailleFeedback get() = isServiceEnabled(AccessibilityServiceInfo.FEEDBACK_BRAILLE)

val AccessibilityManager.hasGenericFeedback get() = isServiceEnabled(AccessibilityServiceInfo.FEEDBACK_GENERIC)

val AccessibilityManager.hasHapticFeedback get() = isServiceEnabled(AccessibilityServiceInfo.FEEDBACK_HAPTIC)

val AccessibilityManager.hasSpokenFeedback get() = isServiceEnabled(AccessibilityServiceInfo.FEEDBACK_SPOKEN)

val AccessibilityManager.hasVisualFeedback get() = isServiceEnabled(AccessibilityServiceInfo.FEEDBACK_VISUAL)

val AccessibilityManager.isSwitchAccessEnabled: Boolean
    get() = getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK).any { "com.android.switchaccess.SwitchAccessService" in it.id }

fun getContentDescriptionForMinusGradeString(grade: String, context: Context): String {
    return getContentDescriptionForMinusGradeString(grade, context.resources)
}

fun getContentDescriptionForMinusGradeString(grade: String, resources: Resources): String {
    return if (grade.contains("-")) {
        resources.getString(
            R.string.a11y_gradeLetterMinusContentDescription,
            grade.substringBefore("-")
        )
    } else grade
}

fun isAccessibilityEnabled(context: Context): Boolean {
    val am: AccessibilityManager? = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager?
    return am?.isEnabled ?: false && am?.isTouchExplorationEnabled ?: false
}

fun View.accessibilityClassName(accessibilityClassName: String) {
    accessibilityDelegate = object : View.AccessibilityDelegate() {
        override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
            super.onInitializeAccessibilityNodeInfo(host, info)

            info.className = accessibilityClassName
        }
    }
}


