package com.instructure.canvasapi2.utils

const val REMOTE_CONFIG_PREFS_FILE = "remote-config-prefs"

// A separate prefs repo for remote config
object RemoteConfigPrefs : PrefManager(REMOTE_CONFIG_PREFS_FILE) {

}