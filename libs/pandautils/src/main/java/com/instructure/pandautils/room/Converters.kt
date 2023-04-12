package com.instructure.pandautils.room

import androidx.room.TypeConverter
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
}