//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//


package com.instructure.soseedy.utils

import org.junit.After
import org.junit.Assert
import org.junit.Before
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

abstract class CommandLineTest {

    private var sysOut = System.out
    var stdOut: ByteArrayOutputStream? = null

    val missingRequiredOption = "Missing required option"
    val missingRequiredOptions = "Missing required options"

    @Before
    fun setup() {
        emptyResultsDir()
        stdOut = ByteArrayOutputStream()
        System.setOut(PrintStream(stdOut))
        Assert.assertNotNull(stdOut)
    }

    @After
    fun tearDown() {
        System.setOut(sysOut)
    }

    private fun emptyResultsDir() {
        val csvDir = FileWriter.csvDir
        File(csvDir).listFiles().forEach { file ->
            file.delete()
        }
        Assert.assertTrue(File(csvDir).listFiles().isEmpty())
    }

    fun testCsvFile(): File {
        val file = File(FileWriter.csvDir).listFiles().first()
        Assert.assertNotNull(file)
        return file
    }
}
