package com.instructure.loginapi.login.util

import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.utils.BooleanPref
import com.instructure.canvasapi2.utils.GsonPref
import com.instructure.canvasapi2.utils.IntPref
import com.instructure.canvasapi2.utils.LongPref
import com.instructure.canvasapi2.utils.PrefManager
import com.instructure.pandautils.dialogs.RatingDialog

data class SavedLoginInfo(val accountDomain: AccountDomain, val canvasLogin: Int)

object LoginPrefs : PrefManager("loginPrefs") {
    var lastSavedLogin by GsonPref(SavedLoginInfo::class.java, null)
}