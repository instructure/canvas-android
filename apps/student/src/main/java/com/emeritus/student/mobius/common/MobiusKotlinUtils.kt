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
package com.emeritus.student.mobius.common

import android.content.Context
import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import kotlin.reflect.KFunction2

fun <I, J, O> Connectable<I, O>.contraMap(
    mapper: KFunction2<J, Context, I>,
    context: Context,
    isDisabledForTesting: Boolean = false
): Connectable<J, O> {
    return Connectable { output ->
        val delegateConnection = connect(output)
        object : Connection<J> {
            var lastValue: I? = null

            override fun accept(value: J) {
                if (isDisabledForTesting) return
                val mappedValue: I = mapper(value, context)
                // Only push value if it has changed (prevents duplicate renders)
                if (mappedValue != lastValue) {
                    lastValue = mappedValue
                    delegateConnection.accept(mappedValue)
                }
            }

            override fun dispose() = delegateConnection.dispose()
        }
    }
}
