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

package com.instructure.androidfoosball.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.activities.ChangeAvatarActivity
import com.instructure.androidfoosball.activities.Mode
import com.instructure.androidfoosball.interfaces.FragmentCallbacks
import com.instructure.androidfoosball.interfaces.TextEditCallback
import com.instructure.androidfoosball.models.User
import com.instructure.androidfoosball.utils.AnimUtils
import com.instructure.androidfoosball.utils.FireUtils
import kotlinx.android.synthetic.phone.fragment_user.*
import com.squareup.picasso.Picasso


class UserFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private var mCallbacks: FragmentCallbacks? = null
    private var mTextEditCallbacks: TextEditCallback? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mCallbacks = context as FragmentCallbacks?
        mTextEditCallbacks = context as TextEditCallback?
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_user, container, false)
        return rootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        swipeRefreshLayout.setOnRefreshListener(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loadData()
    }

    private fun loadData() {
        winProgress.visibility = View.VISIBLE
        lossProgress.visibility = View.VISIBLE

        val user = mCallbacks?.mUser
        if (user != null) {
            userEmail.text = user.email
            setupAvatar(user)
            setupWinLossCount(user)
            setupPhrase(user)
        }
    }

    private fun setupAvatar(user: User) {
        val ref = mCallbacks!!.mDatabase!!.child("users").child(user.id).child("avatar")
        ref.keepSynced(false)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val url = dataSnapshot.getValue(String::class.java)
                Picasso.with(context).load(url).error(R.drawable.sadpanda).into(avatar)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Picasso.with(context).load(R.drawable.sadpanda).into(avatar)
            }
        })
        if (!TextUtils.isEmpty(user.avatar)) {
            Picasso.with(context).load(user.avatar).placeholder(R.drawable.sadpanda).error(R.drawable.sadpanda).into(avatar)
        } else {
            Picasso.with(context).load(R.drawable.sadpanda).into(avatar)
        }

        avatar.setOnClickListener {
            mCallbacks?.mUser?.let { u ->
                startActivityForResult(Intent(ChangeAvatarActivity.createIntent(activity, u.id, Mode.CAMERA)), REQUEST_CODE_TAKE_PICTURE)
            }
        }
    }

    private fun setupWinLossCount(user: User) {
        FireUtils.getWinCount(user.id, mCallbacks!!.mDatabase!!) { value ->
            win.text = value.toString()
            winProgress!!.visibility = View.INVISIBLE
        }

        FireUtils.getLossCount(user.id, mCallbacks!!.mDatabase!!) { value ->
            loss.text = value.toString()
            lossProgress!!.visibility = View.INVISIBLE
        }
    }

    private fun setupPhrase(user: User) {
        FireUtils.getVictoryPhrase(user.id, mCallbacks!!.mDatabase!!) { value ->
            victoryPhrase.text = value
            swipeRefreshLayout.isRefreshing = false
            AnimUtils.fadeIn(320, victoryPhraseCard)
        }

        FireUtils.getStartupPhrase(user.id, mCallbacks!!.mDatabase!!) { value ->
            startupPhrase!!.text = value
            AnimUtils.fadeIn(320, startupPhraseCard)
        }

        victoryEdit.setOnClickListener { mTextEditCallbacks!!.requestTextEdit(victoryPhrase.id, victoryPhrase.text.toString()) }
        startupEdit.setOnClickListener { mTextEditCallbacks!!.requestTextEdit(startupPhrase.id, startupPhrase.text.toString()) }
    }

    fun updateStartupPhraseText(text: String) {
        startupPhrase.text = text
    }

    fun updateVictoryPhraseText(text: String) {
        victoryPhrase.text = text
    }

    override fun onRefresh() {
        swipeRefreshLayout.isRefreshing = true
        loadData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_TAKE_PICTURE && resultCode == Activity.RESULT_OK && data != null) {
            val avatarUrl = data.getStringExtra(ChangeAvatarActivity.EXTRA_AVATAR_URL)
            if (!TextUtils.isEmpty(avatarUrl)) {
                Picasso.with(context).load(avatarUrl).placeholder(R.drawable.sadpanda).error(R.drawable.sadpanda).into(avatar)
            }
        }
    }

    companion object {

        fun newInstance(): UserFragment {
            return UserFragment()
        }

        private val REQUEST_CODE_TAKE_PICTURE = 1337
    }
}
