/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.utils

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.teacher.R
import com.instructure.teacher.dialog.NoInternetConnectionDialog

/** Show a toast with a default length of Toast.LENGTH_SHORT */
fun Context.toast(@StringRes messageResId: Int, length: Int = Toast.LENGTH_SHORT) = Toast.makeText(this, messageResId, length).show()

fun Context.getColorCompat(@ColorRes colorRes: Int) = ContextCompat.getColor(this, colorRes)

fun FragmentActivity.withRequireNetwork(block: () -> Unit) {
    if (APIHelper.hasNetworkConnection()) block() else NoInternetConnectionDialog.show(supportFragmentManager)
}

val Activity.isTablet: Boolean
    get() = resources.getBoolean(R.bool.isDeviceTablet)

