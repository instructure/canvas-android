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

import android.os.Handler
import android.util.Log
import com.google.android.gms.wearable.DataMap
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.instructure.androidfoosball.utils.FireUtils
import com.instructure.androidfoosball.utils.Prefs
import com.instructure.wearutils.WearConst
import com.instructure.wearutils.interfaces.WearableCallbacks
import com.instructure.wearutils.models.DataPage
import com.instructure.wearutils.services.BaseWearService
import java.util.*


class WearService : BaseWearService(), WearableCallbacks {

    override fun onCreate() {
        super.onCreate()
        Log.d("wear", "Wear Service Created")
        setCallbacks(this)
    }

    override fun OnSyncDataItemTask(dataMap: DataMap) {
        Log.d("wear", "OnSyncDataItemTask()")
        Handler().post { /* Action requests from wear device */ }
    }

    override fun OnGetMessageTask(messagePath: String) {
        Log.d("wear", "OnGetMessageTask()")
        Handler().post {
            //Data request from wear device
            if (WearConst.WEAR_DATA_REQUEST == messagePath) {

                val userId = Prefs(applicationContext).userId
                val database = FirebaseDatabase.getInstance().reference

                FireUtils.getWinCount(userId, database) { value ->
                    Log.d("wear", "Sending win count data: " + value)
                    syncString(WearConst.DATA_ITEM_WIN_COUNT, value.toString())
                }

                FireUtils.getLossCount(userId, database) { value ->
                    Log.d("wear", "Sending loss count data: " + value)
                    syncString(WearConst.DATA_ITEM_LOSS_COUNT, value.toString())
                }

                FireUtils.getTables(database) { values ->
                    Log.d("wear", "Sending table data")

                    val type = object : TypeToken<List<DataPage>>() {

                    }.type
                    val pages = ArrayList<DataPage>(values.size)

                    for (table in values) {
                        pages.add(DataPage(table.name, table.currentGame, 0, DataPage.TABLE))
                    }

                    val json = Gson().toJson(pages, type)
                    syncString(WearConst.DATA_ITEM_TABLES, json)
                }
            }
        }
    }
}
