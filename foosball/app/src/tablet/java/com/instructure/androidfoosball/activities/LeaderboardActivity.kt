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

import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.adapters.FoosRankLeaderboardAdapter
import com.instructure.androidfoosball.adapters.LeaderboardAdapter
import com.instructure.androidfoosball.adapters.TeamLeaderboardAdapter
import com.instructure.androidfoosball.adapters.TimeWasterLeaderboardAdapter
import com.instructure.androidfoosball.ktmodels.CustomTeam
import com.instructure.androidfoosball.ktmodels.User
import com.instructure.androidfoosball.utils.*
import kotlinx.android.synthetic.tablet.activity_leaderboard.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import org.jetbrains.anko.sdk21.listeners.onClick


class LeaderboardActivity : AppCompatActivity() {

    companion object {
        const val MIN_GAMES_FOR_RANKING = 9
        const val MIN_GAMES_FOR_TEAM_RANKING = 5
    }

    private val accentColor by lazy { this.resources.getColor(R.color.colorAccent) }
    private val grayColor by lazy { this.resources.getColor(R.color.lightGray) }

    private var loadingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        leaderboardSubtitle.text = getString(R.string.leaderboard_subtitle).format(MIN_GAMES_FOR_RANKING)

        teamLeaderboardSubtitle.text = getString(R.string.leaderboard_subtitle).format(LeaderboardActivity.MIN_GAMES_FOR_TEAM_RANKING)

        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        recyclerView.itemAnimator = DefaultItemAnimator()

        loadIndividualLeaderboard()

