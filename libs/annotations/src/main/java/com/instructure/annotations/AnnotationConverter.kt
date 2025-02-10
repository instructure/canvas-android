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
package com.instructure.annotations

import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.graphics.RectF
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import com.instructure.canvasapi2.models.canvadocs.CanvaDocCoordinate
import com.instructure.canvasapi2.models.canvadocs.CanvaDocInkList
import com.instructure.canvasapi2.utils.ApiPrefs
import com.pspdfkit.annotations.*
import com.pspdfkit.annotations.Annotation
import java.util.*


fun CanvaDocAnnotation.convertCanvaDocAnnotationToPDF(context: Context) : Annotation? {
    return when(annotationType) {
        CanvaDocAnnotation.AnnotationType.INK -> convertInkType(this, context)
        CanvaDocAnnotation.AnnotationType.HIGHLIGHT -> convertHighlightType(this, context)
        CanvaDocAnnotation.AnnotationType.STRIKEOUT -> convertStrikeoutType(this, context)
        CanvaDocAnnotation.AnnotationType.SQUARE -> convertSquareType(this, context)
        CanvaDocAnnotation.AnnotationType.FREE_TEXT -> convertFreeTextType(this, context)
        CanvaDocAnnotation.AnnotationType.TEXT -> convertTextType(this)
        else -> null
    }
}

fun Annotation.convertPDFAnnotationToCanvaDoc(canvaDocId: String) : CanvaDocAnnotation? {
    return when(type) {
        AnnotationType.INK -> (this as InkAnnotation).toCanvaDocAnnotation(canvaDocId)
        AnnotationType.HIGHLIGHT -> (this as HighlightAnnotation).convertToCanvaDoc(canvaDocId)
        AnnotationType.STRIKEOUT -> (this as StrikeOutAnnotation).convertToCanvaDoc(canvaDocId)
        AnnotationType.SQUARE -> (this as SquareAnnotation).convertToCanvaDoc(canvaDocId)
        AnnotationType.STAMP -> (this as StampAnnotation).convertToCanvaDoc(canvaDocId)
        AnnotationType.FREETEXT -> (this as FreeTextAnnotation).convertToCanvaDoc(canvaDocId)
        else -> null
    }
}

//region canvaDoc to PDF
private fun convertInkType(canvaDocAnnotation: CanvaDocAnnotation, context: Context): InkAnnotation {
    val inkAnnotation = CanvaInkAnnotation(
            CanvaPdfAnnotation(
                    page = canvaDocAnnotation.page,
                    userId = canvaDocAnnotation.userId
            )
    )

    inkAnnotation.lineWidth = canvaDocAnnotation.width ?: 0f
    inkAnnotation.boundingBox = canvaDocAnnotation.rect?.let { RectF(it[0][0], it[0][1], it[1][0], it[1][1]) } ?: RectF()
    inkAnnotation.lines = canvaDocAnnotation.inklist?.gestures?.map { it.map { PointF(it.x, it.y) }.toMutableList() }!!.toMutableList()
    inkAnnotation.color = canvaDocAnnotation.getColorInt(ContextCompat.getColor(context, (R.color.textInfo)))
    inkAnnotation.contents = canvaDocAnnotation.contents
    inkAnnotation.name = canvaDocAnnotation.annotationId

    return inkAnnotation
}

private fun convertHighlightType(canvaDocAnnotation: CanvaDocAnnotation, context: Context): HighlightAnnotation {
    val rectList = coordsToListOfRectfs(canvaDocAnnotation.coords)

    val highLightAnnotation = CanvaHighlightAnnotation(CanvaPdfAnnotation(
            page = canvaDocAnnotation.page,
            rectList = rectList,
            userId = canvaDocAnnotation.userId
    ))
    highLightAnnotation.contents = canvaDocAnnotation.contents
    highLightAnnotation.color = canvaDocAnnotation.getColorInt(ContextCompat.getColor(context, (R.color.textInfo)))
    highLightAnnotation.name = canvaDocAnnotation.annotationId

    return highLightAnnotation
}

private fun convertStrikeoutType(canvaDocAnnotation: CanvaDocAnnotation, context: Context): StrikeOutAnnotation {
    val rectList = coordsToListOfRectfs(canvaDocAnnotation.coords)

    val strikeOutAnnotation = CanvaStrikeOutAnnotation(CanvaPdfAnnotation(
            page = canvaDocAnnotation.page,
            rectList = rectList,
            userId = canvaDocAnnotation.userId
    ))
    strikeOutAnnotation.contents = canvaDocAnnotation.contents
    strikeOutAnnotation.color = canvaDocAnnotation.getColorInt(ContextCompat.getColor(context, (R.color.textInfo)))
    strikeOutAnnotation.name = canvaDocAnnotation.annotationId

    return strikeOutAnnotation
}

