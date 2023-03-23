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
package com.instructure.student.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.instructure.canvasapi2.utils.*
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.loginapi.login.dialog.NoInternetConnectionDialog
import com.instructure.pandautils.analytics.SCREEN_VIEW_APPLICATION_SETTINGS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.notification.preferences.EmailNotificationPreferencesFragment
import com.instructure.pandautils.features.notification.preferences.PushNotificationPreferencesFragment
import com.instructure.pandautils.fragments.RemoteConfigParamsFragment
import com.instructure.pandautils.utils.*
import com.instructure.student.BuildConfig
import com.instructure.student.R
import com.instructure.student.activity.NothingToSeeHereFragment
import com.instructure.student.activity.SettingsActivity
import com.instructure.student.databinding.DialogAboutBinding
import com.instructure.student.databinding.FragmentApplicationSettingsBinding
import com.instructure.student.dialog.LegalDialogStyled
import com.instructure.student.mobius.settings.pairobserver.ui.PairObserverFragment

@ScreenView(SCREEN_VIEW_APPLICATION_SETTINGS)
@PageView(url = "profile/settings")
class ApplicationSettingsFragment : ParentFragment() {

    private val binding by viewBinding(FragmentApplicationSettingsBinding::bind)

    override fun title(): String = getString(R.string.settings)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_application_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyTheme()
        setupViews()
    }

    override fun applyTheme() = with(binding) {
        toolbar.setupAsBackButton(this@ApplicationSettingsFragment)
        ViewStyler.themeToolbarColored(requireActivity(), toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews() = with(binding) {
        profileSettings.onClick {
            val frag = if (ApiPrefs.isStudentView) {
                // Profile settings not available in Student View
                NothingToSeeHereFragment.newInstance()
            } else {
                ProfileSettingsFragment.newInstance()
            }

            addFragment(frag)
        }

        // Account Preferences currently only contains the debug language selector, so we'll hide it in prod
        if (BuildConfig.DEBUG) {
            accountPreferences.setVisible()
            accountPreferences.onClick { addFragment(AccountPreferencesFragment.newInstance()) }
        }

        legal.onClick { LegalDialogStyled().show(requireFragmentManager(), LegalDialogStyled.TAG) }
        pinAndFingerprint.setGone() // TODO: Wire up once implemented

        if (ApiPrefs.canGeneratePairingCode == true) {
            pairObserver.setVisible()
            pairObserver.onClick {
                if (APIHelper.hasNetworkConnection()) {
                    addFragment(PairObserverFragment.newInstance())
                } else {
                    NoInternetConnectionDialog.show(requireFragmentManager())
                }
            }
        }

        pushNotifications.onClick {
            addFragment(PushNotificationPreferencesFragment.newInstance())
        }

        emailNotifications.onClick {
            addFragment(EmailNotificationPreferencesFragment.newInstance())
        }

        about.onClick {
            val binding = DialogAboutBinding.inflate(layoutInflater)
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.about)
                .setView(binding.root)
                .show()
                .apply {
                    binding.domain.text = ApiPrefs.domain
                    binding.loginId.text = ApiPrefs.user!!.loginId
                    binding.email.text = ApiPrefs.user!!.email ?: ApiPrefs.user!!.primaryEmail
                    binding.version.text = "${getString(R.string.canvasVersionNum)} ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                }
        }

        if (ApiPrefs.canvasForElementary) {
            elementaryViewSwitch.isChecked = ApiPrefs.elementaryDashboardEnabledOverride
            elementaryViewLayout.setVisible()
            ViewStyler.themeSwitch(requireContext(), elementaryViewSwitch, ThemePrefs.brandColor)
            elementaryViewSwitch.setOnCheckedChangeListener { _, isChecked ->
                ApiPrefs.elementaryDashboardEnabledOverride = isChecked

                val analyticsBundle = Bundle().apply {
                    putBoolean(AnalyticsParamConstants.MANUAL_C4E_STATE, isChecked)
                }
                Analytics.logEvent(AnalyticsEventConstants.CHANGED_C4E_MODE, analyticsBundle)
            }
        }

        setUpAppThemeSelector()
        setUpSubscribeToCalendarFeed()

        if (BuildConfig.DEBUG) {
            featureFlags.setVisible()
            featureFlags.onClick {
                addFragment(FeatureFlagsFragment())
            }

            remoteConfigParams.setVisible()
            remoteConfigParams.onClick {
                addFragment(RemoteConfigParamsFragment())
            }
        }
    }

    private fun addFragment(fragment: Fragment) {
        (activity as? SettingsActivity)?.addFragment(fragment)
    }

    private fun setUpAppThemeSelector() {
        val initialAppTheme = AppTheme.fromIndex(ThemePrefs.appTheme)
        binding.appThemeStatus.setText(initialAppTheme.themeNameRes)

        binding.appThemeContainer.onClick {
            AppThemeSelector.showAppThemeSelectorDialog(requireContext(), binding.appThemeStatus)
        }
    }

    private fun setUpSubscribeToCalendarFeed() {
        val calendarFeed = ApiPrefs.user?.calendar?.ics
        if (!calendarFeed.isNullOrEmpty()) {
            binding.subscribeToCalendar.apply {
               setVisible()
                onClick {
                    AlertDialog.Builder(requireContext())
                        .setMessage(R.string.subscribeToCalendarMessage)
                        .setPositiveButton(R.string.subscribeButton) { dialog, _ ->
                            dialog.dismiss()
                            openCalendarLink(calendarFeed)
                        }
                        .setNegativeButton(R.string.cancel, {dialog, _ -> dialog.dismiss()})
                        .showThemed()
                }
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
}
