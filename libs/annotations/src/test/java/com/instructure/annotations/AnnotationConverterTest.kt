/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

import org.junit.Assert
import org.junit.Test

class AnnotationConverterTest : Assert() {

    //region getTextSizeFromFont - supported web point sizes
    @Test
    fun testGetTextSizeFromFont_14pt() {
        assertEquals(14f, getTextSizeFromFont("14pt"))
    }

    @Test
    fun testGetTextSizeFromFont_22pt() {
        assertEquals(22f, getTextSizeFromFont("22pt"))
    }

    @Test
    fun testGetTextSizeFromFont_38pt() {
        assertEquals(38f, getTextSizeFromFont("38pt"))
    }
    //endregion

    @Test
    fun testGetTextSizeFromFont_sizeWebDoesNotCurrentlyOffer_isParsedGenerically() {
        assertEquals(11f, getTextSizeFromFont("11pt"))
    }

    @Test
    fun testGetTextSizeFromFont_fullFontStringWithFamily_parsesSize() {
        assertEquals(14f, getTextSizeFromFont("14pt Verdana"))
    }

    //region getTextSizeFromFont - unanchored / case-insensitive matching
    @Test
    fun testGetTextSizeFromFont_multiWordFamily_doesNotStopAtFirstSpace() {
        assertEquals(22f, getTextSizeFromFont("22pt Times New Roman"))
    }

    @Test
    fun testGetTextSizeFromFont_leadingWeightKeyword_isNotAnchoredAtStart() {
        assertEquals(38f, getTextSizeFromFont("bold 38pt Verdana"))
    }

    @Test
    fun testGetTextSizeFromFont_uppercasePtToken_isCaseInsensitive() {
        assertEquals(38f, getTextSizeFromFont("38PT Verdana"))
    }

    @Test
    fun testGetTextSizeFromFont_leadingUnrelatedNumber_picksNumberAttachedToPtToken() {
        // "2" is not followed by "pt" (there's a space then "14pt"), so it must not be picked up
        // as the size - only the number actually attached to the "pt" token counts.
        assertEquals(14f, getTextSizeFromFont("Arial 2 14pt"))
    }

    @Test
    fun testGetTextSizeFromFont_negativeSign_isNeverNegative() {
        assertEquals(14f, getTextSizeFromFont("-14pt X"))
    }
    //endregion

    //region getFontFamilyFromFont
    @Test
    fun testGetFontFamilyFromFont_realisticFontString_parsesFamily() {
        assertEquals("Verdana", getFontFamilyFromFont("14pt Verdana"))
    }

    @Test
    fun testGetFontFamilyFromFont_multiWordFamily_parsesWholeFamily() {
        assertEquals("Times New Roman", getFontFamilyFromFont("22pt Times New Roman"))
    }

    @Test
    fun testGetFontFamilyFromFont_leadingWeightKeyword_parsesFamilyAfterPtToken() {
        assertEquals("Verdana", getFontFamilyFromFont("bold 38pt Verdana"))
    }

    @Test
    fun testGetFontFamilyFromFont_uppercasePtToken_parsesFamily() {
        assertEquals("Verdana", getFontFamilyFromFont("38PT Verdana"))
    }

    @Test
    fun testGetFontFamilyFromFont_sizeOnly_returnsNull() {
        assertNull(getFontFamilyFromFont("14pt"))
    }

    @Test
    fun testGetFontFamilyFromFont_null_returnsNull() {
        assertNull(getFontFamilyFromFont(null))
    }

    @Test
    fun testGetFontFamilyFromFont_empty_returnsNull() {
        assertNull(getFontFamilyFromFont(""))
    }

    @Test
    fun testGetFontFamilyFromFont_ptWithNoSize_returnsNull() {
        assertNull(getFontFamilyFromFont("pt"))
    }

    @Test
    fun testGetFontFamilyFromFont_familyWithNoSize_returnsNull() {
        assertNull(getFontFamilyFromFont("Verdana"))
    }

    @Test
    fun testGetFontFamilyFromFont_garbage_returnsNull() {
        assertNull(getFontFamilyFromFont("not-a-font-string"))
    }
    //endregion

    //region fallback
    @Test
    fun testGetTextSizeFromFont_null_returnsDocumentedFallback() {
        assertEquals(defaultFreeTextPointSize, getTextSizeFromFont(null))
    }

