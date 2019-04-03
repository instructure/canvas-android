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
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.CutThroatGame
import com.instructure.androidfoosball.utils.bind
import org.jetbrains.anko.sdk21.listeners.onClick

class WinCutThroatGameDialog(
        context: Context,
        val game: CutThroatGame,
        private val onEndGame: () -> Unit,
        private val onUndoGoal: () -> Unit
) : Dialog(context, R.style.AppTheme) {

    private val COUNTDOWN_SECONDS = 15

    private val playerLayout: TeamLayout by bind(R.id.playerLayout)
    private val victoryTrophy: ImageView by bind(R.id.victoryTrophy)
    private val playerNameView: TextView by bind(R.id.playerNameView)
    private val countdownView: CountdownCircle by bind(R.id.countdownView)
    private val undoView: TextView by bind(R.id.undoView)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setContentView(R.layout.dialog_win_game_cut_throat)
        setupViews()
        setupListeners()
        animateTrophy()
    }

    private fun setupViews() {
        game.getWinner()?.let { user ->
            playerLayout.addUser(user)
            playerNameView.text = context.getString(R.string.team_win_game).format(user.name)
        }
    }

    private fun animateTrophy() {
        // Set initial scale
        victoryTrophy.scaleX = 0f
        victoryTrophy.scaleY = 0f

        // Start scale animation
        AnimatorSet().apply {
            playTogether(
                    ObjectAnimator.ofFloat(victoryTrophy, "scaleX", 0f, 1f),
                    ObjectAnimator.ofFloat(victoryTrophy, "scaleY", 0f, 1f)
            )
            duration = 600
            startDelay = 200
            interpolator = OvershootInterpolator()
        }.start()

        // Start ongoing rotation animation
        ObjectAnimator.ofFloat(victoryTrophy, "rotation", -6f, 6f).apply {
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            duration = 3000
        }.start()
    }

    private fun setupListeners() {

        countdownView.startCountdown(COUNTDOWN_SECONDS) { seconds ->
            if (seconds > 0) {
                //countdownSeconds.text = seconds.toString()
            } else {
                onEndGame()
                dismiss()
            }
        }

        countdownView.onClick {
            onEndGame()
            dismiss()
        }

        undoView.onClick {
            onUndoGoal()
            dismiss()
        }
    }
}
