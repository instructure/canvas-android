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
package com.instructure.student.activity

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.instructure.canvasapi2.managers.CommunicationChannelsManager
import com.instructure.canvasapi2.models.CommunicationChannel
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.student.R
import com.instructure.student.adapter.NotificationPreferencesRecyclerAdapter
import kotlinx.android.synthetic.main.notification_preferences_activity.*
import kotlinx.android.synthetic.main.notification_preferences_activity.listView as recyclerView

@PageView(url = "profile/communication")
class NotificationPreferencesActivity : AppCompatActivity() {

    private lateinit var adapter: NotificationPreferencesRecyclerAdapter

    private var pushChannel: CommunicationChannel? = null

    private var apiCalls: WeaveJob? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notification_preferences_activity)
        setupToolbar()
        configureRecyclerView()
        savedInstanceState?.getParcelable<CommunicationChannel>(Const.ITEM)?.let {
            pushChannel = it
            adapter.fetchNotificationPreferences(it)
        } ?: fetchCommunicationChannels()
    }

    private fun setupToolbar() {
        toolbar.setupAsBackButton { finish() }
        ViewStyler.themeToolbar(this, toolbar, Color.WHITE, Color.BLACK, false)
    }

    fun configureRecyclerView() {
        adapter = NotificationPreferencesRecyclerAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.isSelectionEnabled = false
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setEmptyView(emptyView)
    }

    private fun fetchCommunicationChannels() {
        apiCalls?.cancel()
        apiCalls = tryWeave {
            val channels = awaitApi<List<CommunicationChannel>> { CommunicationChannelsManager.getCommunicationChannels(ApiPrefs.user!!.id, it, false) }
            pushChannel = channels.first { "push".equals(it.type, true) }
            adapter.fetchNotificationPreferences(pushChannel!!)
        } catch {
            Toast.makeText(this, R.string.pushNotificationsError, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (pushChannel != null) outState.putParcelable(Const.ITEM, pushChannel)
    }

    override fun onDestroy() {
        super.onDestroy()
        apiCalls?.cancel()
        adapter.cancel()
    }

}
