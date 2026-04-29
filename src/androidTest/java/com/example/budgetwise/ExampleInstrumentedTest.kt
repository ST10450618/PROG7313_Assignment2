package com.example.budgetwise

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

// NOTE: This file is superseded by com.budgetwise.app.ExampleInstrumentedTest.
// Kept here as a placeholder — do not add tests to this file.
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.budgetwise.app", appContext.packageName)
    }
}
