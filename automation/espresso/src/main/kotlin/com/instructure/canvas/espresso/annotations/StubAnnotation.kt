package com.instructure.canvas.espresso.annotations

// When applied to a test method, denotes that the test is stubbed out and not yet implemented
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Stub(val description: String = "")
