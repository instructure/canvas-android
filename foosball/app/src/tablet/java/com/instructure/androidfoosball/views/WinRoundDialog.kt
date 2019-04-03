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

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.Game
import com.instructure.androidfoosball.ktmodels.Table
import com.instructure.androidfoosball.utils.bind
import org.jetbrains.anko.sdk21.listeners.onClick

class WinRoundDialog(context: Context, val game: Game, val onNextRound: () -> Unit, val onUndoGoal: () -> Unit) : Dialog(context) {

    private val COUNTDOWN_SECONDS = 8

    private val countdownView: CountdownCircle by bind(R.id.countdownView)
    private val countdownSeconds: TextView by bind(R.id.countdownSeconds)
    private val teamLayout: TeamLayout by bind(R.id.teamLayout)
    private val victoryStar: ImageView by bind(R.id.victoryStar)
    private val undoView: View by bind(R.id.undoView)
    private val teamNameView: TextView by bind(R.id.winningTeamNameView)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setContentView(R.layout.dialog_win_round)
        setupViews()
        setupListeners()
        animateStar()
    }

    private fun setupViews() {
        game.currentRound().getWinningTeam()?.let { team ->
            teamLayout.team = team
            teamNameView.text = context.getString(R.string.team_win_round).format(game.currentRound().getTeamName(team, Table.getSelectedTable()))
        }
    }

    private fun animateStar() {
        // Set initial scale
        victoryStar.scaleX = 0f
        victoryStar.scaleY = 0f

        // Start scale animation
        AnimatorSet().apply {
            playTogether(
                    ObjectAnimator.ofFloat(victoryStar, "scaleX", 0f, 1f),
                    ObjectAnimator.ofFloat(victoryStar, "scaleY", 0f, 1f)
            )
            duration = 600
            startDelay = 200
            interpolator = OvershootInterpolator()
        }.start()

        // Start ongoing rotation animation
        ObjectAnimator.ofFloat(victoryStar, "rotation", 0f, 360f).apply {
            repeatCount = ObjectAnimator.INFINITE
            duration = 10000
        }.start()
    }

    private fun setupListeners() {
        // Countdown timer
        countdownView.startCountdown(COUNTDOWN_SECONDS) { seconds ->
            if (seconds > 0) {
                countdownSeconds.text = seconds.toString()
            } else {
                onNextRound()
                dismiss()
            }
        }

        // Undo goal
        undoView.onClick {
            onUndoGoal()
            dismiss()
        }

        // Next round (Swap now)
        countdownView.onClick {
            onNextRound()
            dismiss()
        }
    }
}
