/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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
package com.instructure.parentapp.fragments

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.instructure.parentapp.R
import com.instructure.parentapp.util.ParentPrefs
import com.instructure.parentapp.util.ViewUtils

open class ParentFragment : Fragment() {

    private var dialogToolbar: Toolbar? = null

    protected open val rootLayout: Int
        get() = -1

    fun setActionbarColor(actionBarColor: Int) {
        dialogToolbar?.setBackgroundColor(actionBarColor)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun setStatusBarColor(statusBarColor: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && statusBarColor != Integer.MAX_VALUE) {
            //make the status bar darker than the toolbar
            activity?.window?.statusBarColor = ViewUtils.darker(statusBarColor, 0.85f)
        }
    }

    protected open fun setupDialogToolbar(rootView: View) {
        dialogToolbar = rootView.findViewById<View>(R.id.toolbar) as Toolbar
        dialogToolbar?.visibility = View.VISIBLE

        dialogToolbar?.setNavigationIcon(R.drawable.ic_close_white)
        dialogToolbar?.setNavigationContentDescription(R.string.close)
        dialogToolbar?.setNavigationOnClickListener { activity?.onBackPressed() }

        setStatusBarColor(ParentPrefs.currentColor)
        setActionbarColor(ParentPrefs.currentColor)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

}
