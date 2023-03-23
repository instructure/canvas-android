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
import android.view.View
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_SETTINGS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.dialogs.RatingDialog
import com.instructure.pandautils.features.notification.preferences.EmailNotificationPreferencesFragment
import com.instructure.pandautils.features.notification.preferences.PushNotificationPreferencesFragment
import com.instructure.pandautils.fragments.BasePresenterFragment
import com.instructure.pandautils.fragments.RemoteConfigParamsFragment
import com.instructure.pandautils.utils.*
import com.instructure.teacher.BuildConfig
import com.instructure.teacher.R
import com.instructure.teacher.databinding.FragmentSettingsBinding
import com.instructure.teacher.dialog.LegalDialog
import com.instructure.teacher.factory.ProfileSettingsFragmentPresenterFactory
import com.instructure.teacher.presenters.ProfileSettingsFragmentPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.setupBackButton
import com.instructure.teacher.viewinterface.ProfileSettingsFragmentView

@ScreenView(SCREEN_VIEW_SETTINGS)
class SettingsFragment : BasePresenterFragment<ProfileSettingsFragmentPresenter, ProfileSettingsFragmentView>(),
    ProfileSettingsFragmentView {

    private val binding by viewBinding(FragmentSettingsBinding::bind)

    private var canvasContext: CanvasContext? by NullableParcelableArg(key = Const.CANVAS_CONTEXT)

    override fun layoutResId() = R.layout.fragment_settings

    override fun onActivityCreated(savedInstanceState: Bundle?) = with(binding) {
        super.onActivityCreated(savedInstanceState)
        versionTextView.text = getString(R.string.fullVersion, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
        profileButton.onClick {
            RouteMatcher.route(
                requireContext(),
                Route(null, ProfileFragment::class.java, canvasContext, canvasContext?.makeBundle() ?: Bundle())
            )
        }
        rateButton.onClick { RatingDialog.showRateDialog(requireActivity(), AppType.TEACHER) }
        legalButton.onClick { LegalDialog().show(requireFragmentManager(), LegalDialog.TAG) }
        notificationPreferenesButton.onClick {
            RouteMatcher.route(
                requireContext(),
                Route(null, PushNotificationPreferencesFragment::class.java, canvasContext, canvasContext?.makeBundle() ?: Bundle())
            )
        }
        emailNotifications.onClick {
            RouteMatcher.route(
                requireContext(),
                Route(null, EmailNotificationPreferencesFragment::class.java, canvasContext, canvasContext?.makeBundle() ?: Bundle())
            )
        }
        if (BuildConfig.DEBUG) {
            featureFlagButton.setVisible()
            featureFlagButton.onClick {
                RouteMatcher.route(
                    requireContext(),
                    Route(null, FeatureFlagsFragment::class.java, canvasContext, canvasContext?.makeBundle() ?: Bundle())
                )
            }

            remoteConfigButton.setVisible()
            remoteConfigButton.onClick {
                RouteMatcher.route(
                    requireContext(),
                    Route(null, RemoteConfigParamsFragment::class.java, canvasContext, canvasContext?.makeBundle() ?: Bundle())
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpAppThemeSelector()
    }

    private fun setUpAppThemeSelector() = with(binding) {
        val initialAppTheme = AppTheme.fromIndex(ThemePrefs.appTheme)
        appThemeStatus.setText(initialAppTheme.themeNameRes)

        appThemeContainer.onClick {
            AppThemeSelector.showAppThemeSelectorDialog(requireContext(), appThemeStatus)
        }
    }

    override fun getPresenterFactory() = ProfileSettingsFragmentPresenterFactory()

    override fun onReadySetGo(presenter: ProfileSettingsFragmentPresenter) {
        setupToolbar()
    }

    fun setupToolbar() = with(binding) {
        toolbar.setupBackButton(this@SettingsFragment)
        toolbar.title = getString(R.string.settings)
        ViewStyler.themeToolbarColored(requireActivity(), toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
    }

    override fun onRefreshStarted() {}

    override fun onRefreshFinished() {}

    override fun onPresenterPrepared(presenter: ProfileSettingsFragmentPresenter) {}

    companion object {
        fun newInstance(args: Bundle) = SettingsFragment().apply { arguments = args }
    }
}