        setupListeners()
    }

    private fun loadIndividualLeaderboard() {
        loadingJob?.cancel()
        loadingJob = launch(UI) {
            progressBar.setVisible(true)
            unselectAll()
            selectIndividual()
            val users = FirebaseDatabase.getInstance().reference.awaitList<User>("users")
            val sortedUsers = awaitAsync { users.sortByWinRatio(MIN_GAMES_FOR_RANKING) }
            recyclerView.adapter = LeaderboardAdapter(this@LeaderboardActivity, sortedUsers)
            progressBar.setVisible(false)
        }
    }

    private fun selectIndividual() {
        leaderboardSelected.setBackgroundColor(accentColor)
        leaderboardText.typeface = Typeface.create(leaderboardText.typeface, Typeface.DEFAULT_BOLD.style)
        leaderboardSubtitle.typeface = Typeface.create(leaderboardSubtitle.typeface, Typeface.DEFAULT_BOLD.style)

    }

    private fun unselectIndividual() {
        leaderboardSelected.setBackgroundColor(grayColor)
        leaderboardText.typeface = Typeface.create(leaderboardText.typeface, Typeface.DEFAULT.style)
        leaderboardSubtitle.typeface = Typeface.create(leaderboardSubtitle.typeface, Typeface.DEFAULT.style)
    }

    private fun selectTeam() {
        teamLeaderboardSelected.setBackgroundColor(accentColor)
        teamLeaderboardText.typeface = Typeface.create(teamLeaderboardText.typeface, Typeface.DEFAULT_BOLD.style)
        teamLeaderboardSubtitle.typeface = Typeface.create(teamLeaderboardSubtitle.typeface, Typeface.DEFAULT_BOLD.style)
    }

    private fun unselectTeam() {
        teamLeaderboardSelected.setBackgroundColor(grayColor)
        teamLeaderboardText.typeface = Typeface.create(teamLeaderboardText.typeface, Typeface.DEFAULT.style)
        teamLeaderboardSubtitle.typeface = Typeface.create(teamLeaderboardSubtitle.typeface, Typeface.DEFAULT.style)
    }

    private fun selectFoosRank() {
        foosRankSelected.setBackgroundColor(accentColor)
        foosRankText.typeface = Typeface.create(teamLeaderboardText.typeface, Typeface.DEFAULT_BOLD.style)
        foosRankSubtitle.typeface = Typeface.create(teamLeaderboardSubtitle.typeface, Typeface.DEFAULT_BOLD.style)
    }

    private fun unselectFoosRank() {
        foosRankSelected.setBackgroundColor(grayColor)
        foosRankText.typeface = Typeface.create(teamLeaderboardText.typeface, Typeface.DEFAULT.style)
        foosRankSubtitle.typeface = Typeface.create(teamLeaderboardSubtitle.typeface, Typeface.DEFAULT.style)
    }

    private fun selectTimeWaster() {
        timeWasterSelected.setBackgroundColor(accentColor)
        timeWasterText.typeface = Typeface.create(teamLeaderboardText.typeface, Typeface.DEFAULT_BOLD.style)
        timeWasterSubtitle.typeface = Typeface.create(teamLeaderboardSubtitle.typeface, Typeface.DEFAULT_BOLD.style)
    }

    private fun unselectTimeWaster() {
        timeWasterSelected.setBackgroundColor(grayColor)
        timeWasterText.typeface = Typeface.create(teamLeaderboardText.typeface, Typeface.DEFAULT.style)
        timeWasterSubtitle.typeface = Typeface.create(teamLeaderboardSubtitle.typeface, Typeface.DEFAULT.style)
    }

    private fun unselectAll() {
        unselectFoosRank()
        unselectIndividual()
        unselectTeam()
        unselectTimeWaster()
    }

    private fun loadTeamLeaderboard() {
        loadingJob?.cancel()
        loadingJob = launch(UI) {
            progressBar.setVisible(true)
            unselectAll()
            selectTeam()
            val teams = FirebaseDatabase.getInstance().reference.awaitList<CustomTeam>("customTeams")
            val users = FirebaseDatabase.getInstance().reference.awaitList<User>("users").associateBy { it.id }
            val sortedTeam = awaitAsync { teams.sortCustomTeamByWinRatio(MIN_GAMES_FOR_TEAM_RANKING) }
            recyclerView.adapter = TeamLeaderboardAdapter(this@LeaderboardActivity, sortedTeam, users)
            progressBar.setVisible(false)
        }
    }

    private fun loadFoosRankLeaderboard() {
        loadingJob?.cancel()
        loadingJob = launch(UI) {
            progressBar.setVisible(true)
            unselectAll()
            selectFoosRank()
            val users = FirebaseDatabase.getInstance().reference.awaitList<User>("users")
            val sortedUsers = awaitAsync { users.filter { !it.guest }.sortByFoosRanking() }
            recyclerView.adapter = FoosRankLeaderboardAdapter(this@LeaderboardActivity, sortedUsers) {
                startActivity(EloDialogActivity.createIntent(this@LeaderboardActivity, it.foosRankMap))
            }
            progressBar.setVisible(false)
        }
    }

    private fun loadTimeWasterLeaderboard() {
        loadingJob?.cancel()
        loadingJob = launch(UI) {
            progressBar.setVisible(true)
            unselectAll()
            selectTimeWaster()
            val users = FirebaseDatabase.getInstance().reference.awaitList<User>("users")
            val sortedUsers = awaitAsync { users.filter { !it.guest }.sortedByDescending { it.wins + it.losses } }
            recyclerView.adapter = TimeWasterLeaderboardAdapter(this@LeaderboardActivity, sortedUsers)
            progressBar.setVisible(false)
        }
    }

    private fun setupListeners() {
        leaderboardWrapper.setOnClickListener { loadIndividualLeaderboard() }
        teamLeaderboardWrapper.setOnClickListener { loadTeamLeaderboard() }
        foosRankWrapper.onClick { loadFoosRankLeaderboard() }
        timeWasterWrapper.onClick { loadTimeWasterLeaderboard() }
        hiddenButton.onDoubleTap { timeWasterWrapper.setVisible().performClick() }
    }

    override fun onDestroy() {
        loadingJob?.cancel()
        super.onDestroy()
    }
}

private suspend inline fun <reified T: Any> DatabaseReference.awaitList(s: String): List<T> {
    return suspendCancellableCoroutine { continuation ->

        var loaded = false

        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (loaded) return
                loaded = true
                launch(continuation.context) {
                    val list = async(CommonPool) {
                        dataSnapshot.children.mapNotNull { it.getValue(T::class.java) }
                    }.await()
                    continuation.resume(list)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }

        continuation.invokeOnCompletion({ removeEventListener(listener) }, true)
        this.child(s).addListenerForSingleValueEvent(listener)
    }
}

private suspend fun <T> awaitAsync(block: suspend CoroutineScope.() -> T): T
        = async(context = CommonPool, block = block).await()
