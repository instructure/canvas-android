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
package com.instructure.teacher.fragments

import android.os.Bundle
import com.instructure.pandautils.utils.enableAlgorithmicDarkening
import com.instructure.pandautils.utils.setGone

/**
 * TEMPORARY fragment to house discussion submissions
 * in speed grader until discussions is finished
 * TODO: REMOVE ME EVENTUALLY
 */
class SimpleWebViewFragment : InternalWebViewFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        setShouldLoadUrl(false)
        setShouldAuthenticateUponLoad(true)
        shouldRouteInternally = false
        binding.canvasWebView.setInitialScale(100)
        super.onActivityCreated(savedInstanceState)
        binding.canvasWebView.enableAlgorithmicDarkening()

        loadUrl(url)
        toolbar?.setGone()
    }

    companion object {
        const val URL = "url"

        fun newInstance(url: String) = SimpleWebViewFragment().apply {
            this.url = url
        }

        fun createBundle(url: String): Bundle {
            return Bundle().apply {
                putString(URL, url)
            }
        }
    }
}
