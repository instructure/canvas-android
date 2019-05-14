package com.instructure.student.db.sqlColAdapters

import com.squareup.sqldelight.ColumnAdapter
import com.instructure.canvasapi2.models.CanvasContext

class CanvasContext : ColumnAdapter<CanvasContext, String> {
    override fun decode(databaseValue: String): CanvasContext {
        val parsed = databaseValue.split(",")
        val type = when (parsed[0]) {
            CanvasContext.Type.COURSE.name -> CanvasContext.Type.COURSE
            else -> { CanvasContext.Type.UNKNOWN}
        }
        val id = parsed[1].toLong()

        return CanvasContext.getGenericContext(type, id, "")
    }

    override fun encode(value: CanvasContext): String {
        return "${value.type},${value.id}"
    }

}