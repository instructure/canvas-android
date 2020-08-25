/*
 * Copyright (C) 2018 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
@file:Suppress("PackageDirectoryMismatch")

package okreplay

import com.instructure.espresso.ditto.DittoConfig
import com.instructure.espresso.ditto.DittoMode
import okreplay.CaseFormat.LOWER_CAMEL
import okreplay.CaseFormat.LOWER_UNDERSCORE
import org.junit.runner.Description

/**
 * Manages tapes and sessions for playback and recording. Simply call [start] to begin a session and [stop] to end it.
 */
class DittoRecorder(private val config: DittoConfig) {

    private var tape: DittoTape? = null

    private val tapeLoader get() = YamlTapeLoader(config.tapeRoot)

    /**
     * Starts the recorder. In [PLAY][DittoMode.PLAY] mode this will attempt to load an existing tape with the
     * with the specified [tapeName]. If such a tape does not exist, or the mode is [RECORD][DittoMode.RECORD], a new
     * tape will be created.
     *
     * If [sequential] is true then the recorder will attempt to play/record a unique response for each request,
     * including duplicate requests. If false, only one response will be played/recorded per matching request.
     *
     * Request matching can be customized via [matchRule]. If null, the default [config] match rule will be used.
     *
     * [mode] must be either [PLAY][DittoMode.PLAY] or [RECORD][DittoMode.RECORD], otherwise
     * an [IllegalArgumentException] will be thrown.
     */
    fun start(tapeName: String, sequential: Boolean, matchRule: MatchRule?, mode: DittoMode) {
        val tapeMode = when (mode) {
            DittoMode.RECORD -> if (sequential) TapeMode.WRITE_SEQUENTIAL else TapeMode.WRITE_ONLY
            DittoMode.PLAY -> if (sequential) TapeMode.READ_SEQUENTIAL else TapeMode.READ_ONLY
            else -> throw IllegalArgumentException("DittoRecorder should only be run in RECORD or PLAY modes")
        }
        val ymlTape = if (mode == DittoMode.RECORD) tapeLoader.newTape(tapeName) else tapeLoader.loadTape(tapeName)
        tape = DittoTape(ymlTape).also {
            it.mode = tapeMode
            it.matchRule = matchRule ?: config.matchRule
            DittoConfig.interceptor.start(it)
        }
    }

    /**
     * Stops the recorder. If [shouldSave] is true then the recorded tape will be written to disk.
     */
    fun stop(shouldSave: Boolean) {
        if (shouldSave) tape?.let { tapeLoader.writeTape(it.wrappedTape) }
        tape = null
        DittoConfig.interceptor.stop()
    }

    companion object {

        /** Generates a default tape name for the given [description] */
        fun defaultTapeName(description: Description): String = with(description) {
            methodName ?: throw IllegalAccessException("Method name cannot be for the provided description.")
            return LOWER_CAMEL.to(LOWER_UNDERSCORE, methodName).replace('_', ' ')
        }

    }


}
