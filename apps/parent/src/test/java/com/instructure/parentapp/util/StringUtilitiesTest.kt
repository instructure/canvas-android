/*
 * Copyright (C) 2016 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.parentapp.util

import org.junit.Test

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class StringUtilitiesTest {

    //region isEmpty
    @Test
    fun isEmpty_TestEmptyNoSpace() {
        assertTrue(StringUtilities.isEmpty(""))
    }

    @Test
    fun isEmpty_TestEmptySpace() {
        assertFalse(StringUtilities.isEmpty(" "))
    }

    @Test
    fun isEmpty_TestNotEmptySingle() {
        assertFalse(StringUtilities.isEmpty("Not Empty"))
    }

    @Test
    fun isEmpty_TestNotEmptyVarArgs() {
        assertFalse(StringUtilities.isEmpty("Not", "Empty"))
    }

    @Test
    fun isEmpty_TestNoParams() {
        assertFalse(StringUtilities.isEmpty())
    }

    @Test
    fun isEmpty_TestNullParam() {
        assertTrue(StringUtilities.isEmpty((null as String?)))
    }
    //endregion

    //region isStringNumeric
    @Test
    fun isStringNumeric_TestEmptySpaceString() {
        assertFalse(StringUtilities.isStringNumeric(" "))
    }

    @Test
    fun isStringNumeric_TestEmptyString() {
        assertFalse(StringUtilities.isStringNumeric(""))
    }

    @Test
    fun isStringNumeric_TestNullString() {
        assertFalse(StringUtilities.isStringNumeric(null))
    }

    @Test
    fun isStringNumeric_TestAlphaNumeric() {
        assertFalse(StringUtilities.isStringNumeric("123ABC"))
    }

    @Test
    fun isStringNumeric_TestNumeric() {
        assertTrue(StringUtilities.isStringNumeric("234"))
    }

    @Test
    fun isStringNumeric_TestMinusSignFront() {
        assertTrue(StringUtilities.isStringNumeric("-234"))
    }

    @Test
    fun isStringNumeric_TestMinusSignBack() {
        assertFalse(StringUtilities.isStringNumeric("234-"))
    }

    @Test
    fun isStringNumeric_TestDecimal() {
        assertTrue(StringUtilities.isStringNumeric("23.123"))
    }
    //endregion

    //region simplifyHTML
    @Test
    fun simplifyHTML_TestNullString() {
        assertEquals("", StringUtilities.simplifyHTML(null))
    }

    @Test
    fun simplifyHTML_TestEmptyString() {
        assertEquals("", StringUtilities.simplifyHTML(" "))
    }

    @Test
    fun simplifyHTML_TestObjChar() {
        assertEquals("Should be", StringUtilities.simplifyHTML("Should\uFFFCbe"))
    }

    @Test
    fun simplifyHTML_TestRegularString() {
        assertEquals("http://whatever", StringUtilities.simplifyHTML("http://whatever"))
    }
    //endregion
}
