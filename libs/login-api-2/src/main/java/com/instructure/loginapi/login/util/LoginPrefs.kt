package com.instructure.loginapi.login.util

import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.utils.GsonPref
import com.instructure.canvasapi2.utils.PrefManager

data class SavedLoginInfo(val accountDomain: AccountDomain, val canvasLogin: Int)

object LoginPrefs : PrefManager("loginPrefs") {
    var lastSavedLogin by GsonPref(SavedLoginInfo::class.java, null)
}