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
 *
 */
@file:Suppress("unused")

package com.instructure.canvasapi2.utils

import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import java.io.File
import java.io.InputStream
import java.lang.reflect.Field
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/** Returns true if this string is non-null and non-blank, otherwise returns false */
@OptIn(ExperimentalContracts::class)
@Suppress("NOTHING_TO_INLINE")
inline fun String?.isValid(): Boolean {
    contract {
        returns(true) implies (this@isValid != null)
    }
    return !isNullOrBlank()
}

/** Returns this string if is it non-null and non-blank, otherwise returns null */
@Suppress("NOTHING_TO_INLINE")
inline fun String?.validOrNull(): String? = this?.takeIf { it.isValid() }

/**
 * Replaces the first instance of [oldValue] - found after the first instance of [delimiter] - with [newValue].
 * If [delimiter] is not found, this will find the first instance of [oldValue] and replace it with [newValue].
 * If [oldValue] is not found, this will return the original, unmodified String.
 */
fun String.replaceFirstAfter(delimiter: String, oldValue: String, newValue: String): String {
    val delimiterIdx = indexOf(delimiter)
    val valueIdx = indexOf(oldValue, delimiterIdx)
    return if (valueIdx== -1) this else replaceRange(valueIdx, valueIdx + oldValue.length, newValue)
}

inline fun <T> List<T>.intersectBy(other: List<T>, unique: (T) -> Any): List<T> {
    val list = this.toMutableList()
    val iterator = list.iterator()
    while (iterator.hasNext()) {
        val compareValue = unique(iterator.next())
        if (other.indexOfFirst { compareValue == unique(it) } == -1) {
            iterator.remove()
        }
    }
    return list
}

/**
 * Returns a patched version of this list using elements from the provided [patch] list, retaining the original order.
 * All elements in the original list which match an element in the [patch] list - where a match is determined by the
 * provided [matchValue] - will be replaced with that element. Elements in the [patch] list which do not match
 * any elements in the original list will not be included.
 */
fun <T> List<T>.patchedBy(patch: List<T>, matchValue: (T) -> Any): List<T> {
    val patchMap = patch.associateBy(matchValue)
    return map { patchMap[matchValue(it)] ?: it }
}

inline fun <T> tryOrNull(block: () -> T?): T? {
    return try {
        block()
    } catch (ignore: Throwable) {
        null
    }
}

/**
 * Returns the first element matching the given [predicate], or `null` if no such element was found.
 * @param [predicate] function that takes the current element and previous element, and returns a boolean. The
 * previous element will always be null for the first element in the list.
 */
fun <E> List<E>.findWithPrevious(predicate: (previous: E?, it: E) -> Boolean): E? {
    var previous: E? = null
    forEach {
        if (predicate(previous, it)) return it
        previous = it
    }
    return null
}

val Locale.isRtl get() = TextUtilsCompat.getLayoutDirectionFromLocale(this) == ViewCompat.LAYOUT_DIRECTION_RTL

/** Creates a range surrounding this number */
fun Int.rangeWithin(scope: Int): IntRange = (this - scope)..(this + scope)

/**
 * Ensures the starting value of this range is not less than the specified [minValue].
 *
 * @return This range if the starting value is greater than or equal to [minValue], otherwise a
 * new range of equal length with a starting value of [minValue].
 */
fun IntRange.coerceAtLeast(minValue: Int) = if (start > minValue) this else minValue..(minValue + endInclusive - start)

/**
 * Copies this [InputStream] into the specified [File]
 */
fun InputStream.copyTo(file: File) = use { input -> file.outputStream().use { output -> input.copyTo(output) } }

/**
 * A property whose sole purpose is to force *when* expressions to be exhaustive. This is useful
 * in *when* expressions that switch on sealed classes, do not require a return value, and must
 * guarantee that all current and future branches are covered.
 *
 * For example, imagine a sealed class **Fruit** with inheritors **Apple** and **Orange**. Let's
 * say that the following code must accept any **Fruit** and prepare it for use in a fruit salad by
 * processing it in a manner unique to that inheritor - i.e. **Apples** must be sliced and
 * **Oranges** must be peeled:
 *
 * ```
 *
 *     val fruit: Fruit = ...
 *
 *     when(fruit) {
 *         is Apple -> AppleSlicer.slice(fruit)
 *         is Orange -> OrangePeeler.peel(fruit)
 *     }
 * ```
 *
 * Imagine this code is tucked away in a remote corner of the project and is quickly forgotten. What
 * happens a few months later when we add a new inheritor, **Banana**, but neglect to update this
 * code? In the best case all the **Bananas** will go into the fruit salad whole and unwashed,
 * hopefully giving our guests food poisoning. In the worst case it tries to add a null **Banana**
 * to the salad, causing a tear in the fabric of time and space which ripples out into the infinite
 * void, destroying everything in its wake and coming to an end only when the entire universe has
 * been obliterated by an unstoppable cascade of [NullPointerException]s.
 *
 * By adding `.exhaustive` to the end of the *when* expression the code will no longer compile
 * unless all branches have been exhausted:
 *
 * ```
 *
 *     val fruit: Fruit = ...
 *
 *     when(fruit) { // COMPILER ERROR
 *         is Apple -> AppleSlicer.slice(fruit)
 *         is Orange -> OrangePeeler.peel(fruit)
 *     }.exhaustive
 * ```
 *
 * This allows us to fail fast when adding new features and reduces the likelihood of introducing
 * additional bugs during future development.
 */
@Suppress("unused")
val Any?.exhaustive get() = Unit

/**
 * Convenience delegate for accessing a private field via reflection
 */
class ReflectField<T>(private val fieldName: String, private val declaringClass: Class<*>) : ReadWriteProperty<Any, T> {

    lateinit var field: Field

    operator fun provideDelegate(thisRef: Any, prop: KProperty<*>): ReadWriteProperty<Any, T> {
        field = declaringClass.getDeclaredField(fieldName)
        field.isAccessible = true
        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return field.get(thisRef) as T
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        field.set(thisRef, value)
    }

}
