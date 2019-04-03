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

import android.annotation.SuppressLint
import android.graphics.Color
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.models.Table
import com.instructure.androidfoosball.utils.Prefs
import kotlinx.android.synthetic.phone.activity_nfc_read.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.vibrator


class NfcReadActivity : AppCompatActivity() {

    private val TAG = "FoosNFC"

    private val mAuth = FirebaseAuth.getInstance()
    internal lateinit var mUser: FirebaseUser
    private val mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user != null) {
            mUser = user
            parseNfc()
        } else {
            startActivity<SignInActivity>()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vibrator.vibrate(200) // Provide haptic feedback as soon as possible
        setContentView(R.layout.activity_nfc_read)
    }

    private fun parseNfc() {
        try {
            val rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            val foosRecord = (rawMsgs[0] as NdefMessage).records[0]

            val uri = foosRecord.toUri()
            val segments = uri.pathSegments

            Log.i(TAG, "Read NFC Tag for table " + segments[0] + ", side " + segments[1])

            assignTeam(segments[0], segments[1].toIntOrNull() ?: 0)
        } catch (e: Throwable) {
            try {
                val segments = intent.data.pathSegments
                Log.i(TAG, "Read URI for table " + segments[0] + ", side " + segments[1])
                assignTeam(segments[0], segments[1].toIntOrNull() ?: 0)
            } catch (e: Throwable) {
                fail(e.message ?: "Unknown error parsing NFC data")
            }
        }
    }

    private fun assignTeam(tableId: String, side: Int) {

        val db = FirebaseDatabase.getInstance().reference
        db.child("tables").child(tableId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val table = dataSnapshot.getValue(Table::class.java)!!

                val colorString: String
                val teamColor: Int

                colorString = if (side == 0) table.sideOneColor else table.sideTwoColor
                teamColor = Color.parseColor(colorString)
                val teamName = if (side == 0) table.sideOneName else table.sideTwoName
                val userId = Prefs(this@NfcReadActivity).userId
                Log.v(TAG, "Assigning Team " + side)
                db.child("incoming").child(tableId).child(if (side == 0) "sideOne" else "sideTwo").setValue(userId)

                success(table.name, teamName, teamColor)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                fail(databaseError.message)
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun success(tableName: String, teamName: String, teamColor: Int) {
        successView.setCardBackgroundColor(teamColor)
        tableLabel.text = tableName
        teamLabel.text = teamName
        loadingView.visibility = View.GONE
        successView.visibility = View.VISIBLE
        errorView.visibility = View.GONE
        finishAfterDelay()
    }


    private fun fail(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        loadingView.visibility = View.GONE
        successView.visibility = View.GONE
        errorView.visibility = View.VISIBLE
        finishAfterDelay()
    }

    private fun finishAfterDelay() {
        Handler().postDelayed({ finish() }, 3000)
    }

    override fun onBackPressed() {
        // Do nothing. TRAP THE USER!!!!
    }

    override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener(mAuthListener)
    }

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthListener)
    }

}
