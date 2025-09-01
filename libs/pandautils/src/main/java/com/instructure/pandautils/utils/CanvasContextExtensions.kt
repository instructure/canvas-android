/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
@file:JvmName("CanvasContextExtensions")
package com.instructure.pandautils.utils

import android.os.Bundle
import androidx.annotation.ColorInt
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.interactions.router.Route

@get:ColorInt
val CanvasContext?.color: Int get() {
    val themedColor = ColorKeeper.getOrGenerateColor(this)
    return if (ColorKeeper.darkTheme) themedColor.dark else themedColor.light
}

@get:ColorInt
val CanvasContext?.lightColor: Int get() {
    val themedColor = ColorKeeper.getOrGenerateColor(this)
    return themedColor.light
}

@get:ColorInt
val User?.studentColor: Int get() {
    val themedColor = ColorKeeper.getOrGenerateUserColor(this)
    return if (ColorKeeper.darkTheme) themedColor.dark else themedColor.light
}

@get:ColorInt
val CanvasContext?.courseOrUserColor: Int get() {
    return if (this is User) this.studentColor else this.color
}

val CanvasContext.isCourse: Boolean get() = this.type == CanvasContext.Type.COURSE
val CanvasContext.isGroup: Boolean get() = this.type == CanvasContext.Type.GROUP
val CanvasContext.isCourseOrGroup: Boolean get() = this.type == CanvasContext.Type.GROUP || this.type == CanvasContext.Type.COURSE
val CanvasContext.isNotUser: Boolean get() = this.type != CanvasContext.Type.USER
val CanvasContext.isCourseContext: Boolean get() = this.type != CanvasContext.Type.USER
val CanvasContext.isUser: Boolean get() = this.type  == CanvasContext.Type.USER

// It's possible they routed via a group context, which isn't supported in the Teacher app yet
fun CanvasContext.isDesigner(): Boolean = this.isCourseContext && (this as? Course)?.isDesigner == true

fun CanvasContext.makeBundle(bundle: Bundle = Bundle(), block: Bundle.() -> Unit = {}): Bundle =
    bundle.apply {
        putParcelable(Const.CANVAS_CONTEXT, this@makeBundle)
        block()
    }

val Route.argsWithContext get() = canvasContext?.makeBundle(arguments) ?: arguments

fun Bundle.getCanvasContext(): CanvasContext? = this.getParcelable(Const.CANVAS_CONTEXT)
fun Bundle.hasCanvasContext(): Boolean = this.containsKey(Const.CANVAS_CONTEXT)
