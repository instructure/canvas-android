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
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import com.instructure.canvasapi2.models.canvadocs.CanvaDocCoordinate
import com.instructure.canvasapi2.models.canvadocs.CanvaDocInkList
import com.instructure.canvasapi2.utils.ApiPrefs
import com.pspdfkit.annotations.*
import com.pspdfkit.annotations.Annotation
import com.pspdfkit.utils.EdgeInsets
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
    getFontFamilyFromFont(canvaDocAnnotation.font)?.let { freeTextAnnotation.fontName = it }
    freeTextAnnotation.fillColor = Color.TRANSPARENT
    freeTextAnnotation.flags.remove(AnnotationFlags.NOZOOM)
    shrinkFreeTextToFitBox(freeTextAnnotation)

    return freeTextAnnotation
}

/**
 * Web sizes a free-text `rect` to exactly fit its own rendering of the text. PSPDFKit's Android text
 * layout is wider and taller than web's at the same nominal point size (see MBL-20131), so rendering
 * at web's parsed size can overflow that rect - and PSPDFKit clips overflow instead of wrapping the
 * box, silently losing text. Shrinks [FreeTextAnnotation.getTextSize] - never grows it - until the
 * wrapped text fits. Any failure here (missing bounds, measurement throwing) leaves the annotation at
 * its already-assigned size rather than breaking annotation loading.
 *
 * Measurement uses [TextPaint]'s default typeface. PSPDFKit's own substitution for a family absent from
 * the device is not exposed by its API, so measurement and render can diverge; that residual risk is
 * unavoidable with the tools the SDK offers.
 *
 * The winning measurement from [largestFittingPointSize] is reused for [applyMeanLineErrorTopInset]
 * instead of being recomputed - both the size decision and the inset arithmetic come from the exact
 * same [StaticLayout] result, and free-text annotations can otherwise trigger dozens of layout passes
 * on the main thread.
 *
 * Shrinking to fit also leaves vertical slack in the box (web's leading is tighter than Android's
 * substituted font's), which [applyMeanLineErrorTopInset] partially compensates for.
 */
private fun shrinkFreeTextToFitBox(freeTextAnnotation: FreeTextAnnotation) {
    val contents = freeTextAnnotation.contents
    if (contents.isNullOrBlank()) return

    val box = freeTextAnnotation.boundingBox ?: return
    // Fields are in PDF space (top > bottom), so RectF.height() - which assumes the opposite
    // convention - would return a negative number here.
    val boxWidth = box.right - box.left
    val boxHeight = box.top - box.bottom
    val insets = runCatching { freeTextAnnotation.textInsets }.getOrNull()
    val usableWidth = boxWidth - (insets?.left ?: 0f) - (insets?.right ?: 0f)
    val usableHeight = boxHeight - (insets?.top ?: 0f) - (insets?.bottom ?: 0f)
    if (usableWidth <= 0f || usableHeight <= 0f) return

    val maxHeight = usableHeight * (1f - freeTextShrinkSafetyMarginFraction)

    val fitResult = runCatching {
        largestFittingPointSize(
            requestedPointSize = freeTextAnnotation.textSize,
            minPointSize = minShrinkPointSize,
            maxHeight = maxHeight,
        ) { candidate -> measureWrappedText(contents, usableWidth, candidate) }
    }.getOrNull() ?: return

    freeTextAnnotation.textSize = fitResult.pointSize
    applyMeanLineErrorTopInset(freeTextAnnotation, usableHeight, maxHeight, fitResult.measurement)
}

/**
 * Best-effort halving of a residual vertical error that shrink-to-fit cannot remove: the SDK exposes
 * no line-spacing control and no usable custom-font mechanism (see MBL-20131 progress notes), so once
 * text is shrunk to fit the box's width, our leading is still tighter than web's - each successive
 * line renders further above where web puts it. Shifting the whole block down by the *mean* per-line
 * error (rather than leaving it all accumulated at the last line) halves the worst-case offset. This
 * does not eliminate the drift - nothing client-side can - it only makes it less bad.
 *
 * [measurement] is the exact result [largestFittingPointSize] already produced for the chosen size -
 * not recomputed here, so this adds no extra [StaticLayout] pass. Failure here
 * ([FreeTextAnnotation.setTextInsets] throwing) leaves the annotation's insets untouched rather than
 * breaking annotation loading.
 */
