/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.canvas.espresso

import android.os.SystemClock
import androidx.test.espresso.web.model.Atom
import androidx.test.espresso.web.model.ElementReference
import androidx.test.espresso.web.sugar.Web

/**
 * Like withElement(), except that you can repeat the attempt up to [secsToRepeat] times,
 * with a one-second delay between attempts.
 */
fun<E> Web.WebInteraction<E>.withElementRepeat(element: Atom<ElementReference>, secsToRepeat: Int = 10) : Web.WebInteraction<E> {

    // Repeat the check once a second, for as many seconds as the caller specified
    repeat(secsToRepeat) {
        try {
            return this.withElement(element)
        }
        catch(re: RuntimeException) {
            SystemClock.sleep(1000)
        }
    }

    // If we reach this point, we've failed.  Run the check once more without the try/catch
    // to surface the proper error.
    return this.withElement(element)
}