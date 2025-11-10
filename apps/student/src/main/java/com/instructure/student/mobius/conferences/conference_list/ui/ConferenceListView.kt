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
package com.instructure.student.mobius.conferences.conference_list.ui

import android.app.Activity
import android.net.Uri
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.asChooserExcludingInstructure
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.items
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.withRequireNetwork
import com.instructure.student.R
import com.instructure.student.databinding.FragmentConferenceListBinding
import com.instructure.student.mobius.common.ui.MobiusView
import com.instructure.student.mobius.conferences.conference_details.ui.ConferenceDetailsRepositoryFragment
import com.instructure.student.mobius.conferences.conference_list.ConferenceHeaderType
import com.instructure.student.mobius.conferences.conference_list.ConferenceListEvent
import com.instructure.student.router.RouteMatcher
import com.spotify.mobius.functions.Consumer

class ConferenceListView(
    val canvasContext: CanvasContext,
    inflater: LayoutInflater,
    parent: ViewGroup
) : MobiusView<ConferenceListViewState, ConferenceListEvent, FragmentConferenceListBinding>(
    inflater,
    FragmentConferenceListBinding::inflate,
    parent
) {

    private lateinit var listAdapter: ConferenceListAdapter

    init {
        binding.toolbar.setupAsBackButton { (context as? Activity)?.onBackPressed() }
        binding.toolbar.subtitle = canvasContext.name

        // Set up menu
        with(binding.toolbar.menu.add(0, R.id.openExternallyButton, 0, R.string.openInBrowser)) {
            setIcon(R.drawable.ic_open_in_browser)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setOnMenuItemClickListener {
                (context as? FragmentActivity)?.withRequireNetwork {
                    consumer?.accept(ConferenceListEvent.LaunchInBrowser)
                }
                true
            }
        }
    }

    override fun applyTheme() {
        ViewStyler.themeToolbarColored(context as Activity, binding.toolbar, canvasContext)
    }

    override fun onConnect(output: Consumer<ConferenceListEvent>) {
        binding.swipeRefreshLayout.setOnRefreshListener { output.accept(ConferenceListEvent.PullToRefresh) }
        listAdapter = ConferenceListAdapter(object : ConferenceListAdapterCallback {
            override fun onConferenceClicked(conferenceId: Long) {
                output.accept(ConferenceListEvent.ConferenceClicked(conferenceId))
            }

            override fun reload() = output.accept(ConferenceListEvent.PullToRefresh)

            override fun onHeaderClicked(headerType: ConferenceHeaderType) {
                output.accept(ConferenceListEvent.HeaderClicked(headerType))
            }
        })
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = listAdapter
    }

    override fun onDispose() = Unit

    override fun render(state: ConferenceListViewState) {
        binding.swipeRefreshLayout.isRefreshing = state is ConferenceListViewState.Loading

        binding.launchInBrowserProgressBar.setVisible(state.isLaunchingInBrowser)
        binding.toolbar.menu.items.first().isVisible = !state.isLaunchingInBrowser

        when (state) {
            is ConferenceListViewState.Loading -> Unit
            is ConferenceListViewState.Loaded -> {
                listAdapter.data = state.itemStates
            }
        }.exhaustive
    }

    fun showConferenceDetails(conference: Conference) {
        RouteMatcher.route(activity as FragmentActivity, ConferenceDetailsRepositoryFragment.makeRoute(canvasContext, conference))
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
