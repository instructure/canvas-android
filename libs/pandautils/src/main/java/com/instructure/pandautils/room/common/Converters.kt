package com.instructure.pandautils.room.common

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class Converters {
    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return list.joinToString()
    }

    @TypeConverter
    fun fromStringToListString(s: String): List<String> {
        return s.split(", ")
    }

    @TypeConverter
    fun fromLongList(list: List<Long>) : String {
        return list.joinToString()
    }

    @TypeConverter
    fun fromStringToLongList(s: String): List<Long> {
        return s.split(", ").mapNotNull { it.toLongOrNull() }
    }

    @TypeConverter
    fun dateToLong(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun longToDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    @TypeConverter
    fun stringToStringBooleanMap(value: String): Map<String, Boolean> {
        return Gson().fromJson(value,  object : TypeToken<Map<String, Boolean>>() {}.type)
    }

    @TypeConverter
    fun stringBooleanMapToString(value: Map<String, Boolean>?): String {
        return if(value == null) "" else Gson().toJson(value)
    }
}