private fun convertSquareType(canvaDocAnnotation: CanvaDocAnnotation, context: Context) : SquareAnnotation {
    val rect = canvaDocAnnotation.rect?.let { RectF(it[0][0], it[0][1], it[1][0], it[1][1]) }
    val squareAnnotation = CanvaSquareAnnotation(CanvaPdfAnnotation(
            page = canvaDocAnnotation.page,
            rect = rect,
            userId = canvaDocAnnotation.userId
    ))
    squareAnnotation.contents = canvaDocAnnotation.contents
    squareAnnotation.color = canvaDocAnnotation.getColorInt(ContextCompat.getColor(context, (R.color.textInfo)))
    squareAnnotation.borderWidth = canvaDocAnnotation.width?.toFloat() ?: 2f //default width of 2
    squareAnnotation.name = canvaDocAnnotation.annotationId

    return squareAnnotation
}

private fun convertFreeTextType(canvaDocAnnotation: CanvaDocAnnotation, context: Context) : FreeTextAnnotation {
    val rect = canvaDocAnnotation.rect?.let { RectF(it[0][0], it[1][1], it[1][0], it[0][1]) }
    val freeTextAnnotation = CanvaFreeTextAnnotation(CanvaPdfAnnotation(
            page = canvaDocAnnotation.page,
            rect = rect,
            userId = canvaDocAnnotation.userId
    ), contents = canvaDocAnnotation.contents ?: "")

    freeTextAnnotation.color = canvaDocAnnotation.getColorInt(ContextCompat.getColor(context, (R.color.darkGrayAnnotation)))
    freeTextAnnotation.name = canvaDocAnnotation.annotationId
    freeTextAnnotation.textSize = getTextSizeFromFont(canvaDocAnnotation.font)
    freeTextAnnotation.fillColor = Color.TRANSPARENT
    freeTextAnnotation.flags.remove(AnnotationFlags.NOZOOM)

    return freeTextAnnotation
}

private fun getTextSizeFromFont(font: String?): Float{
    // 14pt, 22pt, 38pt are the supported font sizes on web, but these don't match up well to PSPDFkit's font
    return when {
        font?.contains("14pt") == true -> smallFont
        font?.contains("22pt") == true -> mediumFont
        font?.contains("38pt") == true -> largeFont
        else -> smallFont
    }
}

private fun convertTextType(canvaDocAnnotation: CanvaDocAnnotation) : StampAnnotation {
    val rect = canvaDocAnnotation.rect?.let { RectF(it[0][0], it[0][1], it[1][0], it[1][1]) }
    val stampAnnotation = CanvaStampAnnotation(CanvaPdfAnnotation(
            page = canvaDocAnnotation.page,
            rect = rect,
            userId = canvaDocAnnotation.userId
    ))

    stampAnnotation.name = canvaDocAnnotation.annotationId
    stampAnnotation.subject = getStampSubjectFromColorHex(canvaDocAnnotation.color)

    return stampAnnotation
}
//endregion


//region PDF to canvadoc
fun InkAnnotation.toCanvaDocAnnotation(canvaDocId: String): CanvaDocAnnotation {
    // inkList is a list of lists; Kotlin adds wildcards to generic lists and Paperparcel can't handle that
    @Suppress("UNCHECKED_CAST")
    return CanvaDocAnnotation(
            annotationId = this.name ?: "",
            userName = ApiPrefs.user?.shortName,
            documentId = canvaDocId,
            subject = CanvaDocAnnotation.INK_SUBJECT,
            page = this.pageIndex,
            width = this.lineWidth,
            annotationType = CanvaDocAnnotation.AnnotationType.INK,
            rect = listOfRectsToListOfListOfFloats(listOf(this.boundingBox)),
            color = this.colorToHexString(),
            contents = this.contents,
            inklist = CanvaDocInkList(convertListOfPointFToCanvaDocCoordinates(this.lines)),
            isEditable = true
    )

}

fun HighlightAnnotation.convertToCanvaDoc(canvaDocId: String): CanvaDocAnnotation {
    return CanvaDocAnnotation(
            annotationId = this.name ?: "",
            userName = ApiPrefs.user?.shortName,
            documentId = canvaDocId,
            subject = CanvaDocAnnotation.HIGHLIGHT_SUBJECT,
            page = this.pageIndex,
            context = this.getContext(),
            width = this.borderWidth,
            annotationType = CanvaDocAnnotation.AnnotationType.HIGHLIGHT,
            rect = listOfRectsToListOfListOfFloats(this.rects),
            coords = convertListOfRectsToListOfListOfListOfFloats(this.rects),
            color = this.colorToHexString(),
            contents = this.contents,
            isEditable = true
    )
}



