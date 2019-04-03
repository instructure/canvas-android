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

package com.instructure.androidfoosball.wear

import android.os.Bundle
import android.support.wearable.view.CardFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_table.*

class TableCardFragment : CardFragment() {

    override fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.fragment_table, container, false)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        label.text = arguments.getString(TITLE)
        status.text = arguments.getString(STATUS)
    }

    companion object {

        private val TITLE = "TITLE"
        private val STATUS = "STATUS"

        fun newInstance(title: String, status: String): TableCardFragment {
            val fragment = TableCardFragment()
            val args = Bundle()
            args.putString(TITLE, title)
            args.putString(STATUS, status)
            fragment.arguments = args
            return fragment
        }
    }
}