    @Test
    fun testGetTextSizeFromFont_empty_returnsDocumentedFallback() {
        assertEquals(defaultFreeTextPointSize, getTextSizeFromFont(""))
    }

    @Test
    fun testGetTextSizeFromFont_ptWithNoSize_returnsDocumentedFallback() {
        assertEquals(defaultFreeTextPointSize, getTextSizeFromFont("pt"))
    }

    @Test
    fun testGetTextSizeFromFont_familyWithNoSize_returnsDocumentedFallback() {
        assertEquals(defaultFreeTextPointSize, getTextSizeFromFont("Verdana"))
    }

    @Test
    fun testGetTextSizeFromFont_garbage_returnsDocumentedFallback() {
        assertEquals(defaultFreeTextPointSize, getTextSizeFromFont("not-a-font-string"))
    }
    //endregion

    //region clamping
    @Test
    fun testGetTextSizeFromFont_absurdlyLargeValue_isClampedToMax() {
        assertEquals(200f, getTextSizeFromFont("99999pt X"))
    }

    @Test
    fun testGetTextSizeFromFont_zeroValue_isClampedToMin() {
        assertEquals(1f, getTextSizeFromFont("0pt X"))
    }
    //endregion

    //region largestFittingPointSize
    @Test
    fun testLargestFittingPointSize_alreadyFits_returnsRequestedSizeUnchanged() {
        var measureCalls = 0
        val result = largestFittingPointSize(
            requestedPointSize = 14f,
            minPointSize = 1f,
            maxHeight = 100f,
        ) { measureCalls++; WrappedTextMeasurement(10f, 3) }

        assertEquals(14f, result.pointSize)
        // No extra measurement work once the requested size already fits.
        assertEquals(1, measureCalls)
    }

    @Test
    fun testLargestFittingPointSize_needsShrinking_convergesNearOptimalSize() {
        // Linear relationship: height = pointSize * 5. maxHeight = 40 => optimal size is 8f.
        val result = largestFittingPointSize(
            requestedPointSize = 14f,
            minPointSize = 1f,
            maxHeight = 40f,
        ) { pointSize -> WrappedTextMeasurement(pointSize * 5f, 1) }

        assertTrue("expected result near 8f but was ${result.pointSize}", Math.abs(result.pointSize - 8f) <= 0.3f)
        assertTrue(result.pointSize * 5f <= 40f)
        // The returned measurement must be the one actually taken at the returned size, not stale.
        assertEquals(result.pointSize * 5f, result.measurement.height)
    }

    @Test
    fun testLargestFittingPointSize_nothingFits_returnsMinPointSizeFloor() {
        val result = largestFittingPointSize(
            requestedPointSize = 14f,
            minPointSize = 1f,
            maxHeight = 10f,
        ) { WrappedTextMeasurement(1000f, 5) }

        assertEquals(1f, result.pointSize)
    }

    @Test
    fun testLargestFittingPointSize_nonMonotonicMeasureHeight_fitsOrFallsBackToFloor() {
        // Deliberately non-monotonic: whether a size "fits" flips on its integer part, not on its
        // magnitude, so a bisection has no monotonic signal to follow. requestedPointSize (20) itself
        // reports a non-fitting height here, so the search genuinely has to bisect rather than
        // returning immediately - unlike the old version of this test, whose fake measure happened to
        // report a fit for the untouched requested size, short-circuiting before any bisection ran.
        //
        // Termination and bounds are already structurally guaranteed by the fixed iteration counts and
        // the requested/min clamps - asserting "result in [min, requested]" would hold even for a
        // broken implementation, so it proves nothing. What the algorithm actually promises for an
        // arbitrary non-monotonic measure is narrower: the returned size either demonstrably fits (its
        // own measured height is <= maxHeight), or the search found nothing that fits anywhere it
        // looked and transparently fell back to the documented floor (minPointSize). It does NOT
        // promise the globally best-fitting size for a non-monotonic measure.
        fun measure(pointSize: Float) =
            WrappedTextMeasurement(height = if (pointSize.toInt() % 2 == 0) 50f else 5f, lineCount = 1)

        val minPointSize = 1f
        val maxHeight = 15f
        val result = largestFittingPointSize(
            requestedPointSize = 20f,
            minPointSize = minPointSize,
            maxHeight = maxHeight,
        ) { measure(it) }

        val resultFits = measure(result.pointSize).height <= maxHeight
        assertTrue(
            "expected the result (${result.pointSize}) to either fit or fall back to the floor ($minPointSize)",
            resultFits || result.pointSize == minPointSize
        )
    }

