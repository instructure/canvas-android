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

package com.instructure.parentapp.features.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.instructure.pandautils.base.BaseCanvasFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.instructure.pandautils.utils.LocaleUtils
import com.instructure.pandautils.views.CanvasLoadingView
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.parentapp.R
import com.instructure.parentapp.util.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SplashFragment : BaseCanvasFragment() {

    private val viewModel: SplashViewModel by viewModels()

    @Inject
    lateinit var navigation: Navigation

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        lifecycleScope.collectOneOffEvents(viewModel.events, ::handleAction)

        return ComposeView(requireActivity()).apply {
            setContent {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = colorResource(id = R.color.backgroundLightest))
                ) {
                    AndroidView(
                        factory = {
                            CanvasLoadingView(it).apply {
                                setOverrideColor(it.getColor(R.color.login_parentAppTheme))
                            }
                        },
                        modifier = Modifier.size(120.dp)
                    )
                }
            }
        }
    }

    private fun handleAction(action: SplashAction) {
        when (action) {
            is SplashAction.LocaleChanged -> LocaleUtils.restartApp(requireContext())

            is SplashAction.InitialDataLoadingFinished -> {
                findNavController().currentBackStackEntry?.savedStateHandle?.set(INITIAL_DATA_LOADED_KEY, true)
            }

            is SplashAction.NavigateToNotAParentScreen -> {
                findNavController().popBackStack()
                navigation.navigate(activity, navigation.notAParent)
            }

            is SplashAction.ApplyTheme -> ThemePrefs.applyCanvasTheme(action.canvasTheme, requireContext())
        }
    }

    companion object {
        const val INITIAL_DATA_LOADED_KEY = "initialDataLoaded"
    }
}
