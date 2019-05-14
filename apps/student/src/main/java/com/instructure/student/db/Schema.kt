package com.instructure.student.db

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.student.Submission
import com.instructure.student.db.sqlColAdapters.CanvasContextAdapter
import com.instructure.student.db.sqlColAdapters.DateAdapter
import com.squareup.sqldelight.db.SqlDriver

fun createQueryWrapper(driver: SqlDriver): StudentDb {
    return StudentDb(
        driver = driver,
        submissionAdapter = Submission.Adapter(
            lastActivityDateAdapter = DateAdapter(),
            canvasContextAdapter = CanvasContextAdapter()
        )
    )
}

object Schema : SqlDriver.Schema by StudentDb.Schema {
    override fun create(driver: SqlDriver) {
        StudentDb.Schema.create(driver)

        // Add some test data
        createQueryWrapper(driver).apply {
            submissionQueries.insertOnlineTextSubmission(
                "test",
                "some name",
                123L,
                CanvasContext.getGenericContext(CanvasContext.Type.COURSE, 124L)
            )
            submissionQueries.insertOnlineTextSubmission(
                "test2",
                "some name2",
                1234L,
                CanvasContext.getGenericContext(CanvasContext.Type.COURSE, 124L)
            )
        }
    }
}