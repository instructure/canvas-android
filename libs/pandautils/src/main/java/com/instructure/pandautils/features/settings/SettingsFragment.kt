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

package com.instructure.pandautils.features.settings

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.instructure.pandautils.base.BaseCanvasFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.interactions.router.Route
import com.instructure.pandautils.R
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.databinding.FragmentSettingsBinding
import com.instructure.pandautils.features.about.AboutFragment
import com.instructure.pandautils.features.legal.LegalDialogFragment
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.pandautils.utils.showThemed
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

const val OFFLINE_ENABLED = "offlineEnabled"

@AndroidEntryPoint
class SettingsFragment : BaseCanvasFragment() {

    @Inject
    lateinit var settingsRouter: SettingsRouter

    @Inject
    lateinit var sharedEvents: SettingsSharedEvents

    private val viewModel: SettingsViewModel by viewModels()

    private val binding: FragmentSettingsBinding by viewBinding(FragmentSettingsBinding::bind)

    private var bitmap: Bitmap? = null
    private var xPos = 0
    private var yPos = 0
    private var scrollValue = 0
    private var appThemeChange = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        ThemePrefs.reapplyCanvasTheme(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = layoutInflater.inflate(R.layout.fragment_settings, container, false)

        bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            savedInstanceState?.getParcelable("bitmap", Bitmap::class.java)
        } else {
            savedInstanceState?.getParcelable("bitmap")
        }

        bitmap?.let {
            view.findViewById<ImageView>(R.id.backImage).setImageBitmap(it)
        }
        return view
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (appThemeChange) {
            val w = requireView().measuredWidth
            val h = requireView().measuredHeight
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            requireView().draw(canvas)
            outState.putParcelable("bitmap", bitmap)
            outState.putInt("xPos", xPos)
            outState.putInt("yPos", yPos)
            outState.putInt("scrollValue", scrollValue)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        xPos = savedInstanceState?.getInt("xPos") ?: (requireView().measuredWidth / 2)
        yPos = savedInstanceState?.getInt("yPos") ?: (requireView().measuredHeight / 2)

        if (bitmap != null) {
            binding.settingsComposeView.post {
                val w = requireView().measuredWidth
                val h = requireView().measuredHeight
                val finalRadius = kotlin.math.sqrt((w * w + h * h).toDouble()).toFloat()
                val anim = ViewAnimationUtils.createCircularReveal(
                    binding.settingsComposeView,
                    xPos,
                    yPos,
                    0f,
                    finalRadius
                )
                anim.duration = 1000
                anim.start()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewStyler.setStatusBarDark(requireActivity(), ThemePrefs.primaryColor)

        lifecycleScope.collectOneOffEvents(viewModel.events, ::handleAction)
        lifecycleScope.collectOneOffEvents(sharedEvents.events, ::handleSharedViewModelAction)

        binding.settingsComposeView.apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                SettingsScreen(uiState) {
                    requireActivity().onBackPressed()
                }
            }
        }
    }

    private fun handleAction(action: SettingsViewModelAction) {
        when (action) {
            is SettingsViewModelAction.Navigate -> {
                navigate(action.item)
            }

            is SettingsViewModelAction.AppThemeClickPosition -> {
                xPos = action.xPos
                yPos = action.yPos
                appThemeChange = true
            }
        }
    }

    private fun navigate(item: SettingsItem) {
        when (item) {
            SettingsItem.ABOUT -> {
                AboutFragment.newInstance().show(childFragmentManager, null)
            }

            SettingsItem.PROFILE_SETTINGS -> {
                settingsRouter.navigateToProfileSettings()
            }

            SettingsItem.PUSH_NOTIFICATIONS -> {
                settingsRouter.navigateToPushNotificationsSettings()
            }

            SettingsItem.EMAIL_NOTIFICATIONS -> {
                settingsRouter.navigateToEmailNotificationsSettings()
            }

            SettingsItem.PAIR_WITH_OBSERVER -> {
                settingsRouter.navigateToPairWithObserver()
            }

            SettingsItem.LEGAL -> {
                LegalDialogFragment().show(childFragmentManager, null)
            }

            SettingsItem.OFFLINE_SYNCHRONIZATION -> {
                settingsRouter.navigateToSyncSettings()
            }

            SettingsItem.SUBSCRIBE_TO_CALENDAR -> {
                ApiPrefs.user?.calendar?.ics?.let { calendarFeed ->
                    AlertDialog.Builder(requireContext())
                        .setMessage(R.string.subscribeToCalendarMessage)
                        .setPositiveButton(R.string.subscribeButton) { dialog, _ ->
                            dialog.dismiss()
                            openCalendarLink(calendarFeed)
                        }
                        .setNegativeButton(R.string.cancel, { dialog, _ -> dialog.dismiss() })
                        .showThemed()
                }
            }

            SettingsItem.ACCOUNT_PREFERENCES -> {
                settingsRouter.navigateToAccountPreferences()
            }

            SettingsItem.REMOTE_CONFIG -> {
                settingsRouter.navigateToRemoteConfig()
            }

            SettingsItem.FEATURE_FLAGS -> {
                settingsRouter.navigateToFeatureFlags()
            }

            SettingsItem.RATE_APP -> {
                settingsRouter.navigateToRateApp()
            }

            SettingsItem.INBOX_SIGNATURE -> {
                settingsRouter.navigateToInboxSignature()
            }

            else -> {

            }
        }
    }

    private fun openCalendarLink(calendarLink: String) {
        val webcalLink = calendarLink.replace("https://", "webcal://")
        val googleCalendarLink = "https://calendar.google.com/calendar/r?cid=$webcalLink"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setData(Uri.parse(googleCalendarLink))
        startActivity(intent)
    }

    private fun handleSharedViewModelAction(action: SettingsSharedAction) {
        when (action) {
            is SettingsSharedAction.UpdateSignatureSettings -> {
                viewModel.updateSignatureSettings(action.enabled)
            }
        }
    }

    companion object {
        fun newInstance(route: Route): SettingsFragment {
            return SettingsFragment().apply {
                arguments = route.arguments
            }
        }

        fun makeRoute(offlineEnabled: Boolean): Route {
            return Route(SettingsFragment::class.java, null, Bundle().apply {
                putBoolean(OFFLINE_ENABLED, offlineEnabled)
            })
        }
    }
}