private fun applyMeanLineErrorTopInset(
    freeTextAnnotation: FreeTextAnnotation,
    usableBoxHeight: Float,
    maxHeight: Float,
    measurement: WrappedTextMeasurement,
) {
    val extraTopInset = meanLineErrorInset(measurement.lineCount, usableBoxHeight, maxHeight, measurement.height)
    if (extraTopInset <= 0f) return

    runCatching {
        val existing = freeTextAnnotation.textInsets
        freeTextAnnotation.textInsets = EdgeInsets(
            (existing?.top ?: 0f) + extraTopInset,
            existing?.left ?: 0f,
            existing?.bottom ?: 0f,
            existing?.right ?: 0f
        )
    }
}

/**
 * Constant downward shift equal to the mean of the per-line vertical error that accumulates when our
 * leading is tighter than web's: line `i` (0-indexed) sits `i * (webLeading - ourLeading)` above where
 * web renders it, so the error ranges from 0 (first line) to `(n-1) * (webLeading - ourLeading)` (last
 * line); its mean is half that. Expressed in terms of the measured block instead of an unknown leading
 * difference: the total slack `usableBoxHeight - measuredHeight` is `n` line-gaps' worth of that same
 * per-line difference, so the mean error is `((n - 1) / (2n)) * slack`.
 *
 * [maxHeight] is the same budget [largestFittingPointSize] measured [measuredHeight] against
 * (`usableBoxHeight` after [freeTextShrinkSafetyMarginFraction] is reserved) - the result is clamped to
 * that budget, not to the full [usableBoxHeight]. Clamping against [usableBoxHeight] instead would let
 * `measuredHeight + result` exceed [maxHeight] and eat into the very safety margin the shrink search
 * reserved to absorb divergence between Android's [StaticLayout] and PSPDFKit's own layout - reachable
 * whenever `measuredHeight > (1 - margin) * usableBoxHeight`, i.e. exactly the tight-fit case where
 * clipping is likeliest.
 *
 * Guarded so it can only ever hand back a value in `[0, slack]` that also satisfies
 * `measuredHeight + result <= maxHeight`: never applied for a single line (n < 2, mean error is 0
 * anyway), never applied when there is no slack to redistribute ([measuredHeight] >= [usableBoxHeight]),
 * and clamped to whichever of `slack` or the [maxHeight] budget is tighter. [lineCount] <= 0 also
 * short-circuits to 0, avoiding a divide-by-zero.
 */
internal fun meanLineErrorInset(lineCount: Int, usableBoxHeight: Float, maxHeight: Float, measuredHeight: Float): Float {
    if (lineCount < 2) return 0f
    if (measuredHeight >= usableBoxHeight) return 0f

    val slack = usableBoxHeight - measuredHeight
    val raw = ((lineCount - 1).toFloat() / (2f * lineCount)) * slack
    val maxHeightBudget = (maxHeight - measuredHeight).coerceAtLeast(0f)
    return raw.coerceIn(0f, minOf(slack, maxHeightBudget))
}

/** Pairs the chosen point size with the exact [WrappedTextMeasurement] it was measured at, so callers
 * that need the measurement (e.g. for [applyMeanLineErrorTopInset]) never have to measure again. */
internal data class FittingSizeResult(val pointSize: Float, val measurement: WrappedTextMeasurement)

