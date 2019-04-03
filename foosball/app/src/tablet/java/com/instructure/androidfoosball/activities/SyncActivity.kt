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
package com.instructure.androidfoosball.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.instructure.androidfoosball.App
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.Table
import com.instructure.androidfoosball.services.FoosballSyncService
import com.instructure.androidfoosball.utils.Prefs
import kotlinx.android.synthetic.tablet.activity_sync.*

class SyncActivity : AppCompatActivity() {

    private var mIsServiceBound = false
    lateinit private var mFoosballService: FoosballSyncService

    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as FoosballSyncService.FoosballSyncServiceBinder
            mFoosballService = binder.getService()
            mIsServiceBound = true
            requestSync()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mIsServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sync)
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, FoosballSyncService::class.java)
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (mIsServiceBound) {
            unbindService(mServiceConnection)
            mIsServiceBound = false
        }
    }

    private fun requestSync() {
        mFoosballService.requestSync block@ { success ->
            progressbar.visibility = View.GONE
            if (!success) {
                statusTextView.text = "Firebase Error. You should get that fixed."
            } else {

                if (Prefs.tableId.isNotBlank()) {
                    val table = App.realm.where(Table::class.java).equalTo("id", Prefs.tableId).findFirst()
                    if (table == null) {
                        Prefs.tableId = ""
                    } else {
                        startMainActivity()
                        return@block
                    }
                }

                val tables = App.realm.where(Table::class.java).findAll()
                when {
                    tables.isEmpty() -> statusTextView.text = "There are no tables. Please create a table in Firebase."
                    tables.size == 1 -> {
                        Prefs.tableId = tables.first().id
                        startMainActivity()
                    }
                    else -> showTablePicker(tables)
                }
            }
        }
    }

    private fun showTablePicker(tables: List<Table>) {
        statusTextView.text = "Select a table"
        MaterialDialog.Builder(this@SyncActivity)
                .cancelable(false)
                .title(R.string.pick_a_table)
                .items(tables.map { it.name })
                .itemsCallback { dialog, view, which, text ->
                    Toast.makeText(this@SyncActivity, "Selected table: ${tables[which].name}", Toast.LENGTH_SHORT).show()
                    Prefs.tableId = tables[which].id
                    dialog.dismiss()
                    startMainActivity()
                }
                .show()
    }

    private fun startMainActivity() {
        statusTextView.text = "Done"
        startActivity(Intent(this@SyncActivity, MainActivity::class.java))
    }
}
