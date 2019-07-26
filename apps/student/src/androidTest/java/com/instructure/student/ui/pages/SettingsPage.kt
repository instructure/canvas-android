package com.instructure.student.ui.pages

import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.student.R

class SettingsPage : BasePage(R.id.settingsFragment) {
    private val toolbar by OnViewWithId(R.id.toolbar)
    private val profileSettingLabel by OnViewWithId(R.id.profileSettings)
    private val accountPreferencesLabel by OnViewWithId(R.id.accountPreferences)
    private val pushNotificationsLabel by OnViewWithId(R.id.pushNotifications)
    private val aboutLabel by OnViewWithId(R.id.about)
    private val legalLabel by OnViewWithId(R.id.legal)
    private val helpLabel by OnViewWithId(R.id.help)

    fun launchAboutPage() {
        aboutLabel.click()
    }

    fun launchLegalPage() {
        legalLabel.click()
    }

    fun launchHelpPage() {
        helpLabel.click()
    }


}