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
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.TableKingGame
import kotlinx.android.synthetic.tablet.dialog_win_table_king.*
import org.jetbrains.anko.sdk21.listeners.onClick

class WinTableKingGameDialog(
        context: Context,
        private val game: TableKingGame,
        private val onUndoGoal: () -> Unit,
        private val onEndGame: () -> Unit
) : Dialog(context, R.style.AppTheme) {

    private val COUNTDOWN_SECONDS = 30

    private val playerViews by lazy { listOf(playerLayout1, playerLayout2, playerLayout3, playerLayout4) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setContentView(R.layout.dialog_win_table_king)
        setupViews()
        setupListeners()
        animateTrophy()
    }

    private fun setupViews() {
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
}