fun StrikeOutAnnotation.convertToCanvaDoc(canvaDocId: String): CanvaDocAnnotation {
    return CanvaDocAnnotation(
            annotationId = this.name ?: "",
            userName = ApiPrefs.user?.shortName,
            documentId = canvaDocId,
            subject = CanvaDocAnnotation.STRIKEOUT_SUBJECT,
            page = this.pageIndex,
            context = this.getContext(),
            width = this.borderWidth,
            annotationType = CanvaDocAnnotation.AnnotationType.STRIKEOUT,
            rect = listOfRectsToListOfListOfFloats(this.rects),
            coords = convertListOfRectsToListOfListOfListOfFloats(this.rects),
            color = this.colorToHexString(),
            contents = this.contents,
            isEditable = true
    )
}

fun SquareAnnotation.convertToCanvaDoc(canvaDocId: String): CanvaDocAnnotation {
    return CanvaDocAnnotation(
            annotationId = this.name ?: "",
            userName = ApiPrefs.user?.shortName,
            documentId = canvaDocId,
            subject = CanvaDocAnnotation.SQUARE_SUBJECT,
            page = this.pageIndex,
            context = this.getContext(),
            width = this.borderWidth,
            annotationType = CanvaDocAnnotation.AnnotationType.SQUARE,
            rect = listOfRectsToListOfListOfFloats(listOf(this.boundingBox)),
            color = this.colorToHexString(),
            contents = this.contents,
            isEditable = true
    )
}

fun StampAnnotation.convertToCanvaDoc(canvaDocId: String): CanvaDocAnnotation {
    return CanvaDocAnnotation(
            annotationId = this.name ?: "",
            userName = ApiPrefs.user?.shortName,
            documentId = canvaDocId,
            subject = CanvaDocAnnotation.TEXT_SUBJECT,
            page = this.pageIndex,
            context = this.getContext(),
            width = this.borderWidth,
            annotationType = CanvaDocAnnotation.AnnotationType.TEXT,
            rect = listOfRectsToListOfListOfFloats(listOf(this.boundingBox)),
            icon = "Comment",
            color = getColorHexFromStamp(this.subject ?: this.title),
            contents = this.contents,
            iconColor= this.colorToHexString(),
            isEditable = true
    )
}

fun FreeTextAnnotation.convertToCanvaDoc(canvaDocId: String): CanvaDocAnnotation {
    return CanvaDocAnnotation(
            annotationId = this.name ?: "",
            userName = ApiPrefs.user?.shortName,
            documentId = canvaDocId,
            subject = CanvaDocAnnotation.FREE_TEXT_SUBJECT,
            page = this.pageIndex,
            context = this.getContext(),
            width = this.borderWidth,
            annotationType = CanvaDocAnnotation.AnnotationType.FREE_TEXT,
            rect = listOfRectsToListOfListOfFloats(listOf(this.boundingBox)),
            color = this.colorToHexString(),
            contents = this.contents,
            isEditable = true
    )
}

fun createCommentReplyAnnotation(contents: String, inReplyTo: String, canvaDocId: String, userId: String, page: Int): CanvaDocAnnotation {
    return CanvaDocAnnotation(
            annotationId = "",
            ctxId = "",
            userId = userId,
            userName = "",
            createdAt = "",
            documentId = canvaDocId,
            subject = CanvaDocAnnotation.COMMENT_REPLY_SUBJECT,
            page = page,
            context = "",
            annotationType = CanvaDocAnnotation.AnnotationType.COMMENT_REPLY,
            contents = contents,
            inReplyTo = inReplyTo,
            isEditable = true)
}
//endregion


fun listOfRectsToListOfListOfFloats(rects: List<RectF>?): ArrayList<ArrayList<Float>>? {
    if (rects == null || rects.isEmpty())
        return null

    val listOfLists = ArrayList<ArrayList<Float>>()
    listOfLists.add(arrayListOf(rects.minByOrNull { it.left }?.left ?: 0f, rects.minByOrNull { it.bottom }?.bottom ?: 0f))
    listOfLists.add(arrayListOf(rects.maxByOrNull { it.right }?.right ?: 0f, rects.maxByOrNull { it.top }?.top ?: 0f))

    return listOfLists
}

fun coordsToListOfRectfs(coords: List<List<List<Float>>>?) : MutableList<RectF> {
    val rectList = mutableListOf<RectF>()
    coords?.let {
        it.forEach {
            val tempRect = RectF(it[0][0], it[0][1], it[3][0], it[3][1])
            rectList.add(tempRect)
        }
    }

    return rectList
}

