package com.instructure.student.db

import com.squareup.sqldelight.db.SqlDriver

fun createQueryWrapper(driver: SqlDriver): StudentDb {
    return StudentDb(
        driver = driver
    )
}

object Schema : SqlDriver.Schema by StudentDb.Schema {
    override fun create(driver: SqlDriver) {
        StudentDb.Schema.create(driver)

        // Add some test data
        createQueryWrapper(driver).apply {
            submissionQueries.insertOnlineTextSubmission("test", "some name", 123L, "Course", 124L)
            submissionQueries.insertOnlineTextSubmission("test2", "some name2", 1234L, "Course", 1245L)
        }
    }
}