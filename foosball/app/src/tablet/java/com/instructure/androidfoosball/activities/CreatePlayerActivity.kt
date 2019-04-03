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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.instructure.androidfoosball.App
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.User
import com.instructure.androidfoosball.utils.*
import io.realm.Case
import kotlinx.android.synthetic.tablet.activity_create_player.*
import java.util.*


class CreatePlayerActivity : AppCompatActivity() {

    private val REQUEST_CODE_ADD_AVATAR = 123

    private val mDatabase by lazy {
        FirebaseDatabase.getInstance().reference
    }

    private var mAvatarUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_player)

        /* Show Email input on Name input validation */
        tilDisplayName.validateOnTextChanged({ it.length >= 3 }, "Display name must be at least 3 characters") { newText, isValid ->
            tilEmail.setVisible(isValid)
        }

        /* Show Confirm Email input when a valid email is entered */
        tilEmail.validateOnTextChanged({it.matches(Patterns.EMAIL_ADDRESS)}, "Enter a valid email address") { newText, isValid ->
            tilConfirmEmail.setVisible(isValid)
        }

        tilConfirmEmail.validateOnTextChanged({ it == tilEmail.text }, "Emails do not match") { newText, isValid ->
            controlsContainer.setVisible(isValid)
        }

        btnCreateUser.setOnClickListener {
            val user = User(id = UUID.randomUUID().toString(), name = tilDisplayName.text, email = tilConfirmEmail.text, avatar = mAvatarUrl)
            if (App.realm.where(User::class.java).equalTo("email", user.email, Case.INSENSITIVE).count() > 0){
                Toast.makeText(this@CreatePlayerActivity, "User with email ${user.email} already exists", Toast.LENGTH_SHORT).show()
            } else {
                postUser(user)
            }
        }

        tvAddAvatar.setOnClickListener {
            showImageSourcePicker(this) {
                startActivityForResult(it, REQUEST_CODE_ADD_AVATAR)
            }
        }
    }

    private fun postUser(user: User) {
        if (user.id.isNullOrBlank()) {
            shortToast("Error creating user - invalid user ID")
            return
        }
        mDatabase.child("users").child(user.id).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this@CreatePlayerActivity, "${user.name} successfully added", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@CreatePlayerActivity, "Error adding user: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_ADD_AVATAR && resultCode == Activity.RESULT_OK) {
            mAvatarUrl = data?.getStringExtra(ChangeAvatarActivity.EXTRA_AVATAR_URL) ?: ""
            ivAvatar.setAvatarUrl(mAvatarUrl)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
