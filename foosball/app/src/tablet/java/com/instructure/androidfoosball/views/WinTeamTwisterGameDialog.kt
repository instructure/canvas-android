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
package com.instructure.androidfoosball.views

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.TeamTwisterGame
import kotlinx.android.synthetic.tablet.dialog_win_team_twister.*
import org.jetbrains.anko.sdk21.listeners.onClick

class WinTeamTwisterGameDialog(
        context: Context,
        private val game: TeamTwisterGame,
        private val onUndoGoal: () -> Unit,
        private val onEndGame: () -> Unit
) : Dialog(context, R.style.AppTheme) {

    private val COUNTDOWN_SECONDS = 45

    private val teamLayouts by lazy { listOf(teamLayout1, teamLayout2, teamLayout3, teamLayout4, teamLayout5, teamLayout6) }
    private val scoreViews by lazy { listOf(scoreView1, scoreView2, scoreView3, scoreView4, scoreView5, scoreView6)}
    private val playerViews by lazy { listOf(playerLayout1, playerLayout2, playerLayout3, playerLayout4) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setContentView(R.layout.dialog_win_team_twister)
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        game.teams.sortedByDescending { it.points }.forEachIndexed { i, team ->
            teamLayouts[i].team = team
            scoreViews[i].text = team.points.toString()
        }
        game.teams.flatMap { it.users }.distinct()
                .map { user ->
                    user to game.teams.filter { user in it.users }.sumBy { it.points }
                }
                .sortedByDescending { it.second }
                .forEachIndexed { i, (user, score) ->
                    playerViews[i].setPlayer(user, score)
                }
    }

    private fun setupListeners() {

        countdownView.startCountdown(COUNTDOWN_SECONDS) { seconds ->
            if (seconds == 0) {
                onEndGame()
                dismiss()
            }
        }

        countdownView.onClick {
            countdownView.stopCountdown()
            onEndGame()
            dismiss()
        }

        // Undo goal
        undoView.onClick {
            onUndoGoal()
            dismiss()
        }
    }
}
