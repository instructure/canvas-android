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

import android.media.AudioManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.instructure.androidfoosball.App
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.*
import com.instructure.androidfoosball.utils.*
import io.realm.RealmList
import kotlinx.android.synthetic.tablet.activity_create_team_twister_game.*
import org.jetbrains.anko.sdk21.listeners.onClick
import org.jetbrains.anko.startActivity
import java.util.*

class CreateTeamTwisterGameActivity : AppCompatActivity() {

    private val mTable = Table.getSelectedTable()
    private val mIncomingNfcRef = FirebaseDatabase.getInstance().reference.child("incoming").child(mTable.id)

    private val DEFAULT_POINTS = 4

    private var points = 0
        set(value) {
            field = value
            pointButton.text = value.toString()
            updateDurationRange()
        }

    private val nfcListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val nfc = dataSnapshot.getValue(IncomingData::class.java) ?: return
            if (nfc.sideOne.isBlank() && nfc.sideTwo.isBlank()) return
            fun getUserById(userId: String): User? = App.realm.where(User::class.java).equalTo("id", userId).findFirst()
            when {
                nfc.sideOne.isNotBlank() -> getUserById(nfc.sideOne)?.let { addUser(it) }
                nfc.sideTwo.isNotBlank() -> getUserById(nfc.sideTwo)?.let { addUser(it) }
            }
            mIncomingNfcRef.setValue(IncomingData())
        }

        override fun onCancelled(databaseError: DatabaseError) { }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_team_twister_game)
        volumeControlStream = AudioManager.STREAM_MUSIC
        setupViews()
        mIncomingNfcRef.addValueEventListener(nfcListener)
    }

    private fun setupViews() {
        points = DEFAULT_POINTS

        // Player selection
        playersLayout.onAddPlayerClicked = { selectPlayer() }

        // On players changed
        playersLayout.onPlayersChanged = { onPlayersChanged() }

        // Points selection
        pointButton.onClick {
            val options = (3..9).toList()
            MaterialDialog.Builder(this)
                    .items(options)
                    .itemsCallback { _, _, i, _ -> points = options[i] }
                    .show()
        }

        // Start game
        startGameButton.onClick { createGame() }

        // Set up QR code
        qrCode.setTableSide(mTable, TableSide.SIDE_1)
    }

    private fun updateDurationRange() {
        val minGoals = 1 + 3 * (points - 1)
        val maxGoals = 1 + 6 * (points - 1)
        durationView.text = getString(R.string.durationRange, minGoals, maxGoals)
    }

    private fun onPlayersChanged() {
        // Show start button if ready
        val ready = playersLayout.players.size == 4
        assignTeamsView.setVisible(!ready)
        startGameButton.setVisible(ready)
    }

    private fun selectPlayer() {
        showUserPicker(this) { addUser(it) }
    }

    private fun addUser(user: User) {
        playersLayout.addUser(user)
        mCommentator.announce(user.customAssignmentPhrase.elseIfBlank(user.name))
    }

    private fun createGame() {
        val players = ArrayList(playersLayout.players)
        Collections.shuffle(players)

        val game = TeamTwisterGame()
        game.status = GameStatus.ONGOING.name
        game.pointsToWin = points
        game.startTime = System.currentTimeMillis()
        game.teams = RealmList(
                TeamWithPoints(users = RealmList(players[0], players[1])),
                TeamWithPoints(users = RealmList(players[0], players[2])),
                TeamWithPoints(users = RealmList(players[0], players[3])),
                TeamWithPoints(users = RealmList(players[1], players[2])),
                TeamWithPoints(users = RealmList(players[1], players[3])),
                TeamWithPoints(users = RealmList(players[2], players[3]))
        )
        game.copyToRealmOrUpdate()

        startActivity<TeamTwisterGameActivity>(TeamTwisterGameActivity.EXTRA_GAME_ID to game.id)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mIncomingNfcRef.removeEventListener(nfcListener)
    }
}
