package com.instructure.canvasapi2.utils

// Common interface for shared preference repositories/managers.
// This made testing a little easier.
interface PrefRepoInterface {
    fun getInt(key: String, default: Int = 0) : Int
    fun putInt(key: String, value: Int)

    fun getBoolean(key: String, default: Boolean = false) : Boolean
    fun putBoolean(key: String, value: Boolean)

    fun getString(key: String, default: String? = null) : String?
    fun putString(key: String, value: String)

    fun getFloat(key: String, default: Float = 0f) : Float
    fun putFloat(key: String, value: Float)

    fun getLong(key: String, default: Long = 0L) : Long
    fun putLong(key: String, value: Long)
}