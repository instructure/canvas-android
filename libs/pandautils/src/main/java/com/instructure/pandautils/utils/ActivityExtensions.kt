/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
package com.instructure.pandautils.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.pandautils.R

fun FragmentActivity.withRequireNetwork(block: () -> Unit) {
    if (APIHelper.hasNetworkConnection()) {
        block()
    } else {
        AlertDialog.Builder(this)
            .setTitle(R.string.noInternetConnectionTitle)
            .setMessage(R.string.noInternetConnectionMessage)
            .setCancelable(true)
            .setPositiveButton(android.R.string.ok, { dialog, _ -> dialog.dismiss() })
            .showThemed()
    }
}

fun Context.getFragmentActivity(): FragmentActivity {
    if (this is FragmentActivity) return this
    if (this is ContextWrapper) return this.baseContext.getFragmentActivity()
    else throw IllegalStateException("Not FragmentActivity context")
}

fun Context.getFragmentActivityOrNull(): FragmentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is FragmentActivity) return context
        context = context.baseContext
    }
    return null
}

fun Context.getActivityOrNull(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }

    return null
}
