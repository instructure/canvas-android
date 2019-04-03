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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.instructure.androidfoosball.R
import kotlinx.android.synthetic.tablet.activity_elo_layout.*
import java.util.*

class EloDialogActivity : AppCompatActivity() {

    companion object {
        fun createIntent(context: Context, data: HashMap<String, Int>) : Intent {
            val intent = Intent(context, EloDialogActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable("HashMap", data)
            intent.putExtras(bundle)

            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_elo_layout)

        val dataSet: HashMap<String, Int> = intent.extras.get("HashMap") as HashMap<String, Int>

        if(dataSet.isEmpty()) {
            foosRankView.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
        } else {
            foosRankView.setData(dataSet)
            foosRankView.visibility = View.VISIBLE
            emptyText.visibility = View.GONE
        }
    }
}
