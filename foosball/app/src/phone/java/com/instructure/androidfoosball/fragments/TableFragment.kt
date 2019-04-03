/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.instructure.androidfoosball.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.adapters.TableFireRecyclerAdapter
import com.instructure.androidfoosball.decorators.SpacesItemDecoration
import com.instructure.androidfoosball.interfaces.FragmentCallbacks
import kotlinx.android.synthetic.phone.fragment_tables.*


class TableFragment : Fragment() {

    lateinit private var mCallbacks: FragmentCallbacks
    private var mAdapter: TableFireRecyclerAdapter? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCallbacks = context as FragmentCallbacks
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_tables, container, false)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(SpacesItemDecoration(context, R.dimen.card_spacing))
        mAdapter = TableFireRecyclerAdapter(context, mCallbacks.mDatabase!!.child("tables"))
        recyclerView.adapter = mAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        mAdapter?.cleanup()
    }

    companion object {
        fun newInstance(): TableFragment {
            return TableFragment()
        }
    }
}
