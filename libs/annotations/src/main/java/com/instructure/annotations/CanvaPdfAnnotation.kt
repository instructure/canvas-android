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
@file:Suppress("CONFLICTING_INHERITED_JVM_DECLARATIONS")

package com.instructure.annotations

import android.graphics.RectF
import com.pspdfkit.annotations.*
import com.pspdfkit.annotations.stamps.StampType

class CanvaInkAnnotation(info: CanvaPdfAnnotation) : InkAnnotation(info.page), PSCanvaInterface by info

class CanvaHighlightAnnotation(info: CanvaPdfAnnotation) : HighlightAnnotation(info.page, info.rectList ?: emptyList()), PSCanvaInterface by info

class CanvaStrikeOutAnnotation(info: CanvaPdfAnnotation) : StrikeOutAnnotation(info.page, info.rectList ?: emptyList()), PSCanvaInterface by info

class CanvaSquareAnnotation(info: CanvaPdfAnnotation) : SquareAnnotation(info.page, info.rect ?: RectF()), PSCanvaInterface by info

class CanvaFreeTextAnnotation(info: CanvaPdfAnnotation, contents: String) : FreeTextAnnotation(info.page, info.rect!!, contents), PSCanvaInterface by info

class CanvaNoteAnnotation(info: CanvaPdfAnnotation, iconName: String, contents: String ) : NoteAnnotation(info.page, info.rect ?: RectF(), contents, iconName), PSCanvaInterface by info

class CanvaStampAnnotation(info: CanvaPdfAnnotation) : StampAnnotation(info.page, info.rect ?: RectF(), StampType.APPROVED), PSCanvaInterface by info

