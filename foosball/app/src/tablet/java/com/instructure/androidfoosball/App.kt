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
package com.instructure.androidfoosball

import android.app.Application
import android.content.Context
import android.content.Intent
import com.instructure.androidfoosball.services.FoosballSyncService
import com.instructure.androidfoosball.utils.Commentator
import io.realm.Realm
import io.realm.RealmConfiguration

class App : Application() {

    companion object {

        lateinit var context: Context

        val realm: Realm by lazy {
            val realmConfig = RealmConfiguration.Builder()
                    .schemaVersion(1)
                    .deleteRealmIfMigrationNeeded()
                    .build()
            Realm.getInstance(realmConfig)
        }

        val commentator = Commentator()
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        startService(Intent(this, FoosballSyncService::class.java))
        commentator.initialize(this)
        Realm.init(this)
    }

}
