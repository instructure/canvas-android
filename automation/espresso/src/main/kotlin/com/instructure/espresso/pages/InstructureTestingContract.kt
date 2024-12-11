package com.instructure.espresso.pages

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

interface InstructureTestingContract {
    @Test fun displaysPageObjects()
    @Before fun preLaunchSetup()
    @Rule fun chain(): TestRule
}
