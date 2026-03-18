/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.student.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_NOTHING_TO_SEE_HERE
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.student.R
import com.instructure.student.databinding.FragmentNothingToSeeHereBinding
import com.instructure.student.fragment.ParentFragment

@ScreenView(SCREEN_VIEW_NOTHING_TO_SEE_HERE)
class NothingToSeeHereFragment : ParentFragment() {

    private val binding by viewBinding(FragmentNothingToSeeHereBinding::bind)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_nothing_to_see_here, container, false)

    override fun title() = "" // Don't need this

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyTheme()
    }

    override fun applyTheme() {
        binding.toolbar.setupAsBackButton(this)
        binding.toolbar.applyTopSystemBarInsets()
        ViewStyler.themeToolbarLight(requireActivity(), binding.toolbar)
    }

    companion object {
        fun newInstance(): NothingToSeeHereFragment {
            return NothingToSeeHereFragment()
        }

        fun makeRoute() = Route(NothingToSeeHereFragment::class.java, null)
    }
}
