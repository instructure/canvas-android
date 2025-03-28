package com.instructure.pandautils.room.assignment.list.converter

import androidx.room.TypeConverter
import com.instructure.pandautils.features.assignments.list.filter.AssignmentFilter

class AssignmentFilterConverter {
    private val separator = ","

    @TypeConverter
    fun restoreEnumList(enumNames: String): List<AssignmentFilter> = enumNames.ifEmpty { return emptyList() }.split(separator).map { AssignmentFilter.valueOf(it) }

    @TypeConverter
    fun saveEnumListToString(enums: List<AssignmentFilter>) = enums.joinToString(separator = separator) { it.name }
}