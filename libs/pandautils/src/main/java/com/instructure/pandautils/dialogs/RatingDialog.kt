/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 *
 */
package com.instructure.pandautils.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.utils.*
import com.instructure.pandautils.BuildConfig
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.SCREEN_VIEW_RATING
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.AppType
import com.instructure.pandautils.utils.Utils
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.withArgs
import kotlinx.android.synthetic.main.dialog_rating.view.*

@ScreenView(SCREEN_VIEW_RATING)
class RatingDialog : DialogFragment() {

    object Prefs : PrefManager("rating_dialog") {
        var dateShowAgain by IntPref(FOUR_WEEKS, keyName = "date_show_again")
        var dateFirstLaunched by LongPref(keyName = "date_first_launched")
        var dontShowAgain by BooleanPref(keyName = "dont_show_again")
        var hasShown by BooleanPref(keyName = "has_shown")
    }

    lateinit private var stars: List<ImageView>

    /**
     * Called when the user hits the back button, this will delay the dialog from showing for 4 weeks
     *
     * @param dialog
     */
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Prefs.dateShowAgain = FOUR_WEEKS // Show again in 4 weeks
        Prefs.dateFirstLaunched = System.currentTimeMillis() // Reset the date_first_launched to be right now
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Prefs.hasShown = true
        val appType = arguments?.getSerializable(APP_TYPE) as AppType
        val buttonText = if (Prefs.hasShown) getString(R.string.utils_dontShowAgain) else getString(R.string.done)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_rating, null)
        setupViews(view, appType)

        val dialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.utils_howAreWeDoing)
                .setView(view)
                .setPositiveButton(buttonText) { _, _ -> Prefs.dontShowAgain = true }
                .create()
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        return dialog
    }

    private fun setupViews(view: View, appType: AppType) = with(view) {
        stars = listOf(view.star1, view.star2, view.star3, view.star4, view.star5)

        send.setOnClickListener {
            val message = comments.text?.toString().orEmpty()
            if (message.isBlank()) {
                Prefs.dateShowAgain = FOUR_WEEKS // Show again in 4 weeks
            } else {
                Prefs.dateShowAgain = SIX_WEEKS // Show again in 6 weeks
                // Figure out which app they're using so we can track that in the suggestion
                // App names aren't localized so they don't need translated
                val appTitle = when (appType) {
                    AppType.STUDENT -> getString(R.string.utils_canvas)
                    AppType.PARENT -> getString(R.string.utils_canvasParent)
                    AppType.TEACHER -> getString(R.string.utils_canvasTeacher)
                }
                /* They provided some feedback, so we will open an email with their suggestion
                populated check to make sure they have an app that can handle the mail intent they
                provided some feedback, so we will open an email with their suggestion populated */
                val intent = populateMailIntent(getString(R.string.utils_suggestions) + " - " + appTitle, message)
                intent.resolveActivity(requireActivity().packageManager)?.let { startActivity(intent) }
            }
            // Reset dateFirstLaunched to be right now
            Prefs.dateFirstLaunched = System.currentTimeMillis()
            dismiss()
        }

        val starClickListener = View.OnClickListener { v ->
            stars.forEach { it.setImageResource(R.drawable.ic_star) }
            val selectionIndex = stars.indexOf(v)
            stars.take(selectionIndex + 1).forEach { it.setImageResource(R.drawable.ic_star_default_color) }
            val isFiveStars = selectionIndex >= 4
            comments.setVisible(!isFiveStars)
            send.setVisible(!isFiveStars)
            if (isFiveStars) {
                Utils.goToAppStore(appType, activity)
                Prefs.dontShowAgain = true
                dismiss()
            }
        }

        stars.forEach {
            it.setImageResource(R.drawable.ic_star)
            it.setOnClickListener(starClickListener)
        }
    }

    private fun populateMailIntent(subject: String, title: String): Intent {
        //let the user open their favorite mail client
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.utils_mobileSupportEmailAddress)))
        //try to get the version number and version code
        val pInfo: PackageInfo?
        var versionName = ""
        var versionCode = 0
        try {
            pInfo = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
            versionName = pInfo.versionName
            versionCode = pInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            Logger.d(e.message)
        }

        intent.putExtra(Intent.EXTRA_SUBJECT, subject + " " + versionName)

        val user = ApiPrefs.user
        //populate the email body with information about the user
        var emailBody = ""
        emailBody += title + "\n\n"

        if (user != null) {
            emailBody += requireContext().getString(R.string.utils_userId) + ": " + user.id + "\n"
            emailBody += requireContext().getString(R.string.utils_email) + ": " + user.primaryEmail + "\n"
        }
        emailBody += requireContext().getString(R.string.utils_domain) + ": " + ApiPrefs.domain + "\n"
        emailBody += requireContext().getString(R.string.utils_versionNum) + " " + versionName + " " + versionCode + "\n"
        emailBody += getString(R.string.utils_device) + ": " + Build.MANUFACTURER + " " + Build.MODEL + "\n"
        emailBody += getString(R.string.utils_osVersion) + ": " + Build.VERSION.RELEASE + "\n"
        emailBody += "-----------------------\n"

        intent.putExtra(Intent.EXTRA_TEXT, emailBody)

        return intent
    }

    companion object {

        private const val APP_TYPE = "app_type"
        private const val FOUR_WEEKS = 28
        private const val SIX_WEEKS = 42

        fun newInstance(appType: AppType): RatingDialog {
            return RatingDialog().withArgs {
                putSerializable(APP_TYPE, appType)
            }
        }

        /**
         * Will show the rating dialog when:
         *
         * - user has used the app for 4 weeks
         * - when the user sees the dialog, there are a few use cases for when to show the
         * dialog again
         *
         * 1. User presses 5 stars -> take user to play store and don't show dialog again
         * 2. User presses < 5 stars with no comment -> show again 4 weeks later
         * 3. User presses < 5 stars with a comment -> show again 6 weeks later
         * 4. User presses back -> show again 4 weeks later
         *
         * - when the user sees the dialog again there will be a "don't show again" button
         * that they can press
         * @param context A valid context
         */
        fun showRatingDialog(context: FragmentActivity, appType: AppType) {
            if (Prefs.dontShowAgain || BuildConfig.IS_TESTING) return
            if (Prefs.dateFirstLaunched == 0L) {
                Prefs.dateFirstLaunched = System.currentTimeMillis()
            }
            if (System.currentTimeMillis() >= Prefs.dateFirstLaunched + (Prefs.dateShowAgain.toLong() * 24 * 60 * 60 * 1000)) {
                showRateDialog(context, appType)
            }
        }

        @Suppress("MemberVisibilityCanPrivate")
        fun showRateDialog(context: FragmentActivity, appType: AppType) {
            RatingDialog.newInstance(appType).show(context.supportFragmentManager, RatingDialog::class.java.simpleName)
        }
    }
}


