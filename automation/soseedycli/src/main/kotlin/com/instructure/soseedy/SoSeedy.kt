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


package com.instructure.soseedy

import com.instructure.soseedy.tasks.SeedCourses
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command(
        name                 = "soseedycli.jar",
        header               = ["Execute a data seeding command."],
        description          = ["Wraps multiple data seeding commands that can be used to generate large sets of data" +
                    " for manual or performance testing."],
        sortOptions          = false,
        headerHeading        = "@|bold,underline Usage:|@%n%n",
        synopsisHeading      = "%n",
        descriptionHeading   = "%n@|bold,underline Description:|@%n%n",
        parameterListHeading = "%n@|bold,underline Parameters:|@%n",
        optionListHeading    = "%n@|bold,underline Options:|@%n",
        subcommands = [SeedCourses::class]
)
object SoSeedy : Runnable {
    @Option(names = ["-v", "--version"], description = ["Prints the version"])
    private var version = false

    override fun run() {
        if (version) {
            println("v0.1")
            return
        }
        CommandLine.usage(SoSeedy, System.out)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        CommandLine.run<Runnable>(SoSeedy, System.out, *args)
    }
}
