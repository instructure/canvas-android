package com.instructure.canvas.espresso.annotations

// When applied to a test method, denotes that the test will run "end-to-end", generating real network requests
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class E2E