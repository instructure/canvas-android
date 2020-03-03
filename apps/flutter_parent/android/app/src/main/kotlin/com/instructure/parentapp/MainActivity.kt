/*
 * Copyright (C) 2020 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 

package com.instructure.parentapp

import android.os.AsyncTask
import android.os.Bundle
import androidx.annotation.NonNull
import com.instructure.parentapp.plugins.OldAppMigrations
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant
import okhttp3.HttpUrl
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.FormElement

class MainActivity : FlutterActivity() {
    private val AUTHCODE_CHANNEL = "GET_AUTH_CODE"

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine)
        OldAppMigrations.init(flutterEngine, applicationContext)

        // Implement getAuthCode / JSoup support via MethodChannel
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, AUTHCODE_CHANNEL).setMethodCallHandler { call, result ->
            print("Got AuthCode method ${call.method}");

            if (call.method.equals("getAuthCode")) {

                object : AsyncTask<MethodCall, Void, Void>() {
                    var authCode : String? = null
                    override fun doInBackground(vararg methodCalls: MethodCall): Void? {
                        val _call = methodCalls[0]

                        val domain = _call.argument<String>("domain")
                        val clientId = _call.argument<String>("clientId")
                        val redirectUrl = _call.argument<String>("redirectUrl")
                        val login = _call.argument<String>("login")
                        val password = _call.argument<String>("password")

                        val loginPageResponse = Jsoup.connect("https://$domain/login/oauth2/auth")
                                .method(Connection.Method.GET)
                                .data("client_id", clientId)
                                .data("response_type", "code")
                                .data("redirect_uri", redirectUrl)
                                .execute()
                        val loginForm = loginPageResponse.parse().select("form").first() as FormElement
                        loginForm.getElementById("pseudonym_session_unique_id").`val`(login)
                        loginForm.getElementById("pseudonym_session_password").`val`(password)
                        val authFormResponse = loginForm.submit().cookies(loginPageResponse.cookies()).execute()
                        val authForm = authFormResponse.parse().select("form").first() as FormElement
                        val responseUrl = authForm.submit().cookies(authFormResponse.cookies()).execute().url().toString()
                        authCode = HttpUrl.parse(responseUrl)?.queryParameter("code")
                                ?: throw RuntimeException("/login/oauth2/auth failed!")

                        return null
                    }

                    override fun onPostExecute(nullResult: Void?) : Unit {
                        result.success(authCode)
                    }
                }.execute(call)
            } else {
                result.notImplemented(); // Unknown call
            }
        }

    }

}