/**
 * Largest size at most [requestedPointSize] whose measured height is `<= maxHeight`. [StaticLayout]
 * height only changes when the wrapped line count changes, so [measure] is effectively a step function
 * of point size: a plain bisection can converge inside a fitting step rather than at its top, leaving
 * several points of unused box height. A bounded binary search first finds a size known to fit, then
 * [climbToTopOfStep] runs a second, narrower bisection between that size and the first search's
 * tightest known non-fitting bound, to land at the top of that step. Both phases use fixed iteration
 * counts, never a `while(true)` loop, so a non-monotonic [measure] cannot hang this - it just yields a
 * size that isn't provably optimal (see the returned/fits-or-floor guarantee documented on the
 * non-monotonic unit test). Never exceeds [requestedPointSize]: web's parsed size is the intent, this
 * only ever shrinks it. Never returns less than [minPointSize], even if nothing fits - clipping a
 * pathological case beats rendering invisibly small text. Returns [requestedPointSize] unchanged,
 * measured exactly once, when it already fits.
 *
 * The returned [FittingSizeResult.measurement] is always the actual measurement taken at
 * [FittingSizeResult.pointSize] - it is never recomputed or approximated - so a caller can reuse it
 * directly instead of measuring the chosen size a second time.
 */
internal fun largestFittingPointSize(
    requestedPointSize: Float,
    minPointSize: Float,
    maxHeight: Float,
    measure: (pointSize: Float) -> WrappedTextMeasurement,
): FittingSizeResult {
    if (requestedPointSize <= minPointSize) {
        // Already at (or below) the floor: shrinking further is pointless, so there is nothing to
        // decide and nothing worth measuring for. lineCount=0 is a sentinel a caller's
        // meanLineErrorInset guard already treats as "no measurement, no inset".
        return FittingSizeResult(requestedPointSize, WrappedTextMeasurement(0f, 0))
    }

    val requestedMeasurement = measure(requestedPointSize)
    if (requestedMeasurement.height <= maxHeight) {
        return FittingSizeResult(requestedPointSize, requestedMeasurement)
    }

    var low = minPointSize
    var high = requestedPointSize
    var best = minPointSize
    var bestMeasurement: WrappedTextMeasurement? = null

    for (i in 0 until sizeSearchBisectionIterations) {
        if (high - low <= sizeSearchBisectionTolerance) break
        val mid = (low + high) / 2f
        val midMeasurement = measure(mid)
        if (midMeasurement.height <= maxHeight) {
            best = mid
            bestMeasurement = midMeasurement
            low = mid
        } else {
            high = mid
        }
    }

    // Nothing in the loop fit: fall back to the floor, measured once so the caller still gets a real
    // measurement paired with the size actually returned (documented floor behaviour above).
    val resolvedBestMeasurement = bestMeasurement ?: measure(minPointSize)
    return climbToTopOfStep(best, resolvedBestMeasurement, high.coerceAtMost(requestedPointSize), maxHeight, measure)
}

/**
 * Narrows from [best] (already known to fit, with [bestMeasurement] its exact measurement) up to
 * [ceiling] (the outer bisection's tightest known non-fitting bound, itself at most
 * `requestedPointSize`) using a second, independent bisection. This is what actually lands the result
 * at the top of a fitting step rather than wherever the outer bisection's coarser tolerance happened to
 * stop - replacing a fixed linear climb (which measured a step per candidate regardless of how close
 * it already was) with a search whose measurement count only grows with the log of the remaining range.
 * Bounded iteration count, no `while(true)`: a non-monotonic [measure] just ends the climb at its
 * current best, never loops.
 */
private fun climbToTopOfStep(
    best: Float,
    bestMeasurement: WrappedTextMeasurement,
    ceiling: Float,
    maxHeight: Float,
    measure: (pointSize: Float) -> WrappedTextMeasurement,
): FittingSizeResult {
    if (ceiling <= best) return FittingSizeResult(best, bestMeasurement)

    var low = best
    var lowMeasurement = bestMeasurement
    var high = ceiling

    for (i in 0 until sizeSearchClimbIterations) {
        if (high - low <= sizeSearchClimbTolerance) break
        val mid = (low + high) / 2f
        val midMeasurement = measure(mid)
        if (midMeasurement.height <= maxHeight) {
            low = mid
            lowMeasurement = midMeasurement
        } else {
            high = mid
        }
    }
    return FittingSizeResult(low, lowMeasurement)
}

