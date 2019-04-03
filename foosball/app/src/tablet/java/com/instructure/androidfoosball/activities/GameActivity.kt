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

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.media.AudioManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.database.*
import com.instructure.androidfoosball.App
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.*
import com.instructure.androidfoosball.push.PushIntentService
import com.instructure.androidfoosball.receivers.GoalReceiver
import com.instructure.androidfoosball.utils.*
import com.instructure.androidfoosball.views.TableRequestedDialog
import com.instructure.androidfoosball.views.WinGameDialog
import com.instructure.androidfoosball.views.WinRoundDialog
import kotlinx.android.synthetic.tablet.activity_game.*
import org.jetbrains.anko.sdk21.listeners.onClick
import org.jetbrains.anko.textColor
import java.util.*


class GameActivity : AppCompatActivity() {

    private val table = Table.getSelectedTable()
    private val incomingDataRef = FirebaseDatabase.getInstance().reference.child("incoming").child(table.id)

    private val mGameId by lazy { intent.getStringExtra(EXTRA_GAME_ID) ?: "" }
    private val mGame by lazy { App.realm.where(Game::class.java).equalTo("id", mGameId).findFirst()!! }
    private val mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val mTable = Table.getSelectedTable()

    private val nfcListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            TableRequestedDialog.showIfNecessary(incomingDataRef, dataSnapshot, supportFragmentManager)
        }

        override fun onCancelled(databaseError: DatabaseError) { }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        volumeControlStream = AudioManager.STREAM_MUSIC
        setupViews()
        setupRound()
        GoalReceiver.register(this, goalReceiver, 100)
        updateGameStatusBusy()
        incomingDataRef.addValueEventListener(nfcListener)
    }

    @SuppressLint("Range")
    private fun setupViews() {

        // Get table colors
        val sideOneColor = Color.parseColor(mTable.sideOneColor)
        val sideTwoColor = Color.parseColor(mTable.sideTwoColor)

        // Set team name colors
        teamOneName.textColor = sideOneColor
        teamTwoName.textColor = sideTwoColor

        // Set TeamLayout colors
        teamOneLayout.setTeamColor(sideOneColor)
        teamTwoLayout.setTeamColor(sideTwoColor)

        // Pause game
        pauseGameButton.onClick {
            shortToast(R.string.game_paused)
            updateGameStatusFree()
            finish()
        }

        // Quit game
        quitGameButton.onClick {
            MaterialDialog.Builder(this)
                    .title(R.string.quit_game)
                    .content(R.string.confirm_quit_game)
                    .negativeText(android.R.string.cancel)
                    .positiveText(R.string.quit_game)
                    .onPositive { _, _ ->
                        mGame.edit { status = GameStatus.CANCELED.name }
                        updateGameStatusFree()
                        finish()
                    }
                    .show()
        }

        // Undo last goal
        undoView.onClick { undoGoal() }

        // Tap team to count a goal
        teamOneLayout.onClick { goalReceiver.onGoal(TableSide.SIDE_1) }
        teamTwoLayout.onClick { goalReceiver.onGoal(TableSide.SIDE_2) }

    }

    private fun undoGoal() {
        if (mGame.currentRound().goalHistory.isNotEmpty()) {
            val shameTeam = mGame.currentRound().goalHistory.last().team ?: return
            val opposingTeam = if (shameTeam == mGame.teamOne) mGame.teamTwo!! else mGame.teamOne!!
            mCommentator.announceUndoGoal(shameTeam, opposingTeam, mGame.currentRound(), mTable)
            mGame.currentRound().goalHistory.edit { if (isNotEmpty()) remove(last()) }
            refreshScore()
        }
    }

    private fun nextRound(incrementBestOf: Boolean = false) {

        val thisRound = mGame.currentRound()

        // Update player stats
        updateTeamStats(thisRound.getWinningTeam()!!.users, thisRound.getLosingTeam()!!.users)

        // Create next round
        val nextRound = Round(
                pointsToWin = thisRound.pointsToWin,
                sideOneTeam = thisRound.sideTwoTeam,
                sideTwoTeam = thisRound.sideOneTeam,
                startTime = System.currentTimeMillis()
        )

        // Save changes to Realm
        App.realm.inTransaction {
            if (incrementBestOf) mGame.bestOf += 2
            thisRound.endTime = System.currentTimeMillis()
            mGame.rounds.add(nextRound)
        }

        // Update UI with new round
        setupRound()
    }

    private fun setupRound() {
        val round = mGame.currentRound()

        teamOneName.text = round.sideOneTeam?.teamName.elseIfBlank(mTable.sideOneName)
        teamTwoName.text = round.sideTwoTeam?.teamName.elseIfBlank(mTable.sideTwoName)

        teamOneLayout.team = round.sideOneTeam!!
        teamTwoLayout.team = round.sideTwoTeam!!

        roundNumberView.text = mGame.rounds.size.toString()
        maxRoundsView.text = mGame.bestOf.toString()
        pointsToWinView.text = round.pointsToWin.toString()

        setWinStars(teamOneStarsContainer, mGame.getTeamWinCount(round.sideOneTeam!!))
        setWinStars(teamTwoStarsContainer, mGame.getTeamWinCount(round.sideTwoTeam!!))

        roundTimerView.setStartTime(round.startTime)
        gameTimerView.setStartTime(mGame.startTime)

        refreshScore()
        mCommentator.announceGameStart()
    }

    private fun setWinStars(container: LinearLayout, count: Int) {
        container.removeAllViews()
        val dimen = 32f.dp().toInt()
        val params = LinearLayout.LayoutParams(dimen, dimen)
        kotlin.repeat(count) {
            val v = ImageView(this)
            v.setImageResource(R.drawable.vd_star_amber_a400_48dp)
            container.addView(v, params)
        }
    }

    private val goalReceiver: GoalReceiver = GoalReceiver { side ->
        if (mGame.currentRound().hasWinner()) return@GoalReceiver
        mGame.currentRound().recordGoal(side)
        mCommentator.announceGoal(side, mGame.currentRound(), mTable)
        refreshScore()
    }

    private fun refreshScore() {
        val round = mGame.currentRound()

        // Get scores
        val scoreTeamOne = round.getScore(round.sideOneTeam!!)
        val scoreTeamTwo = round.getScore(round.sideTwoTeam!!)

        // Update score views
        teamOneScore.text = scoreTeamOne.toString()
        teamTwoScore.text = scoreTeamTwo.toString()

        if (scoreTeamOne + scoreTeamTwo > 0) {
            undoView.visibility = View.VISIBLE
            goalTimerView.setStartTime(round.goalHistory.last().time)
        } else {
            undoView.visibility = View.GONE
            goalTimerView.setStartTime(round.startTime)
        }

        val servingSide = mGame.getServingSide()
        teamOneServingIndicator.visibility = if (servingSide == TableSide.SIDE_1) View.VISIBLE else View.GONE
        teamTwoServingIndicator.visibility = if (servingSide == TableSide.SIDE_2) View.VISIBLE else View.GONE

        if (mGame.hasWinner()) {
            mGame.getWinningTeam()!!.users.map(User::customVictoryPhrase).filter(String::isNotBlank).apply {
                if (isNotEmpty()) mCommentator.queueAnnounce(joinToString(". "))
            }
            WinGameDialog(this, mGame, { nextRound(true) }, { undoGoal() }, { endGame() }).show()
        } else if (round.hasWinner()) {
            WinRoundDialog(this, mGame, { nextRound() }, { undoGoal() }).show()
        }

        setGameRoundStats(mGame)
    }

    private fun endGame() {
        updateTeamStats(mGame.currentRound().getWinningTeam()!!.users, mGame.currentRound().getLosingTeam()!!.users)
        mGame.edit { status = GameStatus.FINISHED.name }
        mGame.currentRound().edit { endTime = System.currentTimeMillis() }
        updateGameStatusFree()

        finish()
    }

    override fun onStart() {
        super.onStart()
        val am = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getService(this, 0, PushIntentService.getIntent(this, mTable.pushId, mTable.name), PendingIntent.FLAG_CANCEL_CURRENT)
        am.cancel(pi)
    }

    override fun onStop() {
        super.onStop()
        val am = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getService(this, 0, PushIntentService.getIntent(this, mTable.pushId, mTable.name), PendingIntent.FLAG_CANCEL_CURRENT)
        am.set(AlarmManager.RTC_WAKEUP, 90000, pi)
    }

    override fun onDestroy() {
        super.onDestroy()
        GoalReceiver.unregister(this, goalReceiver)
        incomingDataRef.removeEventListener(nfcListener)
    }

    private fun updateTeamStats(winningTeam: List<User>, losingTeam: List<User>) {
        mDatabase.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val allPlayerIds = (winningTeam + losingTeam).map(User::id)

                // Get fresh users
                val userData = dataSnapshot.children.filter { allPlayerIds.contains(it.key) }
                val users = userData.map { it.getValue(User::class.java)?.apply { id = it.key } }

                // Grab fresh winners and losers
                val winners = users.filterNotNull().filter { user -> winningTeam.any { it.id == user.id } }
                val losers = users.filterNotNull().filter { user -> losingTeam.any { it.id == user.id } }

                // Update FoosRanking
                RankingUtils.updateFoosRankings(winners, losers)

                // Increment wins
                winners.forEach { it.wins++ }
                losers.forEach { it.losses++ }

                // Post changes to firebase
                val updateMap = (winners + losers).map { createPlayerStatsMap(it) }.reduce { stats1, stats2 -> stats1 + stats2 }
                mDatabase.updateChildren(updateMap)
            }

            override fun onCancelled(databaseError: DatabaseError) { }
        })

        //don't worry about teams with guests
        (winningTeam + losingTeam).forEach { if(it.guest) return }

        // Skip if either team has fewer than two players
        if (listOf(winningTeam, losingTeam).any { it.size < 2 }) return

        //now update the team database in firebase
        mDatabase.child("customTeams").addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listOf(winningTeam, losingTeam).forEachIndexed { i, team ->

                    val teamHash = team.map { it.id }.getTeamHash()
                    // check if the team exists
                    if (dataSnapshot.hasChild(teamHash)) {
                        // update values
                        val customTeam = dataSnapshot.child(teamHash).getValue(CustomTeam::class.java) ?: return
                        if (i == 0) customTeam.teamWins++ else customTeam.teamLosses++
                        updateTeamStats(teamHash, customTeam.teamWins, customTeam.teamLosses)

                    } else {
                        // add the team
                        val newTeam = CustomTeam(id = teamHash, users = team.map(User::id))
                        if (i == 0) newTeam.teamWins++ else newTeam.teamLosses++
                        addTeam(newTeam)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) { }
        })

    }

    private fun addTeam(team: CustomTeam) {
        mDatabase.child("customTeams").child(team.getTeamHash()).setValue(team)
    }

    private fun updateTeamStats(teamId: String, teamWins: Long, teamLosses: Long) {
        mDatabase.child("customTeams").child(teamId).apply {
            child("teamWins").setValue(teamWins)
            child("teamLosses").setValue(teamLosses)
        }
    }

    private fun createPlayerStatsMap(player: User): Map<String, Any> {
        // Do not update guest users
        if (player.guest) return HashMap()

        fun key(name: String) = "users/${player.id}/$name"
        return HashMap<String, Any>().apply {
            this[key("wins")] = player.wins
            this[key("losses")] = player.losses
            this[key("foosRanking")] = player.foosRanking
            this[key("rankedGamesPlayed")] = player.rankedGamesPlayed
            this[key("foosRankMap")] = player.foosRankMap
        }
    }

    private fun updateGameStatusBusy() {
        mDatabase.child("tables")
                .child(mTable.id)
                .child("currentGame").setValue("BUSY")
    }

    private fun updateGameStatusFree() {
        mDatabase.child("tables").child(mTable.id).apply {
            child("currentGame").setValue("FREE")
            child("currentScoreTeamOne").setValue("")
            child("currentScoreTeamTwo").setValue("")
            child("currentBestOf").setValue("")
            child("currentPointsToWin").setValue("")
            child("currentRound").setValue("")
            child("teamOne").setValue(null)
            child("teamTwo").setValue(null)
        }
    }

    private fun setGameRoundStats(game: Game) {
        val round = mGame.currentRound()

        // Get scores
        val scoreTeamOne = round.getScore(round.sideOneTeam!!)
        val scoreTeamTwo = round.getScore(round.sideTwoTeam!!)

        mDatabase.child("tables").child(mTable.id).apply {
            child("currentScoreTeamOne").setValue(scoreTeamOne.toString())
            child("currentScoreTeamTwo").setValue(scoreTeamTwo.toString())
            child("currentBestOf").setValue(game.bestOf.toString())
            child("currentPointsToWin").setValue(round.pointsToWin.toString())
            child("currentRound").setValue(game.rounds.size.toString())
            child("teamOne").setValue(game.teamOne)
            child("teamTwo").setValue(game.teamTwo)
        }
    }

    override fun onBackPressed() {
        // Do nothing
    }

    companion object {
        const val EXTRA_GAME_ID = "gameId"
    }

}
