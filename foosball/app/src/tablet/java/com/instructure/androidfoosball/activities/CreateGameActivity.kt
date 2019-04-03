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

import android.graphics.Color
import android.media.AudioManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.database.*
import com.instructure.androidfoosball.App
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.*
import com.instructure.androidfoosball.utils.*
import com.instructure.androidfoosball.views.ConfirmPinDialog
import com.instructure.androidfoosball.views.TeamLayout
import kotlinx.android.synthetic.tablet.activity_create_game.*
import org.jetbrains.anko.sdk21.listeners.onClick
import org.jetbrains.anko.startActivity

class CreateGameActivity : AppCompatActivity() {

    private val mTable = Table.getSelectedTable()
    private val mIncomingNfcRef = FirebaseDatabase.getInstance().reference.child("incoming").child(mTable.id)
    private val mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference

    private val BEST_OF_DEFAULT = 3
    private val POINTS_DEFAULT = 5

    private var bestOf = 0
        set(value) {
            field = value
            bestOfButton.text = value.toString()
            updateDurationRange()
        }

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
                nfc.sideOne.isNotBlank() -> getUserById(nfc.sideOne)?.let { addUser(it, teamOneLayout) }
                nfc.sideTwo.isNotBlank() -> getUserById(nfc.sideTwo)?.let { addUser(it, teamTwoLayout) }
            }
            mIncomingNfcRef.setValue(IncomingData())
        }

        override fun onCancelled(databaseError: DatabaseError) { }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_game)
        volumeControlStream = AudioManager.STREAM_MUSIC
        setupViews()
        mIncomingNfcRef.addValueEventListener(nfcListener)
    }

    private fun setupViews() {
        // Set initial team names
        teamOneNameView.setText(mTable.sideOneName)
        teamTwoNameView.setText(mTable.sideTwoName)

        // Set team colors
        teamOneLayout.setTeamColor(Color.parseColor(mTable.sideOneColor))
        teamTwoLayout.setTeamColor(Color.parseColor(mTable.sideTwoColor))

        // Player selection listeners
        teamOneLayout.onAddPlayerClicked = { selectPlayer(teamOneLayout) }
        teamTwoLayout.onAddPlayerClicked = { selectPlayer(teamTwoLayout) }

        // Player selection listeners
        teamOneLayout.onAddTeamClicked = { selectTeam(teamOneLayout) }
        teamTwoLayout.onAddTeamClicked = { selectTeam(teamTwoLayout) }

        // Team changed listeners
        teamOneLayout.onTeamChanged = { onTeamChanged(TableSide.SIDE_1) }
        teamTwoLayout.onTeamChanged = { onTeamChanged(TableSide.SIDE_2) }

        // Setup QR codes
        teamOneQR.setTableSide(mTable, TableSide.SIDE_1)
        teamTwoQR.setTableSide(mTable, TableSide.SIDE_2)

        // Best of selection
        bestOf = BEST_OF_DEFAULT
        bestOfButton.onClick {
            val options = (1..9 step 2).toList()
            MaterialDialog.Builder(this)
                    .items(options)
                    .itemsCallback { _, _, i, _ -> bestOf = options[i] }
                    .show()
        }

        // Points selection
        points = POINTS_DEFAULT
        pointButton.onClick {
            val options = (3..15).toList()
            MaterialDialog.Builder(this)
                    .items(options)
                    .itemsCallback { _, _, i, _ -> points = options[i] }
                    .show()
        }

        // Start game
        startGameButton.onClick {
            if (teamOneLayout.team.users.size != teamTwoLayout.team.users.size) {
                MaterialDialog.Builder(this)
                        .title(R.string.uneven_teams)
                        .content(R.string.uneven_teams_content)
                        .positiveText(android.R.string.yes)
                        .onPositive { _, _ -> createGame() }
                        .negativeText(android.R.string.no)
                        .show()
            } else {
                createGame()
            }
        }
    }

    private fun updateDurationRange() {
        val minGoals = points * (bestOf / 2 + 1)
        val maxGoals = bestOf * (points * 2 - 1)
        durationView.text = getString(R.string.durationRange, minGoals, maxGoals)
    }

    private fun onTeamChanged(side: TableSide) {
        // Update average team win rates
        winRateTeamOne.text = if (teamOneLayout.hasUsers()) getString(R.string.avg_win_rate_formatted).format(teamOneLayout.team.getAverageWinRate()) else ""
        winRateTeamTwo.text = if (teamTwoLayout.hasUsers()) getString(R.string.avg_win_rate_formatted).format(teamTwoLayout.team.getAverageWinRate()) else ""

        if (side == TableSide.SIDE_1) {
            // Update team one custom name
            teamOneNameView.setText(teamOneLayout.team.teamName.elseIfBlank(mTable.sideOneName))
        } else {
            // Update team two custom name
            teamTwoNameView.setText(teamTwoLayout.team.teamName.elseIfBlank(mTable.sideTwoName))
        }


        // Show start button if ready
        val ready = teamOneLayout.hasUsers() && teamTwoLayout.hasUsers()
        assignTeamsView.setVisible(!ready)
        startGameButton.setVisible(ready)
    }

    private fun selectPlayer(teamLayout: TeamLayout) {
        showUserPicker(this) {
            ConfirmPinDialog(this, it) { confirmedUser ->
                addUser(confirmedUser, teamLayout)
            }.show()
        }
    }

    private fun selectTeam(teamLayout: TeamLayout) {
        showTeamPicker(this) {
            ConfirmPinDialog(this, it) { confirmedUser ->
                addUser(confirmedUser, teamLayout)
            }.show()
        }
    }

    private fun addUser(user: User, teamLayout: TeamLayout) {
        (if (teamLayout == teamOneLayout) teamTwoLayout else teamOneLayout).removeUser(user)
        if (teamLayout.addUser(user)) {
            mCommentator.announcePlayerAssignment(
                    user,
                    if (teamLayout == teamOneLayout) mTable.sideOneName else mTable.sideTwoName
            )
        }
    }

    private fun createGame() {

        // Set/update team one custom name
        val teamOne = teamOneLayout.team as Team
        teamOneNameView.text.toString().apply {
            if (isNotBlank() && this != mTable.sideOneName) {
                saveOrUpdateTeam(this, teamOne.users.map(User::id))
            }
        }

        // Set/update team two custom name
        val teamTwo = teamTwoLayout.team as Team
        teamTwoNameView.text.toString().apply {
            if (isNotBlank() && this != mTable.sideTwoName) {
                saveOrUpdateTeam(this, teamTwo.users.map(User::id))
            }
        }

        // Create first round
        val round = Round(
                pointsToWin = points,
                sideOneTeam = teamOne,
                sideTwoTeam = teamTwo,
                startTime = System.currentTimeMillis()
        )

        // Create and save game
        val game = Game()
        game.bestOf = bestOf
        game.rounds.add(round)
        game.startTime = round.startTime
        game.status = GameStatus.ONGOING.name
        game.teamOne = teamOne
        game.teamTwo = teamTwo
        game.copyToRealmOrUpdate()

        startActivity<GameActivity>(GameActivity.EXTRA_GAME_ID to game.id)
        finish()

    }

    private fun saveOrUpdateTeam(teamName: String, userIds: List<String>) {
        val teamHash = userIds.getTeamHash()
        val team = App.realm.where(RealmTeam::class.java).equalTo("id", teamHash).findFirst()
        when {
        // Create new team if it doesn't exist
            team == null -> {
                val newTeam = CustomTeam(teamHash, teamName, 0L, 0L, userIds)
                App.realm.inTransaction { copyToRealmOrUpdate(newTeam.toRealmTeam()) }
                mDatabase.child("customTeams").child(teamHash).setValue(newTeam)
            }
        // Update team name if it has changed
            teamName != team.teamName -> {
                team.edit { this.teamName = teamName }
                mDatabase.child("customTeams").child(teamHash).child("teamName").setValue(team.teamName)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mIncomingNfcRef.removeEventListener(nfcListener)
    }
}
