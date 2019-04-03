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
@file:Suppress("unused")

package com.instructure.androidfoosball.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.afollestad.materialdialogs.MaterialDialog
import com.instructure.androidfoosball.App
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.adapters.TeamAdapter
import com.instructure.androidfoosball.adapters.UserAdapter
import com.instructure.androidfoosball.ktmodels.CustomTeam
import com.instructure.androidfoosball.ktmodels.RealmTeam
import com.instructure.androidfoosball.ktmodels.User
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import kotlinx.android.synthetic.main.dialog_team_picker.view.*
import kotlinx.android.synthetic.main.dialog_user_picker.view.*
import org.jetbrains.anko.displayMetrics
import org.jetbrains.anko.sdk21.listeners.onClick
import org.jetbrains.anko.sdk21.listeners.onItemClick
import java.util.*
import kotlinx.android.synthetic.main.dialog_user_picker.view.clearButton as teamClearButton

inline fun <T : RealmObject> T.edit(block: T.(realm: Realm) -> Unit) = App.realm.inTransaction { block(this) }

inline fun <T : RealmList<*>> T.edit(block: T.(realm: Realm) -> Unit) = App.realm.inTransaction { block(this) }

inline fun Realm.inTransaction(block: Realm.(realm: Realm) -> Unit) {
    beginTransaction()
    block(this)
    commitTransaction()
}

fun <T : RealmObject> T.copyToRealmOrUpdate() = edit { it.copyToRealmOrUpdate(this) }

val Activity.mCommentator: Commentator get() = App.commentator

fun Float.dp() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, App.context.displayMetrics)
fun Float.dp(context: Context) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, context.displayMetrics)

fun <T> List<T>.shift(offset: Int): List<T> = if (offset == 0) this else subList(offset, size) + subList(0, offset)

fun <T> List<T>.split(vararg splitSizes: Int): List<List<T>> {
    if (splitSizes.sum() > size) throw IndexOutOfBoundsException("Sum of requested split sizes is larger than source list size")
    val list = ArrayList<List<T>>()
    var idx = 0
    splitSizes.forEach {
        list.add(subList(idx, idx + it))
        idx += it
    }
    list.add(subList(idx, size))
    return list
}


fun CustomTeam.getWinRate(minGamesRequired: Int) = when {
    teamWins + teamLosses < minGamesRequired -> -1f
    teamWins == 0L -> 0f
    teamLosses == 0L -> 100f
    else -> 100f * teamWins / (teamWins + teamLosses)
}

fun List<String>.getTeamHash(): String = sorted().joinToString("")

fun CustomTeam.getTeamHash() : String = users.getTeamHash()

fun List<CustomTeam>.sortCustomTeamByWinRatio(minGamesRequired: Int)
        = sortedWith(compareBy({ -it.getWinRate(minGamesRequired) }, { -it.teamWins }, { it.teamLosses } ))

@SuppressLint("InflateParams")
fun showUserPicker(context: Context, onUserSelected: (User) -> Unit) {
    val users: List<User> = App.realm.where(User::class.java).findAllSorted("name").toList()
    var dialog: MaterialDialog? = null
    val adapter = UserAdapter(context, users)

    val view = LayoutInflater.from(context).inflate(R.layout.dialog_user_picker, null)
    view.userSearchInput.addTextChangedListener(object: TextWatcher {
        override fun afterTextChanged(p0: Editable?) {}

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(text: CharSequence, p1: Int, p2: Int, p3: Int) {
            adapter.searchQuery = text.toString()
            view.clearButton.visibility = if (text.isBlank()) View.INVISIBLE else View.VISIBLE
        }

    })
    view.userListView.adapter = adapter
    view.userListView.onItemClick { _, _, position, _ ->
        dialog?.dismiss()
        onUserSelected(adapter.getItem(position))
    }
    view.clearButton.onClick { view.userSearchInput.setText("") }

    dialog = MaterialDialog.Builder(context).title(R.string.pick_a_user).customView(view, false).show()
    dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    dialog?.setOnDismissListener {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.userSearchInput.windowToken, 0)
    }
}

@SuppressLint("InflateParams")
fun showTeamPicker(context: Context, onUserSelected: (User) -> Unit) {
    val userMap = App.realm.where(User::class.java).findAllSorted("name").toList().associateBy(User::id)
    val teams = App.realm.where(RealmTeam::class.java).isNotEmpty("teamName").findAllSorted("teamName").map { it.toCustomTeam() }
    var dialog: MaterialDialog? = null
    val adapter = TeamAdapter(context, teams.filter { it.users.size == 2 }, userMap)

    val view = LayoutInflater.from(context).inflate(R.layout.dialog_team_picker, null)
    view.teamSearchInput.addTextChangedListener(object: TextWatcher {
        override fun afterTextChanged(p0: Editable?) {}

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(text: CharSequence, p1: Int, p2: Int, p3: Int) {
            adapter.searchQuery = text.toString()
            view.teamClearButton.visibility = if (text.isBlank()) View.INVISIBLE else View.VISIBLE
        }

    })
    view.teamListView.adapter = adapter
    view.teamListView.onItemClick { _, _, position, _ ->
        dialog?.dismiss()
        val selectedTeam = adapter.getItem(position)
        selectedTeam.users.map { userMap[it]!! }.forEach { onUserSelected(it) }
    }
    view.teamClearButton.onClick { view.teamSearchInput.setText("") }

    dialog = MaterialDialog.Builder(context).title(R.string.pick_a_team).customView(view, false).show()
    dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    dialog?.setOnDismissListener {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.teamSearchInput.windowToken, 0)
    }
}

fun <A> Pair<A, A>.swappedIf(condition: Boolean): Pair<A, A> = if (condition) second to first else this
