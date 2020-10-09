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
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigUtils
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.loginapi.login.dialog.NoInternetConnectionDialog
import com.instructure.pandautils.fragments.RemoteConfigParamsFragment
import com.instructure.pandautils.utils.*
import com.instructure.student.BuildConfig
import com.instructure.student.R
import com.instructure.student.activity.NothingToSeeHereFragment
import com.instructure.student.activity.NotificationPreferencesActivity
import com.instructure.student.activity.SettingsActivity
import com.instructure.student.dialog.HelpDialogStyled
import com.instructure.student.dialog.LegalDialogStyled
import com.instructure.student.mobius.settings.pairobserver.ui.PairObserverFragment
import com.instructure.student.util.Analytics
import kotlinx.android.synthetic.main.dialog_about.*
import kotlinx.android.synthetic.main.fragment_application_settings.*

@PageView(url = "profile/settings")
class ApplicationSettingsFragment : ParentFragment() {

    override fun title(): String = getString(R.string.settings)

    override fun onCreate(savedInstanceState: Bundle?) {
        TelemetryUtils.setInteractionName(this::class.java.simpleName)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_application_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyTheme()
        setupViews()
    }

    override fun applyTheme() {
        toolbar.setupAsBackButton(this)
        ViewStyler.themeToolbar(requireActivity(), toolbar, Color.WHITE, Color.BLACK, false)
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews() {
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
        help.onClick { HelpDialogStyled.show(requireActivity()) }
        pinAndFingerprint.setGone() // TODO: Wire up once implemented

        pairObserver.setVisible()
        pairObserver.onClick {
            if (APIHelper.hasNetworkConnection()) {
                addFragment(PairObserverFragment.newInstance())
            } else {
                NoInternetConnectionDialog.show(requireFragmentManager())
            }
        }

        pushNotifications.onClick {
            Analytics.trackAppFlow(requireActivity(), NotificationPreferencesActivity::class.java)
            startActivity(Intent(requireActivity(), NotificationPreferencesActivity::class.java))
        }

        about.onClick {
            AlertDialog.Builder(requireContext())
                    .setTitle(R.string.about)
                    .setView(R.layout.dialog_about)
                    .show()
                    .apply {
                        domain.text = ApiPrefs.domain
                        loginId.text = ApiPrefs.user!!.loginId
                        email.text = ApiPrefs.user!!.email ?: ApiPrefs.user!!.primaryEmail
                        version.text = "${getString(R.string.canvasVersionNum)} ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                    }
        }

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
}
