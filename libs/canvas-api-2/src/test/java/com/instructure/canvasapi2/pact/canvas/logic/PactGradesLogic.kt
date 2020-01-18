package com.instructure.canvasapi2.pact.canvas.logic

import com.instructure.canvasapi2.models.Grades
import io.pactfoundation.consumer.dsl.LambdaDslObject
import org.junit.Assert

/**
 * Populate a Grades object in a Pact specification.
 */
fun LambdaDslObject.populateGradesObject() : LambdaDslObject {
    this
            .stringType("html_url")
            .numberType("current_score", "final_score")
            .stringType("current_grade")
            .stringType("final_grade")

    return this
}

/**
 * Assert that a Grades object in a response has been populated correctly.
 */
fun assertGradesPopulated(description: String, grades: Grades) {
    Assert.assertNotNull("$description + html_url", grades.htmlUrl)
    Assert.assertNotNull("$description + currentScore", grades.currentScore)
    Assert.assertNotNull("$description + currentGrade", grades.currentGrade)
    Assert.assertNotNull("$description + finalScore", grades.finalScore)
    Assert.assertNotNull("$description + finalGrade", grades.finalGrade)
}