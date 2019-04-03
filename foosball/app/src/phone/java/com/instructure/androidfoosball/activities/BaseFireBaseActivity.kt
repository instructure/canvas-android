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

import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.instructure.androidfoosball.interfaces.FragmentCallbacks
import com.instructure.androidfoosball.models.User

abstract class BaseFireBaseActivity : AppCompatActivity(), FragmentCallbacks {

    override var mUser: User? = null
    override var mAuth: FirebaseAuth? = null
    override var mDatabase: DatabaseReference? = null

    protected abstract fun onAuthStateChange(firebaseAuth: FirebaseAuth)

    protected fun initFireBase() {
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
    }

    private val mAuthStateListener = FirebaseAuth.AuthStateListener { firebaseAuth -> onAuthStateChange(firebaseAuth) }

    override fun onStop() {
        super.onStop()
        mAuth?.removeAuthStateListener(mAuthStateListener)
    }

    override fun onStart() {
        super.onStart()
        mAuth?.addAuthStateListener(mAuthStateListener)
    }
}