internal data class WrappedTextMeasurement(val height: Float, val lineCount: Int)

/**
 * How [text] wraps at [pointSize] when laid out at [width]: both the resulting height and the line
 * count it wrapped into. Layout pixels stand in for PDF points: [largestFittingPointSize] only ever
 * compares this against another value produced the same way, so the units cancel out and the
 * pixel/point distinction doesn't matter.
 */
private fun measureWrappedText(text: String, width: Float, pointSize: Float): WrappedTextMeasurement {
    val paint = TextPaint().apply { textSize = pointSize }
    val layoutWidth = width.toInt().coerceAtLeast(1)
    val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, layoutWidth).build()
    return WrappedTextMeasurement(layout.height.toFloat(), layout.lineCount)
}

// Matches the size token anywhere and case-insensitively, since strings like "bold 38PT Verdana"
// occur. Unsigned, so a leading "-" can never yield a negative size. Group 2 is the rest of the
// string, which may contain spaces ("Times New Roman").
private val freeTextFontRegex = Regex("""(\d+(?:\.\d+)?)\s*pt\b(.*)$""", RegexOption.IGNORE_CASE)

private data class ParsedFont(val pointSize: Float, val family: String?)

private fun parseFont(font: String?): ParsedFont {
    val match = font?.let { freeTextFontRegex.find(it) }
    val parsedSize = match?.groupValues?.getOrNull(1)?.toFloatOrNull()
    val parsedFamily = match?.groupValues?.getOrNull(2)?.trim()?.takeIf { it.isNotBlank() }

    val pointSize = (parsedSize ?: defaultFreeTextPointSize).coerceIn(minFreeTextPointSize, maxFreeTextPointSize)
    return ParsedFont(pointSize, parsedFamily)
}

/**
 * Web's `font` is `"<size>pt <Family>"`, e.g. `"14pt Verdana"`. [FreeTextAnnotation.textSize] is in
 * PDF points, the same unit, so the parsed size maps across directly.
 */
internal fun getTextSizeFromFont(font: String?): Float = parseFont(font).pointSize

/** Null when `font` names no family, so callers leave PSPDFKit's default typeface in place. */
internal fun getFontFamilyFromFont(font: String?): String? = parseFont(font).family

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

fun convertListOfRectsToListOfListOfListOfFloats(rects: List<RectF>?): ArrayList<ArrayList<ArrayList<Float>>>? {
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

// Free Text Annotation font sizes, in PDF points. 14pt is web's smallest supported size, used both
// as the fallback for an unparseable `font` and as the default for free text created on mobile.
const val defaultFreeTextPointSize = 14f
private const val minFreeTextPointSize = 1f
private const val maxFreeTextPointSize = 200f

// Floor for shrink-to-fit (shrinkFreeTextToFitBox), distinct from minFreeTextPointSize above - that
// one only sanity-clamps a parsed `font` string and has nothing to do with fitting a box. 1pt text is
// visually indistinguishable from missing text, so shrinking that far just to avoid clipping trades
// one readability problem for a worse one. Below this floor we accept visible-but-clipped text over
// unreadably small text.
internal const val minShrinkPointSize = 6f

// Reserve a slice of the box height as headroom so a small divergence between Android's StaticLayout
// measurement and PSPDFKit's own text layout still lands under the box, instead of clipping anyway.
private const val freeTextShrinkSafetyMarginFraction = 0.03f

// largestFittingPointSize search parameters, all fixed so the search is bounded and deterministic.
// Two bisections in sequence - one to find any size that fits, one to climb to the top of that size's
// fitting step - replace the original single bisection + 16-step fixed linear climb, measuring far
// fewer times in the worst case while still landing at the top of the step.
private const val sizeSearchBisectionIterations = 10
private const val sizeSearchBisectionTolerance = 0.25f
private const val sizeSearchClimbIterations = 8
private const val sizeSearchClimbTolerance = 0.02f