fun convertListOfRectsToListOfListOfListOfFloats(rects: MutableList<RectF>?): ArrayList<ArrayList<ArrayList<Float>>>? {
    if (rects == null || rects.isEmpty()) {
        return null
    }
    // The distance between the top of the line and the bottom
    val rectList: ArrayList<ArrayList<ArrayList<Float>>> = ArrayList()
    rects.forEach {
        val posList = arrayListOf<ArrayList<Float>>()

        val bottomLineLeftTop = arrayListOf(it.left, it.bottom)
        val bottomLineRightBottom = arrayListOf(it.right, it.bottom)
        posList.add(bottomLineLeftTop)
        posList.add(bottomLineRightBottom)

        val topLineLeftTop = arrayListOf(it.left, it.top)
        val topLineRightBottom = arrayListOf(it.right, it.top)
        posList.add(topLineLeftTop)
        posList.add(topLineRightBottom)

        rectList.add(posList)
    }
    return rectList
}

fun convertListOfPointFToCanvaDocCoordinates(linesList: List<List<PointF>>): ArrayList<ArrayList<CanvaDocCoordinate>> {
    val newList = ArrayList<ArrayList<CanvaDocCoordinate>>()
    for((position, list) in linesList.withIndex()) {
        newList.add(ArrayList())
        for(point in list) {
            newList[position].add(CanvaDocCoordinate(point.x, point.y))
        }
    }

    return newList
}

fun Annotation.colorToHexString() = String.format("#%06X", 0xFFFFFF and this.color)

fun generateAnnotationId() = UUID.randomUUID().toString()

fun Annotation.transformStamp() {
    val centerX = this.boundingBox.centerX()
    val centerY = this.boundingBox.centerY()

    // Goal dimension is 9.33w x 13.33h
    val newRect = RectF(centerX - 4.665f, centerY + 6.665f, centerX + 4.665f, centerY - 6.665f)

    this.boundingBox = newRect
}

private fun getStampSubjectFromColorHex(color: String?): String {
    return when (color) {
        blackStampHex -> blackStampSubject
        blueStampHex -> blueStampSubject
        brownStampHex -> brownStampSubject
        greenStampHex -> greenStampSubject
        navyStampHex -> navyStampSubject
        orangeStampHex -> orangeStampSubject
        pinkStampHex -> pinkStampSubject
        purpleStampHex -> purpleStampSubject
        redStampHex -> redStampSubject
        yellowStampHex -> yellowStampSubject
        else -> blueStampSubject
    }
}

private fun getColorHexFromStamp(stampTitle: String?): String {
    return when (stampTitle) {
        blackStampSubject -> blackStampHex
        blueStampSubject -> blueStampHex
        brownStampSubject -> brownStampHex
        greenStampSubject -> greenStampHex
        navyStampSubject -> navyStampHex
        orangeStampSubject -> orangeStampHex
        pinkStampSubject -> pinkStampHex
        purpleStampSubject -> purpleStampHex
        redStampSubject -> redStampHex
        yellowStampSubject -> yellowStampHex
        else -> blueStampHex
    }
}

// Stamp file asset names
const val blackStampFile = "stamps/blackpoint.pdf"
const val blueStampFile = "stamps/bluepoint.pdf"
const val brownStampFile = "stamps/brownpoint.pdf"
const val greenStampFile = "stamps/greenpoint.pdf"
const val navyStampFile = "stamps/navypoint.pdf"
const val orangeStampFile = "stamps/orangepoint.pdf"
const val pinkStampFile = "stamps/pinkpoint.pdf"
const val purpleStampFile = "stamps/purplepoint.pdf"
const val redStampFile = "stamps/redpoint.pdf"
const val yellowStampFile = "stamps/yellowpoint.pdf"

// Stamp subject names
const val blackStampSubject = "BlackStamp"
const val blueStampSubject = "BlueStamp"
const val brownStampSubject = "BrownStamp"
const val greenStampSubject = "GreenStamp"
const val navyStampSubject = "NavyStamp"
const val orangeStampSubject = "OrangeStamp"
const val pinkStampSubject = "PinkStamp"
const val purpleStampSubject = "PurpleStamp"
const val redStampSubject = "RedStamp"
const val yellowStampSubject = "YellowStamp"

// Stamp hex color codes
const val blackStampHex = "#363636"
const val blueStampHex = "#008EE2"
const val brownStampHex = "#8D6437"
const val greenStampHex = "#00AC18"
const val navyStampHex = "#234C9F"
const val orangeStampHex = "#FC5E13"
const val pinkStampHex = "#C31FA8"
const val purpleStampHex = "#741865"
const val redStampHex = "#EE0612"
const val yellowStampHex = "#FCB900"

// Free Text Annotation Font sizes
const val smallFont = 10f
const val mediumFont = 18f
const val largeFont = 32f