/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.student.mobius.conferences.conference_details.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.snackbar.Snackbar
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.asChooserExcludingInstructure
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.pandautils.utils.setHidden
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.student.R
import com.instructure.student.databinding.AdapterConferenceRecordingItemBinding
import com.instructure.student.databinding.FragmentConferenceDetailsBinding
import com.instructure.student.mobius.common.ui.MobiusView
import com.instructure.student.mobius.conferences.conference_details.ConferenceDetailsEvent
import com.spotify.mobius.functions.Consumer

class ConferenceDetailsView(val canvasContext: CanvasContext, inflater: LayoutInflater, parent: ViewGroup) :
    MobiusView<ConferenceDetailsViewState, ConferenceDetailsEvent, FragmentConferenceDetailsBinding>(
        inflater,
        FragmentConferenceDetailsBinding::inflate,
        parent
    ) {

    init {
        binding.toolbar.setupAsBackButton { (context as? Activity)?.onBackPressed() }
        binding.toolbar.subtitle = canvasContext.name

        // Apply top system bar insets for edge-to-edge support
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBars.top)
            insets
        }
        ViewCompat.requestApplyInsets(binding.toolbar)
    }

    override fun applyTheme() {
        ViewStyler.themeToolbarColored(context as Activity, binding.toolbar, canvasContext)
    }

    override fun onConnect(output: Consumer<ConferenceDetailsEvent>) {
        binding.swipeRefreshLayout.setOnRefreshListener { output.accept(ConferenceDetailsEvent.PullToRefresh) }
        binding.joinButton.onClickWithRequireNetwork { output.accept(ConferenceDetailsEvent.JoinConferenceClicked) }
    }

    override fun onDispose() = Unit

    override fun render(state: ConferenceDetailsViewState) = with(binding) {
        swipeRefreshLayout.isRefreshing = state.isLoading

        // Text details
        title.text = state.title
        status.text = state.status
        description.text = state.description
        inProgressIndicator.setVisible(state.showJoinContainer)
        inProgressIndicatorDivider.setVisible(state.showJoinContainer)

        // Join button
        joinButton.setHidden(state.isJoining)
        joinProgressBar.setVisible(state.isJoining)
        joinContainer.setVisible(state.showJoinContainer)

        // Recordings
        recordingsSection.setVisible(state.showRecordingSection)
        populateRecordings(state.recordings)
    }

    private fun populateRecordings(recordings: List<ConferenceRecordingViewState>) {
        binding.recordingsContainer.removeAllViews()
        recordings.forEach { binding.recordingsContainer.addView(makeRecordingListItem(it)) }
    }

    @SuppressLint("InflateParams")
    private fun makeRecordingListItem(state: ConferenceRecordingViewState): View {
        val binding = AdapterConferenceRecordingItemBinding.inflate(LayoutInflater.from(context), null, false)
        with(binding) {
            binding.root.onClickWithRequireNetwork { consumer?.accept(ConferenceDetailsEvent.RecordingClicked(state.recordingId)) }
            recordingTitle.text = state.title
            recordingDate.text = state.date
            recordingDuration.text = state.duration
            binding.root.isEnabled = !state.isLaunching
            recordingProgressBar.setVisible(state.isLaunching)
            recordingContent.alpha = if (state.isLaunching) 0.35f else 1.0f
        }
        return binding.root
    }

    fun displayRefreshError() {
        Snackbar.make(binding.root, R.string.errorOccurred, Snackbar.LENGTH_SHORT).show()
    }

    fun launchUrl(url: String) {
        val colorSchemeParams = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(canvasContext.color)
            .build()

        var intent = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(colorSchemeParams)
            .setShowTitle(true)
            .build()
            .intent

        intent.data = Uri.parse(url)

        // Exclude Instructure apps from chooser options
        intent = intent.asChooserExcludingInstructure()

        context.startActivity(intent)
    }
}
