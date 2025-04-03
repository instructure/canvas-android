package com.instructure.pandautils.room.common

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.GradingSchemeRow
import java.util.Date

class Converters {
    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return list.joinToString()
    }

    @TypeConverter
    fun fromStringToListString(s: String): List<String> {
        return s.takeIf { it.isNotEmpty() }?.split(", ").orEmpty()
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
    fun fromLongArray(array: LongArray?) : String? {
        return array?.joinToString()
    }

    @TypeConverter
    fun fromStringToLongArray(s: String?): LongArray? {
        return s?.split(", ")?.mapNotNull { it.toLongOrNull() }?.toLongArray()
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
    fun fromIntList(list: List<Int>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun toIntList(s: String?): List<Int>? {
        return s?.split(",")?.mapNotNull { it.toIntOrNull() }
    }

    @TypeConverter
    fun stringToStringBooleanMap(value: String): Map<String, Boolean> {
        return Gson().fromJson(value,  object : TypeToken<Map<String, Boolean>>() {}.type)
    }

    @TypeConverter
    fun stringBooleanMapToString(value: Map<String, Boolean>?): String {
        return if(value == null) "" else Gson().toJson(value)
    }

    @TypeConverter
    fun gradingSchemeRowListToString(value: List<GradingSchemeRow>?): String {
        return if(value == null) "" else Gson().toJson(value)
    }

    @TypeConverter
    fun stringToGradingSchemeRowList(value: String): List<GradingSchemeRow>? {
        return Gson().fromJson(value,  object : TypeToken<List<GradingSchemeRow>>() {}.type)
    }

    @TypeConverter
    fun canvasContextToString(value: CanvasContext): String {
        return value.contextId
    }

    @TypeConverter
    fun stringToCanvasContext(value: String): CanvasContext {
        return CanvasContext.fromContextCode(value) ?: CanvasContext.defaultCanvasContext()
    }
}