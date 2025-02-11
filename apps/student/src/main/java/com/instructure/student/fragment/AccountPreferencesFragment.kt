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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.utils.LocaleUtils
import com.instructure.pandautils.utils.cleanDisplayName
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.pandautils.analytics.SCREEN_VIEW_ACCOUNT_PREFERENCES
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.student.BuildConfig
import com.instructure.student.R
import com.instructure.student.databinding.FragmentAccountPreferencesBinding
import com.instructure.student.databinding.SettingsSpinnerBinding
import com.instructure.student.databinding.SettingsSpinnerItemBinding
import java.util.Locale

@ScreenView(SCREEN_VIEW_ACCOUNT_PREFERENCES)
@PageView(url = "profile/settings")
class AccountPreferencesFragment : ParentFragment() {

    private val binding by viewBinding(FragmentAccountPreferencesBinding::bind)

    private val languages: List<Pair<String, String>> by lazy {
        val translationArray = BuildConfig.TRANSLATION_TAGS.split(";")
        listOf(
            ApiPrefs.ACCOUNT_LOCALE to "Account Locale",
            ApiPrefs.DEVICE_LOCALE to "Device Locale"
        ) + translationArray
            .map { it to Locale.Builder().setLanguageTag(it).build().cleanDisplayName }
    }

    private var mLanguageListener: AdapterView.OnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>) = Unit
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            restartAppForLocale(position)
        }
    }

    override fun title(): String = getString(R.string.accountPreferences)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_account_preferences, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyTheme()
        setupViews()
    }

    override fun applyTheme() = with(binding) {
        toolbar.title = title()
        toolbar.setupAsBackButton(this@AccountPreferencesFragment)
        ViewStyler.themeToolbarColored(requireActivity(), toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
    }

    private fun setupViews() = with(binding) {
        if (BuildConfig.DEBUG) {
            // Only show language override picker in debug builds
            languageContainer.setVisible()
            languageSpinner.post {
                val selectedIdx = languages.indexOfFirst { it.first == ApiPrefs.selectedLocale }
                languageSpinner.setSelection(selectedIdx, false)
                languageSpinner.post { languageSpinner.onItemSelectedListener = mLanguageListener }
            }
            languageSpinner.adapter = SettingsSpinnerAdapter(languages.map { it.second }.toTypedArray())
        }
    }

    private inner class SettingsSpinnerAdapter<String>(val items: Array<String>) : BaseAdapter() {
        private val inflater: LayoutInflater = LayoutInflater.from(requireContext())

        override fun getCount(): Int = items.size

        override fun getItem(position: Int): String = items[position]

        override fun getItemId(position: Int): Long = 0

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: View = convertView ?: inflater.inflate(R.layout.settings_spinner, parent, false)
            val binding = SettingsSpinnerBinding.bind(view)
            binding.indicator.setImageDrawable(ColorKeeper.getColoredDrawable(requireContext(), R.drawable.ic_expand, requireContext().getColor(R.color.white)))
            binding.indicator.setColorFilter(ContextCompat.getColor(requireContext(), R.color.textDark))
            binding.title.text = getItem(position).toString()
            return binding.root
        }

        override fun getDropDownView(position: Int, view: View?, parent: ViewGroup): View {
            val v = inflater.inflate(R.layout.settings_spinner_item, parent, false)
            val binding = SettingsSpinnerItemBinding.bind(v)
            binding.title.text = getItem(position).toString()
            return binding.root
        }
    }

    private fun restartAppForLocale(position: Int) {
        AlertDialog.Builder(requireContext(), R.style.AccessibleAlertDialog)
                .setTitle(R.string.restartingCanvas)
                .setMessage(if (position == 0) R.string.defaultLanguageWarning else R.string.languageDialogText)
                .setPositiveButton(R.string.yes) { _, _ ->
                    // Set the language
                    ApiPrefs.selectedLocale = languages[position].first
                    LocaleUtils.restartApp(requireContext())
                }
                .setNegativeButton(R.string.no, null)
                .setCancelable(true)
                .show()
    }

    companion object {
        fun newInstance(): AccountPreferencesFragment {
            return AccountPreferencesFragment()
        }
    }

}
