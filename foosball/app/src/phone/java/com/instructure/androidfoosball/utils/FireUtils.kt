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

package com.instructure.androidfoosball.utils

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.instructure.androidfoosball.models.Table


object FireUtils {

    fun getTables(database: DatabaseReference, onFinish: (tables: List<Table>) -> Unit) {
        database.keepSynced(false)
        database.child("tables").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                onFinish(dataSnapshot.children.map { it.getValue(Table::class.java)!!.apply { id = it.key } })
            }

            override fun onCancelled(databaseError: DatabaseError) {
                onFinish(emptyList())
            }
        })
    }

    fun setStartupPhrase(id: String, database: DatabaseReference, phrase: String) {
        database.child("users").child(id).child("customAssignmentPhrase").setValue(phrase)
    }

    fun setVictoryPhrase(id: String, database: DatabaseReference, phrase: String) {
        database.child("users").child(id).child("customVictoryPhrase").setValue(phrase)
    }

    fun getWinCount(id: String, database: DatabaseReference, callback: (value: Int) -> Unit) {
        database.child("users").child(id).child("wins").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                callback(dataSnapshot.getValue(Int::class.java) ?: 0)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(0)
            }
        })
    }

    fun getLossCount(id: String, database: DatabaseReference, callback: (value: Int) -> Unit) {
        database.child("users").child(id).child("losses").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                callback(dataSnapshot.getValue(Int::class.java) ?: 0)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(0)
            }
        })
    }

    fun getStartupPhrase(id: String, database: DatabaseReference, callback: (value: String) -> Unit) {
        database.child("users").child(id).child("customAssignmentPhrase").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                callback(dataSnapshot.getValue(String::class.java) ?: "")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback("")
            }
        })
    }

    fun getVictoryPhrase(id: String, database: DatabaseReference, callback: (value: String) -> Unit) {
        database.child("users").child(id).child("customVictoryPhrase").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                callback(dataSnapshot.getValue(String::class.java) ?: "")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback("")
            }
        })
    }
}
