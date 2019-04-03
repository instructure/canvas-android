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

package com.instructure.androidfoosball.holders


import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.interfaces.FragmentCallbacks
import com.instructure.androidfoosball.models.Table
import com.instructure.androidfoosball.models.User
import com.instructure.androidfoosball.utils.Prefs
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.phone.adapter_table.view.*


class TableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    
    fun bind(context: Context?, table: Table, adapterCallback: (Int) -> Unit) {
        
        itemView.tableLabel.text = table.name
        val currentGame = table.currentGame

        if (context != null) {
            when (currentGame) {
                "FREE" -> {
                    //Table not busy
                    itemView.tableStatusResult.text = context.getString(R.string.status_free)
                    itemView.cardGameState.visibility = View.GONE
                }
                "TABLE_KING" -> {
                    itemView.tableStatusResult.text = context.getString(R.string.tableKing)
                    itemView.cardGameState.visibility = View.GONE
                }
                "TEAM_TWISTER" -> {
                    itemView.tableStatusResult.text = context.getString(R.string.teamTwister)
                    itemView.cardGameState.visibility = View.GONE
                }
                "BUSY" -> {
                    //Table busy
                    itemView.tableStatusResult.text = context.getString(R.string.status_busy)
                    itemView.cardGameState.visibility = View.VISIBLE
                    itemView.bestOfCount.text = table.currentBestOf
                    itemView.roundCount.text = table.currentRound
                    itemView.pointsCount.text = table.currentPointsToWin
                    itemView.teamOneScore.text = table.currentScoreTeamOne
                    itemView.teamTwoScore.text = table.currentScoreTeamTwo

                    // Reset avatar views
                    with(itemView) { listOf(playerOne, playerTwo, playerThree, playerFour).forEach { it.visibility = View.INVISIBLE }}

                    safeLet(table.teamOne, table.teamTwo) { teamOne, teamTwo ->
                        fun setTeamAvatars(users: List<User>, vararg views: CircleImageView) {
                            for ( (user, view) in users.zip(views)) {
                                view.visibility = View.VISIBLE
                                if (user.avatar.isNullOrBlank()) {
                                    view.setImageResource(R.drawable.sadpanda)
                                } else {
                                    Picasso.with(view.context).load(user.avatar).error(R.drawable.sadpanda).into(view)
                                }
                            }
                        }
                        setTeamAvatars(teamOne.users.reversed(), itemView.playerTwo, itemView.playerOne)
                        setTeamAvatars(teamTwo.users, itemView.playerThree, itemView.playerFour)
                    }

                    itemView.buttonNotifyWhenDone.setOnClickListener {
                        if (!table.pushId.isBlank()) {
                            FirebaseMessaging.getInstance().subscribeToTopic(table.pushId)
                        } else {
                            Log.e("push", "Table PushId was null cannot subscribe to topic")
                        }
                        (context as? FragmentCallbacks)?.mUser?.let { user ->
                            val ref = FirebaseDatabase.getInstance().reference.child("incoming").child(table.id)
                            ref.updateChildren(
                                mapOf(
                                    "tableRequestUserId" to user.id,
                                    "tableRequestTime" to System.currentTimeMillis().toString()
                                )
                            )
                        }
                    }

                }
                else -> {
                    itemView.tableStatusResult.text = context.getString(R.string.status_unknown)
                    itemView.cardGameState.visibility = View.GONE
                }
            }

            if (Prefs(context).preferredTableId == table.pushId)
                itemView.preferredTable.visibility = View.VISIBLE
            else itemView.preferredTable.visibility = View.GONE

            itemView.rootView.setOnLongClickListener {
                Prefs(context).preferredTableId = table.pushId
                itemView.preferredTable.visibility = View.VISIBLE
                adapterCallback(adapterPosition)
                true
            }
        }
    }

    fun <T1: Any, T2: Any> safeLet(p1: T1?, p2: T2?, block: (T1, T2)-> Unit): Boolean {
        if (p1 != null && p2 != null) {
            block(p1, p2)
            return true
        }
        return false
    }
}
