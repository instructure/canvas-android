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
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.blueprint.ListFragment
import com.instructure.pandautils.blueprint.ListManager
import com.instructure.pandautils.blueprint.ListPresenter
import com.instructure.pandautils.blueprint.ListRecyclerAdapter

abstract class BaseListFragment<
        MODEL,
        PRESENTER : ListPresenter<MODEL, VIEW>,
        VIEW : ListManager<MODEL>,
        HOLDER : RecyclerView.ViewHolder,
        ADAPTER : ListRecyclerAdapter<MODEL, HOLDER, VIEW>>
    : ListFragment<MODEL, PRESENTER, VIEW, HOLDER, ADAPTER>(), NavigationCallbacks {
    var canvasContext: CanvasContext? by NullableParcelableArg(key = Const.CANVAS_CONTEXT)

    protected lateinit var rootView: View

    abstract fun layoutResId(): Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(layoutResId(), container, false)
        return rootView
    }

    fun apiCheck(): Boolean = isAdded

    override fun onHandleBackPressed(): Boolean = false

    companion object {
        fun createBundle(canvasContext: CanvasContext?): Bundle {
            val extras = Bundle()
            extras.putParcelable(Const.CANVAS_CONTEXT, canvasContext)
            return extras
        }
    }
}
