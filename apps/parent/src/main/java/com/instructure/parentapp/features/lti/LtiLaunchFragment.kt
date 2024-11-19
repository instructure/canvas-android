/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.parentapp.features.lti

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.instructure.pandautils.base.BaseCanvasFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.instructure.canvasapi2.utils.validOrNull
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.NullableStringArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.asChooserExcludingInstructure
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.pandautils.utils.setTextForVisibility
import com.instructure.pandautils.utils.studentColor
import com.instructure.pandautils.utils.toast
import com.instructure.parentapp.R
import com.instructure.parentapp.databinding.FragmentLtiLaunchBinding
import com.instructure.parentapp.util.ParentPrefs
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LtiLaunchFragment : BaseCanvasFragment() {

    private val binding by viewBinding(FragmentLtiLaunchBinding::bind)

    private val viewModel: LtiLaunchViewModel by viewModels()

    var title: String? by NullableStringArg(key = LTI_TITLE)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_lti_launch, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loadingView.setOverrideColor(ParentPrefs.currentStudent?.studentColor ?: ThemePrefs.primaryColor)
        binding.toolName.setTextForVisibility(title.validOrNull())

        lifecycleScope.collectOneOffEvents(viewModel.events, ::handleAction)
    }

    private fun handleAction(action: LtiLaunchAction) {
        when (action) {
            is LtiLaunchAction.LaunchCustomTab -> {
                launchCustomTab(action.url)
            }
            is LtiLaunchAction.ShowError -> {
                toast(R.string.errorOccurred)
                if (activity != null) {
                    requireActivity().onBackPressed()
                }
            }
        }
    }

    private fun launchCustomTab(url: String) {
        val uri = Uri.parse(url)
            .buildUpon()
            .appendQueryParameter("display", "borderless")
            .appendQueryParameter("platform", "android")
            .build()

        val colorSchemeParams = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(ThemePrefs.primaryColor)
            .build()

        var intent = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(colorSchemeParams)
            .setShowTitle(true)
            .build()
            .intent

        intent.data = uri

        // Exclude Instructure apps from chooser options
        intent = intent.asChooserExcludingInstructure()

        requireContext().startActivity(intent)
        Handler(Looper.getMainLooper()).postDelayed({
            if (activity == null) return@postDelayed
            requireActivity().onBackPressed()
        }, 500)
    }

    companion object {
        const val LTI_URL = "lti_url"
        const val LTI_TITLE = "lti_title"
    }
}