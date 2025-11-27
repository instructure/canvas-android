package com.instructure.student.test

import org.junit.Test
import org.junit.Assert.*

class DummyFailingTest {
    
    @Test
    fun `this test always fails`() {
        fail("This is a dummy test that always fails")
    }
    
    @Test
    fun `this test also fails`() {
        assertEquals("Expected value", "Actual value")
    }
    
    @Test
    fun `this test passes`() {
        assertTrue(true)
    }
}
