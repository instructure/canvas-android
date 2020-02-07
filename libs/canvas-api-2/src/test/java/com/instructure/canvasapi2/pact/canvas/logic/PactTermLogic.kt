package com.instructure.canvasapi2.pact.canvas.logic

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Term
import io.pactfoundation.consumer.dsl.LambdaDslObject
import org.junit.Assert.assertNotNull

/**
 * Populate a Term object in a Pact specification.  This seemed simple enough to not need
 * its own code module, but we may end up moving this to its own module in the future.
 */
fun LambdaDslObject.populateTermFields() : LambdaDslObject {
    this
            .id("id")
            .stringType("name")
            .stringMatcher("start_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .stringMatcher("end_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")

    return this
}

/**
 * Assert that a term object in a response has been properly populated.
 */
fun assertTermPopulated(description: String, term: Term) {
    assertNotNull("$description + id", term.id)
    assertNotNull("$description + name", term.name)
    assertNotNull("$description + endAt", term.endAt) // startAt is private
    assertNotNull("$description + startDate", term.startDate)
    assertNotNull("$description + endDate", term.endDate)
}