    @Test
    fun testLargestFittingPointSize_neverExceedsRequestedPointSize() {
        val result = largestFittingPointSize(
            requestedPointSize = 14f,
            minPointSize = 1f,
            maxHeight = 5f,
        ) { pointSize -> WrappedTextMeasurement(pointSize * 100f, 1) }

        assertTrue(result.pointSize <= 14f)
    }

    @Test
    fun testLargestFittingPointSize_requestedAlreadyAtMin_returnsRequestedWithoutMeasuring() {
        var measureCalls = 0
        val result = largestFittingPointSize(
            requestedPointSize = 1f,
            minPointSize = 1f,
            maxHeight = 0.01f,
        ) { measureCalls++; WrappedTextMeasurement(1000f, 1) }

        assertEquals(1f, result.pointSize)
        assertEquals(0, measureCalls)
    }

    @Test
    fun testLargestFittingPointSize_neverReturnsBelowTheActualShrinkFloorUsedInProduction() {
        // Fix 3 (MBL-20131): shrinkFreeTextToFitBox passes minShrinkPointSize, not the parse-only
        // minFreeTextPointSize, as the floor. Pin the search's floor guarantee to that actual constant
        // so a future edit that widens the gap between the two constants would be caught here.
        val result = largestFittingPointSize(
            requestedPointSize = 14f,
            minPointSize = minShrinkPointSize,
            maxHeight = 10f,
        ) { WrappedTextMeasurement(1000f, 5) }

        assertEquals(minShrinkPointSize, result.pointSize)
    }

    // StaticLayout's real height is quantised by wrapped line count, so it behaves like these two
    // fakes: flat (or slowly rising) while the line count holds, then a large jump when it increases.
    // A bisection alone can land anywhere inside a fitting plateau; these tests pin that the result
    // sits at the plateau's TOP (closest to requestedPointSize), not merely somewhere under maxHeight.

    @Test
    fun testLargestFittingPointSize_stepFunctionMeasureHeight_convergesAtTopOfLineCountPlateau() {
        // height = lines(size) * size * factor, mirroring "ceil(lines(size)) * size * factor": the
        // line count only changes at fixed thresholds, so height jumps sharply there instead of
        // rising smoothly. Within the lines=5 plateau (9.6, 12], height = 6 * size, crossing
        // maxHeight = 60 exactly at size = 10 - the true top of the fitting range below 14f.
        fun lines(size: Float): Int = when {
            size <= 9.6f -> 4
            size <= 12f -> 5
            else -> 6
        }
        fun stepHeight(size: Float): Float = lines(size) * size * 1.2f

        val result = largestFittingPointSize(
            requestedPointSize = 14f,
            minPointSize = 1f,
            maxHeight = 60f,
        ) { size -> WrappedTextMeasurement(stepHeight(size), lines(size)) }

        assertTrue("expected result near the top of the step (~10f) but was ${result.pointSize}", Math.abs(result.pointSize - 10f) <= 0.1f)
        assertTrue(stepHeight(result.pointSize) <= 60f)
        assertTrue(result.pointSize <= 14f)
    }

    @Test
    fun testLargestFittingPointSize_flatPlateauMeasureHeight_doesNotSettleInsideThePlateau() {
        // Numbers taken from the on-device defect report (MBL-20131): height reads 45 across a whole
        // range of sizes (a flat plateau, since ceil'd line count doesn't change), then jumps to 67
        // once the size crosses 12.5f. maxHeight = 58.85 (the box height minus the safety margin).
        // Height alone can't distinguish "top of plateau" from "stuck early in the plateau" here -
        // both read 45 - so the assertion is on the returned SIZE, which the old bug got badly wrong
        // (it landed around 11.97, far from the true top of 12.5).
        fun stepHeight(size: Float): Float = when {
            size <= 8f -> 30f
            size <= 12.5f -> 45f
            else -> 67f
        }

        val result = largestFittingPointSize(
            requestedPointSize = 14f,
            minPointSize = 1f,
            maxHeight = 58.85f,
        ) { size -> WrappedTextMeasurement(stepHeight(size), 4) }

        assertTrue("expected result near the top of the plateau (~12.5f) but was ${result.pointSize}", Math.abs(result.pointSize - 12.5f) <= 0.1f)
        assertTrue(stepHeight(result.pointSize) <= 58.85f)
        assertTrue(result.pointSize <= 14f)
    }
    //endregion

