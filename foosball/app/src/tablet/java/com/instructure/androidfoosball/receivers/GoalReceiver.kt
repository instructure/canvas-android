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
package com.instructure.androidfoosball.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.instructure.androidfoosball.ktmodels.TableSide

class GoalReceiver(val onGoal: (TableSide) -> Unit) : BroadcastReceiver() {

    companion object {
        val GOAL_ACTION = "action_goal"
        val EXTRA_SIDE = "scoringSide"

        fun getGoalIntent(side: TableSide) = Intent(GOAL_ACTION).apply {
            putExtra(EXTRA_SIDE, side.name)
        }

        fun sendGoal(context: Context, side: TableSide) {
            context.sendOrderedBroadcast(getGoalIntent(side), null)
        }

        fun register(context: Context, receiver: GoalReceiver, priority: Int = 0) {
            val filter = IntentFilter(GOAL_ACTION)
            filter.priority = priority
            context.registerReceiver(receiver, filter)
        }

        fun unregister(context: Context, receiver: GoalReceiver) {
            context.unregisterReceiver(receiver)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == GOAL_ACTION) {
            val sideString = intent.getStringExtra(EXTRA_SIDE)
            onGoal(TableSide.valueOf(sideString))
            abortBroadcast()
        }
    }
}
