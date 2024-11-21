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
package com.instructure.pandautils.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.utils.Const

abstract class BaseFragment : BaseCanvasFragment(), NavigationCallbacks {
    protected lateinit var rootView: View

    abstract fun layoutResId(): Int
    abstract fun onCreateView(view: View)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(layoutResId(), container, false)
        onCreateView(rootView)
        return rootView
    }

    fun apiCheck(): Boolean = isAdded

    protected fun showToast(@StringRes s: Int) {
        Toast.makeText(activity, s, Toast.LENGTH_SHORT).show()
    }

    protected fun showToast(s: String?) {
        Toast.makeText(activity, s, Toast.LENGTH_SHORT).show()
    }

    override fun onHandleBackPressed(): Boolean = false

    companion object {
        fun createBundle(canvasContext: CanvasContext?): Bundle {
            val extras = Bundle()
            extras.putParcelable(Const.CANVAS_CONTEXT, canvasContext)
            return extras
        }
    }
}