    //region meanLineErrorInset
    @Test
    fun testMeanLineErrorInset_realDeviceCase_matchesMeasuredMeanError() {
        // Measured on device for MBL-20131: usableBoxHeight=60.669983, measured=45.0, lines=4.
        // web leading 15.17 vs our leading 11.25 => mean per-line error ~5.88. maxHeight (the search's
        // own budget, usableBoxHeight after the 3% shrink safety margin) is ~58.85 here - well above
        // measuredHeight + 5.88, so the fix-1 clamp does not engage in this real-world case.
        val result = meanLineErrorInset(
            lineCount = 4,
            usableBoxHeight = 60.669983f,
            maxHeight = 58.85f,
            measuredHeight = 45.0f
        )
        assertTrue("expected ~5.88 but was $result", Math.abs(result - 5.88f) <= 0.01f)
    }

    @Test
    fun testMeanLineErrorInset_rawValueWouldExceedMaxHeightBudget_isClampedToMaxHeight() {
        // Fix 1 (MBL-20131): the inset must never push measuredHeight + inset past maxHeight, the same
        // budget largestFittingPointSize measured against - clamping against usableBoxHeight alone
        // would let this eat into the shrink safety margin. Numbers chosen so the raw (uncapped)
        // mean-error value clearly exceeds the maxHeight budget: slack = 100 - 80 = 20,
        // raw = (3/4)/2 * 20 = 7.5, but the maxHeight budget is only 82 - 80 = 2.
        val result = meanLineErrorInset(
            lineCount = 4,
            usableBoxHeight = 100f,
            maxHeight = 82f,
            measuredHeight = 80f
        )

        assertEquals(2f, result, 0.001f)
        assertTrue(80f + result <= 82f)
    }

    @Test
    fun testMeanLineErrorInset_singleLine_returnsZero() {
        assertEquals(0f, meanLineErrorInset(lineCount = 1, usableBoxHeight = 60f, maxHeight = 58f, measuredHeight = 20f))
    }

    @Test
    fun testMeanLineErrorInset_zeroLines_returnsZeroWithoutDivideByZero() {
        assertEquals(0f, meanLineErrorInset(lineCount = 0, usableBoxHeight = 60f, maxHeight = 58f, measuredHeight = 20f))
    }

    @Test
    fun testMeanLineErrorInset_measuredEqualsUsable_returnsZero_noSlackToRedistribute() {
        assertEquals(0f, meanLineErrorInset(lineCount = 4, usableBoxHeight = 60f, maxHeight = 58f, measuredHeight = 60f))
    }

    @Test
    fun testMeanLineErrorInset_measuredExceedsUsable_returnsZero_neverNegative() {
        assertEquals(0f, meanLineErrorInset(lineCount = 4, usableBoxHeight = 60f, maxHeight = 58f, measuredHeight = 70f))
    }

    @Test
    fun testMeanLineErrorInset_measuredExceedsMaxHeight_returnsZero_neverNegative() {
        // measuredHeight already exceeds maxHeight (the pathological "nothing fit" case) - the
        // maxHeight budget clamps to 0 rather than going negative.
        assertEquals(0f, meanLineErrorInset(lineCount = 4, usableBoxHeight = 100f, maxHeight = 50f, measuredHeight = 70f))
    }

    @Test
    fun testMeanLineErrorInset_clamp_neverPushesMeasuredHeightPastUsableBoxHeight() {
        // The formula's factor (n-1)/(2n) is mathematically bounded below 0.5 for any n >= 2, so a
        // correctly-derived result can never exceed half the slack - but the clamp is required
        // defensively regardless. maxHeight == usableBoxHeight here so the fix-1 clamp contributes no
        // extra restriction, isolating the original (n-1)/(2n) bound this test targets. Exercise it at
        // an extreme line count, where the factor is closest to its 0.5 ceiling, and assert the
        // invariant the clamp exists to guarantee still holds.
        val usableBoxHeight = 60.669983f
        val measuredHeight = 45.0f
        val result = meanLineErrorInset(
            lineCount = 1_000_000,
            usableBoxHeight = usableBoxHeight,
            maxHeight = usableBoxHeight,
            measuredHeight = measuredHeight
        )

        assertTrue(result >= 0f)
        assertTrue(measuredHeight + result <= usableBoxHeight)
    }
    //endregion
}
