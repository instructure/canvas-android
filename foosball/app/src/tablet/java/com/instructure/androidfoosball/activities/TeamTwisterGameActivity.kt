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

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.media.AudioManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.instructure.androidfoosball.App
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.GameStatus
import com.instructure.androidfoosball.ktmodels.Table
import com.instructure.androidfoosball.ktmodels.TableSide
import com.instructure.androidfoosball.ktmodels.TeamTwisterGame
import com.instructure.androidfoosball.push.PushIntentService
import com.instructure.androidfoosball.receivers.GoalReceiver
import com.instructure.androidfoosball.utils.*
import com.instructure.androidfoosball.utils.Commentator.Sfx
import com.instructure.androidfoosball.views.WinTeamTwisterGameDialog
import kotlinx.android.synthetic.tablet.activity_team_twister_game.*
import org.jetbrains.anko.sdk21.listeners.onClick
import org.jetbrains.anko.textColor
import java.util.*

class TeamTwisterGameActivity : AppCompatActivity() {

    companion object {
        val EXTRA_GAME_ID = "gameId"
    }

    private val mGameId by lazy { intent.getStringExtra(EXTRA_GAME_ID) ?: "" }
    private val mGame by lazy { App.realm.where(TeamTwisterGame::class.java).equalTo("id", mGameId).findFirst()!! }
    private val mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val mTable = Table.getSelectedTable()
    private val mHistory = Stack<TeamTwisterGame>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_twister_game)
        volumeControlStream = AudioManager.STREAM_MUSIC
        setupViews()
        updateState()
        GoalReceiver.register(this, goalReceiver, 100)
        updateGameStatusBusy()
    }

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

        mCommentator.announceGameStart()
    }

    private fun undoGoal() {
        if (mHistory.isNotEmpty()) {
            val snapshot = mHistory.pop()
            App.realm.inTransaction { it.copyToRealmOrUpdate(snapshot) }
            mCommentator.announce("Oops")
        }
        updateState()
    }

    private val goalReceiver: GoalReceiver = GoalReceiver { side ->
        if (mGame.hasWinner) return@GoalReceiver
        val announcement = "${Sfx.ROTATE_DING} ${mGame.getNextSwappedPlayers().joinToString(" and ") { it.name }} - swap now"
        mHistory += App.realm.copyFromRealm(mGame)
        mGame.recordGoal(side)
        if (!mGame.hasWinner) mCommentator.announce(announcement, true)
        updateState()
    }

    private fun updateState() {
        val (team1, team2) = mGame.currentTeams

        teamOneName.text = team1.teamName.elseIfBlank(mTable.sideOneName)
        teamTwoName.text = team2.teamName.elseIfBlank(mTable.sideTwoName)

        teamOneLayout.team = team1
        teamTwoLayout.team = team2

        pointsToWinView.text = mGame.pointsToWin.toString()

        gameTimerView.setStartTime(mGame.startTime)

        teamOneScore.text = team1.points.toString()
        teamTwoScore.text = team2.points.toString()

        undoView.setVisible(mHistory.isNotEmpty())

        val sideOneServing = mGame.getServingSide().isSide1
        teamOneServingIndicator.setVisible(sideOneServing)
        teamTwoServingIndicator.setVisible(!sideOneServing)

        if (mGame.hasWinner) {
            mCommentator.announce(Sfx.WINNING_GOAL.name)
            WinTeamTwisterGameDialog(this, mGame, { undoGoal() }, { endGame() }).show()
        }
    }

    private fun endGame() {
        mGame.edit {
            status = GameStatus.FINISHED.name
            endTime = System.currentTimeMillis()
        }
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
    }

    private fun updateGameStatusBusy() {
        mDatabase.child("tables")
                .child(mTable.id)
                .child("currentGame").setValue("TEAM_TWISTER")
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

    override fun onBackPressed() {
        // Do nothing
    }
}
