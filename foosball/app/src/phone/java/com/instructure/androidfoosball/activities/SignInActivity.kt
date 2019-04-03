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

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.models.User
import com.instructure.androidfoosball.utils.Prefs
import kotlinx.android.synthetic.phone.activity_sign_in.*
import org.jetbrains.anko.sdk21.listeners.onClick


class SignInActivity : BaseFireBaseActivity(), GoogleApiClient.OnConnectionFailedListener {

    private var mGoogleApiClient: GoogleApiClient? = null
    private var mProgressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        setupListeners()

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = GoogleApiClient.Builder(this).enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build()

        initFireBase()
    }

    override fun onAuthStateChange(firebaseAuth: FirebaseAuth) {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            // User is signed in
            Log.d(TAG, "onAuthStateChanged:signed_in:" + firebaseUser.uid)
            finish()
            overridePendingTransition(0, 0)

            val user = User(
                    id = Prefs(this).userId,
                    name = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: "",
                    avatar = firebaseUser.photoUrl?.toString() ?: ""
            )

            startActivity(PrimaryActivity.createIntent(this@SignInActivity, user, intent.action))
        } else {
            // User is signed out
            Log.d(TAG, "onAuthStateChanged:signed_out")
        }
    }


    public override fun onStop() {
        super.onStop()
        hideProgressDialog()
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount, user: User) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)
        showProgressDialog()

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth?.signInWithCredential(credential)?.addOnCompleteListener(this) { task ->
            Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful)

            // If sign in fails, display a message to the user. If sign in succeeds
            // the auth state listener will be notified and logic to handle the
            // signed in user can be handled in the listener.
            if (!task.isSuccessful) {
                Log.w(TAG, "signInWithCredential", task.exception)
                Toast.makeText(this@SignInActivity, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
            } else {
                postUser(acct, user)
            }
            hideProgressDialog()
        }
    }

    private fun postUser(acct: GoogleSignInAccount, user: User) {
        mDatabase!!.child("users").orderByChild("email").equalTo(user.email).limitToFirst(1).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var acctId = acct.id ?: ""
                if (dataSnapshot.childrenCount == 0L) {
                    mDatabase!!.child("users").child(acctId).setValue(user)
                } else {
                    acctId = dataSnapshot.children.iterator().next().getValue(User::class.java)!!.id
                    user.id = acctId
                }
                Prefs(this@SignInActivity).userId = acctId
                finish()
                overridePendingTransition(0, 0)
                startActivity(PrimaryActivity.createIntent(this@SignInActivity, user))
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }

        })
    }

    fun showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(this)
            mProgressDialog!!.setMessage(getString(R.string.loading))
            mProgressDialog!!.isIndeterminate = true
        }

        mProgressDialog!!.show()
    }

    fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Toast.makeText(this, "Connection failed: " + connectionResult.errorMessage!!, Toast.LENGTH_LONG).show()
    }


    private fun setupListeners() {
        sign_in_button.onClick { signIn() }
    }

    private fun signIn() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun handleSignInResult(result: GoogleSignInResult, user: User) {
        Log.d("abcde", "handleSignInResult:" + result.isSuccess)
        if (result.isSuccess) {
            // Signed in successfully, show authenticated UI.
            val acct = result.signInAccount
            Toast.makeText(this@SignInActivity, "Signed in " + acct!!.displayName + " " + acct.photoUrl, Toast.LENGTH_SHORT).show()
            firebaseAuthWithGoogle(acct, user)

            //updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            //updateUI(false);
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            //add the user to the database
            val user = User()
            result.signInAccount?.let { acct ->
                user.id = acct.id ?: ""
                user.name = acct.displayName ?: ""
                user.email = acct.email ?: ""
                user.avatar = acct.photoUrl?.toString() ?: ""
            }
            handleSignInResult(result, user)
        }
    }

    companion object {

        private val TAG = "SignInActivity"

        val ACTION_SHORTCUT_TABLES = "com.instructure.androidfoosball.SHORTCUT_TABLES"
        private val RC_SIGN_IN = 9001
    }
}
