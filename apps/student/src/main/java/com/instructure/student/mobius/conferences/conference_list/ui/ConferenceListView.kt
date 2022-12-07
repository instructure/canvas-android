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
import androidx.recyclerview.widget.LinearLayoutManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.mobius.common.ui.MobiusView
import com.instructure.student.mobius.conferences.conference_details.ui.ConferenceDetailsFragment
import com.instructure.student.mobius.conferences.conference_list.ConferenceListEvent
import com.instructure.student.router.RouteMatcher
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.fragment_conference_list.*

class ConferenceListView(val canvasContext: CanvasContext, inflater: LayoutInflater, parent: ViewGroup) :
    MobiusView<ConferenceListViewState, ConferenceListEvent>(R.layout.fragment_conference_list, inflater, parent) {

    lateinit var listAdapter: ConferenceListAdapter

    init {
        toolbar.setupAsBackButton { (context as? Activity)?.onBackPressed() }
        toolbar.subtitle = canvasContext.name

        // Set up menu
        with(toolbar.menu.add(0, R.id.openExternallyButton, 0, R.string.openInBrowser)){
            setIcon(R.drawable.ic_open_in_browser)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setOnMenuItemClickListener {
                consumer?.accept(ConferenceListEvent.LaunchInBrowser)
                true
            }
        }
    }

    override fun applyTheme() {
        ViewStyler.themeToolbarColored(context as Activity, toolbar, canvasContext)
    }

    override fun onConnect(output: Consumer<ConferenceListEvent>) {
        swipeRefreshLayout.setOnRefreshListener { output.accept(ConferenceListEvent.PullToRefresh) }
        listAdapter =
            ConferenceListAdapter(
                object :
                    ConferenceListAdapterCallback {
                    override fun onConferenceClicked(conferenceId: Long) {
                        output.accept(
                            ConferenceListEvent.ConferenceClicked(
                                conferenceId
                            )
                        )
                    }

                    override fun reload() =
                        output.accept(ConferenceListEvent.PullToRefresh)
                })
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = listAdapter
    }

    override fun onDispose() = Unit

    override fun render(state: ConferenceListViewState) {
        swipeRefreshLayout.isRefreshing = state is ConferenceListViewState.Loading

        launchInBrowserProgressBar.setVisible(state.isLaunchingInBrowser)
        toolbar.menu.items.first().isVisible = !state.isLaunchingInBrowser

        when (state) {
            is ConferenceListViewState.Loading -> Unit
            is ConferenceListViewState.Loaded -> {
                listAdapter.data = state.itemStates
            }
        }.exhaustive
    }

    fun showConferenceDetails(conference: Conference) {
        RouteMatcher.route(context, ConferenceDetailsFragment.makeRoute(canvasContext, conference))
    }

    fun launchUrl(url: String) {
        val colorSchemeParams = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(canvasContext.backgroundColor)
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
