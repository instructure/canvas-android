/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.instructure.androidfoosball.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.instructure.androidfoosball.BuildConfig
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.User
import com.instructure.androidfoosball.utils.*
import com.instructure.androidfoosball.views.ConfirmPinDialog
import kotlinx.android.synthetic.main.activity_edit_player.*
import org.jetbrains.anko.sdk21.listeners.onCheckedChange
import org.jetbrains.anko.sdk21.listeners.onClick

class EditUserActivity : AppCompatActivity() {

    private val REQUEST_CODE_AVATAR = 2212

    private val mUserId: String by lazy { intent.getStringExtra(Const.USER_ID) }
    lateinit private var mUser: User
    private val mDatabase = FirebaseDatabase.getInstance().reference
    private val mCommentator = Commentator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mCommentator.initialize(this)
        getUser()
    }

    private fun getUser() {
        mDatabase.child("users").child(mUserId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.getValue(User::class.java)?.let { user ->
                    user.id = dataSnapshot.key
                    mUser = user
                    setup()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) { }
        })
    }

    private fun setup() {
        setContentView(R.layout.activity_edit_player)

        enablePinView.onCheckedChange { button, isChecked ->
            mUser.pinDisabled = if (isChecked) "false" else "true"
            btnChangePin.isEnabled = isChecked
        }

        enablePinView.isChecked = mUser.pinDisabled != "true"
        avatarView.setAvatar(mUser, resources.getDimension(R.dimen.avatar_size_large).toInt())
        displayNameView.text = mUser.name
        assignmentTextView.text = mUser.customAssignmentPhrase
        victoryTextView.text = mUser.customVictoryPhrase

        addAvatarView.onClick {
            showImageSourcePicker(this, null) { startActivityForResult(it, REQUEST_CODE_AVATAR) }
        }

        btnChangePin.onClick {
            ConfirmPinDialog(this, mUser, true) {}.overrideOnPinHashCreated { mUser.pinHash = it }.show()
        }

        btnSave.onClick { save() }

        if (BuildConfig.APPLICATION_ID.endsWith("tablet")) {
            announceNameView.onClick { mCommentator.announce(displayNameView.text) }
            announceAssignmentView.onClick { mCommentator.announce(assignmentTextView.text) }
            announceVictoryView.onClick { mCommentator.announce(victoryTextView.text) }
        } else {
            announceNameView.visibility = View.GONE
            announceAssignmentView.visibility = View.GONE
            announceVictoryView.visibility = View.GONE
        }

    }

    fun save() {
        ValidatorChain()
                .first(displayNameView.validate("User name must be min 3 letters") { it.length > 3 })
                .then(mUserId.validate("Invalid user ID", { !it.isNullOrBlank() }, { shortToast(it) }))
                .finally {
                    mUser.name = displayNameView.text
                    mUser.customVictoryPhrase = victoryTextView.text
                    mUser.customAssignmentPhrase = assignmentTextView.text
                    mDatabase.child("users").child(mUserId).setValue(mUser)
                    finish()
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_AVATAR && resultCode == Activity.RESULT_OK) {
            mUser.avatar = data?.getStringExtra(ChangeAvatarActivity.EXTRA_AVATAR_URL) ?: ""
            avatarView.setAvatar(mUser)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        mCommentator.shutUp()
    }
}
