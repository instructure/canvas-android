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
package com.instructure.loginapi.login.util

import android.content.Context
import com.google.gson.Gson
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.loginapi.login.model.SignedInUser

object PreviousUsersUtils {

    private const val SIGNED_IN_USERS_PREF_NAME = "signedInUsersList"

    //Does the CURRENT user support Multiple Users.
    operator fun get(context: Context): ArrayList<SignedInUser> {

        val signedInUsers = ArrayList<SignedInUser>()

        val sharedPreferences = context.getSharedPreferences(SIGNED_IN_USERS_PREF_NAME, Context.MODE_PRIVATE)
        val keys = sharedPreferences.all
        for ((_, value) in keys) {
            var signedInUser: SignedInUser? = null

            try {
                signedInUser = Gson().fromJson(value.toString(), SignedInUser::class.java)
            } catch (ignore: Exception) {
                //Do Nothing
            }

            if (signedInUser != null) {
                signedInUsers.add(signedInUser)
            }
        }

        //Sort by last signed in date.
        signedInUsers.sort()
        return signedInUsers
    }

    /**
     * Removes all instances of [SignedInUser] that match non-empty values of [token] or [refreshToken].
     */
    fun removeByToken(context: Context, token: String, refreshToken: String): Boolean {
        val signedInUsers = get(context)
        var removedUser = false
        for (user in signedInUsers) {
            if ((token.isNotBlank() && user.token == token)
                || (refreshToken.isNotBlank() && user.refreshToken == refreshToken)
            ) {
                remove(context, user)
                removedUser = true
            }
        }
        return removedUser
    }

    fun remove(context: Context, signedInUser: SignedInUser): Boolean {

        // Delete Access Token. We don't care about the result.
        OAuthManager.deleteToken()

        // Remove Signed In User from sharedPreferences
        val sharedPreferences = context.getSharedPreferences(SIGNED_IN_USERS_PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(getGlobalUserId(signedInUser.domain, signedInUser.user))
        return editor.commit()
    }

    fun add(
        context: Context,
        signedInUser: SignedInUser,
        domain: String = ApiPrefs.domain,
        user: User? = ApiPrefs.user
    ): Boolean {

        val signedInUserJSON = Gson().toJson(signedInUser)

        //Save Signed In User to sharedPreferences
        val sharedPreferences = context.getSharedPreferences(SIGNED_IN_USERS_PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(getGlobalUserId(domain, user), signedInUserJSON)
        return editor.commit()
    }

    fun getSignedInUser(context: Context, domain: String, userId: Long): SignedInUser? {
        val prefs = context.getSharedPreferences(SIGNED_IN_USERS_PREF_NAME, Context.MODE_PRIVATE)
        val userJson = prefs.getString(getGlobalUserId(domain, userId), null)
        return try {
            Gson().fromJson(userJson, SignedInUser::class.java)
        } catch (e: Exception) {
            null
        }

    }

    private fun getGlobalUserId(domain: String, userId: Long): String {
        return "$domain-$userId"
    }

    private fun getGlobalUserId(domain: String, user: User?): String {
        return if (user == null) "" else getGlobalUserId(domain, user.id)
    }

    fun clear(context: Context) {
        val sharedPreferences = context.getSharedPreferences(SIGNED_IN_USERS_PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}
