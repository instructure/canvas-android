/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.instructure.androidfoosball.wear

import android.app.Activity
import android.app.Fragment
import android.app.FragmentManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.wearable.view.FragmentGridPagerAdapter
import android.support.wearable.view.WatchViewStub
import android.util.Log
import android.view.View
import com.google.android.gms.wearable.DataMap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.instructure.wearutils.WearClient
import com.instructure.wearutils.WearConst
import com.instructure.wearutils.interfaces.DataRequest
import com.instructure.wearutils.interfaces.WearableCallbacks
import com.instructure.wearutils.models.DataPage
import com.instructure.wearutils.models.DataRow
import kotlinx.android.synthetic.main.rect_activity_wear.*
import java.util.*

class WearActivity : Activity(), WearClient.OnConnectedListener, WearableCallbacks, DataRequest {

    private val mWearClient: WearClient by lazy { WearClient(this, this, this) }
    private val mJsonData = HashMap<String, String>(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wear)
        val stub = findViewById(R.id.watch_view_stub) as WatchViewStub
        stub.setOnLayoutInflatedListener { stub ->
            progress.visibility = View.VISIBLE
        }
    }

    private inner class PagerAdapter(fm: FragmentManager, winCount: String, lossCount: String, tables: List<DataPage>) : FragmentGridPagerAdapter(fm) {

        private val mPages = ArrayList<DataRow>()

        init {
            initPages(winCount, lossCount, tables)
        }

        private fun initPages(winCount: String, lossCount: String, tables: List<DataPage>) {
            val row1 = DataRow()
            row1.addPages(DataPage(getString(R.string.wins), winCount, R.drawable.page_bg_1, DataPage.WIN_LOSS))
            row1.addPages(DataPage(getString(R.string.losses), lossCount, R.drawable.page_bg_2, DataPage.WIN_LOSS))

            val row2 = DataRow()
            for (page in tables) {
                if ("FREE" == page.mText) {
                    page.mBackgroundId = R.drawable.page_bg_1
                    page.mText = getString(R.string.status_free)
                } else if ("BUSY" == page.mText) {
                    page.mBackgroundId = R.drawable.page_bg_2
                    page.mText = getString(R.string.status_busy)
                } else {
                    page.mBackgroundId = R.drawable.page_bg_1
                    page.mText = getString(R.string.status_unknown)
                }
                row2.addPages(page)
            }

            mPages.add(row1)
            mPages.add(row2)
        }

        override fun getFragment(row: Int, col: Int): Fragment {
            val page = mPages[row].getPages(col)
            if (page.type == DataPage.WIN_LOSS) {
                return WinLossCardFragment.newInstance(page.mTitle, page.mText)
            } else {
                return TableCardFragment.newInstance(page.mTitle, page.mText)
            }
        }

        override fun getBackgroundForPage(row: Int, col: Int): Drawable {
            val page = mPages[row].getPages(col)
            return ContextCompat.getDrawable(this@WearActivity, page.mBackgroundId)
        }

        override fun getRowCount(): Int {
            return mPages.size
        }

        override fun getColumnCount(row: Int): Int {
            return mPages[row].size()
        }
    }

    public override fun onStart() {
        super.onStart()
        mWearClient.connect()
    }

    public override fun onStop() {
        super.onStop()
        mWearClient.disconnect()
    }

    //region Data Request

    override fun onClientConnected() {
        mJsonData.clear()
        Log.d("wear", "Requesting data...")
        mWearClient.sendMessage(WearConst.WEAR_DATA_REQUEST, null)
    }

    override fun onConnectionFailed() {
    }

    override fun sendDataRequest(requestType: String, json: String) {
    }

    override fun OnSyncDataItemTask(dataMap: DataMap) {
        if (dataMap.containsKey(WearConst.DATA_ITEM_WIN_COUNT)) {
            val json = dataMap.getString(WearConst.DATA_ITEM_WIN_COUNT)
            Log.d("wear", "Got win count: " + json)
            mJsonData.put(WearConst.DATA_ITEM_WIN_COUNT, json)
            dataReadyCheck()
        } else if (dataMap.containsKey(WearConst.DATA_ITEM_LOSS_COUNT)) {
            val json = dataMap.getString(WearConst.DATA_ITEM_LOSS_COUNT)
            Log.d("wear", "Got loss count: " + json)
            mJsonData.put(WearConst.DATA_ITEM_LOSS_COUNT, json)
            dataReadyCheck()
        } else if (dataMap.containsKey(WearConst.DATA_ITEM_TABLES)) {
            val json = dataMap.getString(WearConst.DATA_ITEM_TABLES)
            Log.d("wear", "Got tables: " + json)
            mJsonData.put(WearConst.DATA_ITEM_TABLES, json)
            dataReadyCheck()
        }
    }

    override fun OnGetMessageTask(messagePath: String) {
    }

    private fun dataReadyCheck() {
        if (mJsonData.size >= 3) {

            var winCount = mJsonData[WearConst.DATA_ITEM_WIN_COUNT]
            var lossCount = mJsonData[WearConst.DATA_ITEM_LOSS_COUNT]
            val tableJson = mJsonData[WearConst.DATA_ITEM_TABLES]

            if (winCount.isNullOrBlank()) {
                winCount = 0.toString()
            }

            if (lossCount.isNullOrBlank()) {
                lossCount = 0.toString()
            }

            val type = object : TypeToken<List<DataPage>>() {

            }.type
            val tables = Gson().fromJson<List<DataPage>>(tableJson, type)

            pager.adapter = PagerAdapter(fragmentManager, winCount!!, lossCount!!, tables)
            progress.visibility = View.INVISIBLE
        }
    }

    //endregion
}
