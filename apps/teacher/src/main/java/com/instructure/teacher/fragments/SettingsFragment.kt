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

import android.graphics.Color
import android.os.Bundle
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_SETTINGS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.dialogs.RatingDialog
import com.instructure.pandautils.features.notification.preferences.NotificationPreferencesFragment
import com.instructure.pandautils.fragments.BasePresenterFragment
import com.instructure.pandautils.fragments.RemoteConfigParamsFragment
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.isTablet
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.BuildConfig
import com.instructure.teacher.R
import com.instructure.teacher.dialog.LegalDialog
import com.instructure.teacher.factory.ProfileSettingsFragmentPresenterFactory
import com.instructure.teacher.presenters.ProfileSettingsFragmentPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.setupBackButton
import com.instructure.teacher.viewinterface.ProfileSettingsFragmentView
import kotlinx.android.synthetic.main.fragment_settings.*

@ScreenView(SCREEN_VIEW_SETTINGS)
class SettingsFragment : BasePresenterFragment<ProfileSettingsFragmentPresenter, ProfileSettingsFragmentView>(),
    ProfileSettingsFragmentView {

    override fun layoutResId() = R.layout.fragment_settings

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        versionTextView.text = getString(R.string.fullVersion, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
        profileButton.onClick { RouteMatcher.route(requireContext(), Route(ProfileFragment::class.java, null)) }
        rateButton.onClick { RatingDialog.showRateDialog(requireActivity(), com.instructure.pandautils.utils.AppType.TEACHER) }
        legalButton.onClick { LegalDialog().show(requireFragmentManager(), LegalDialog.TAG) }
        notificationPreferenesButton.onClick { RouteMatcher.route(requireContext(), Route(NotificationPreferencesFragment::class.java, null)) }
        if (BuildConfig.DEBUG) {
            featureFlagButton.setVisible()
            featureFlagButton.onClick { RouteMatcher.route(requireContext(), Route(FeatureFlagsFragment::class.java, null)) }

            remoteConfigButton.setVisible()
            remoteConfigButton.onClick { RouteMatcher.route(requireContext(), Route(RemoteConfigParamsFragment::class.java, null))}
        }
    }

    override fun getPresenterFactory() = ProfileSettingsFragmentPresenterFactory()

    override fun onReadySetGo(presenter: ProfileSettingsFragmentPresenter) {
        setupToolbar()
    }

    fun setupToolbar() {
        toolbar.setupBackButton(this)
        toolbar.title = getString(R.string.settings)
        ViewStyler.themeToolbarBottomSheet(requireActivity(), isTablet, toolbar, Color.BLACK, false)
        ViewStyler.setToolbarElevationSmall(requireContext(), toolbar)
    }

    override fun onRefreshStarted() {}

    override fun onRefreshFinished() {}

    override fun onPresenterPrepared(presenter: ProfileSettingsFragmentPresenter) {}

    companion object {
        fun newInstance(args: Bundle) = SettingsFragment().apply { arguments = args }
    }
}
