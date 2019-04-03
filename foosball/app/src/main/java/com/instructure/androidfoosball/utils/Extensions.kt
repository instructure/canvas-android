/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("unused")

package com.instructure.androidfoosball.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.support.design.widget.TextInputLayout
import android.support.v4.view.GestureDetectorCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.User
import com.instructure.androidfoosball.views.UserInitialsDrawable
import com.squareup.picasso.Picasso
import org.jetbrains.anko.displayMetrics
import org.jetbrains.anko.sdk21.listeners.onTouch
import java.util.*
import java.util.regex.Pattern
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


fun ImageView.setAvatar(user: User?, avatarSize: Int = 0) {
    val picasso = Picasso.with(context).apply { cancelRequest(this@setAvatar) }
    when {
        user == null -> setImageResource(R.drawable.sadpanda)
        user.avatar.isBlank() -> setImageDrawable(UserInitialsDrawable(user, avatarSize))
        else -> picasso.load(user.avatar).placeholder(R.drawable.sadpanda).error(R.drawable.sadpanda).into(this)
    }
}

fun ImageView.setAvatarUrl(url: String?) {
    val picasso = Picasso.with(context).apply { cancelRequest(this@setAvatarUrl) }
    when {
        url.isNullOrBlank() -> setImageResource(R.drawable.sadpanda)
        else -> picasso.load(url).placeholder(R.drawable.sadpanda).error(R.drawable.sadpanda).into(this)
    }
}

fun String?.elseIfBlank(alt: String): String = if (isNullOrBlank()) alt else this!!

fun Activity.shortToast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
fun Activity.shortToast(messageId: Int) = Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show()
fun Activity.longToast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
fun Activity.longToast(messageId: Int) = Toast.makeText(this, messageId, Toast.LENGTH_LONG).show()

fun TextInputLayout.validate(validator: (String) -> Boolean, errorMessage: String, afterValidation: (isValid: Boolean) -> Unit): Boolean {
    return if (validator(editText?.text.toString())) {
        error = ""
        afterValidation(true)
        true
    } else {
        error = errorMessage
        afterValidation(false)
        false
    }
}

fun TextInputLayout.onTextChanged(onChanged: (newText: String) -> Unit) {
    editText?.addTextChangedListener(object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
        override fun afterTextChanged(s: Editable?) { onChanged(s.toString()) }
    })
}

fun EditText.onTextChanged(onChanged: (newText: String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
        override fun afterTextChanged(s: Editable?) { onChanged(s.toString()) }
    })
}

fun TextInputLayout.validateOnTextChanged(validator: (String) -> Boolean, errorMessage: String, afterValidation: (newText: String, isValid: Boolean) -> Unit) {
    onTextChanged { newText -> validate(validator, errorMessage, { afterValidation(newText, it) }) }
}

var TextInputLayout.text: String
    get() = editText?.text.toString()
    set(value) {
        editText?.setText(value)
    }

fun <T : View> T.setVisible(isVisible: Boolean = true): T = apply {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun String.matches(pattern: Pattern) = pattern.matcher(this).matches()

/**
 * Calculates this user's win rate.
 * @param minGamesRequired The minimum number of games this user must have played to warrant a proper win rate
 * *
 * @return A float between 0 and 100, or -1 if the user has not played enough games to warrant a win rate
 */
fun User.getWinRate(minGamesRequired: Int) = when {
    wins + losses < minGamesRequired -> -1f
    wins == 0 -> 0f
    losses == 0 -> 100f
    else -> 100f * wins / (wins + losses)
}

fun List<User>.sortByWinRatio(minGamesRequired: Int)
        = sortedWith(compareBy({ -it.getWinRate(minGamesRequired) }, { -it.wins }, { it.losses } ))

fun List<User>.sortByFoosRanking() = sortedBy { -it.foosRanking }

/**
 * Clamps a color's brightness
 * @property maxBrightness A float between 0 and 1, where 1 is maximum possible brightness
 */
fun Int.clampToBrightness(maxBrightness: Float): Int {
    val hsv = FloatArray(3)
    Color.colorToHSV(this, hsv)
    hsv[2] = hsv[2].coerceAtMost(maxBrightness)
    return Color.HSVToColor(hsv)
}

/** A color generated from the user's name */
val User.avatarColor: Int
    get() = name.hashCode() or 0xFF000000.toInt()

/** The user's initials, or entire name if it's only one word */
val User.initials: String
    get() {
        if (name.isBlank()) return "?"
        val names = name.split(' ', limit = 3).filter { it.isNotBlank() }
        return if (names.size == 1) {
            names[0]
        } else {
            names.fold("") { initials, nextName -> initials + nextName[0].toUpperCase() }
        }
    }

class Unless<T>(val value: T, private val condition: (T) -> Boolean) {
    infix fun then(alternateValue: T) = if (condition(value)) alternateValue else value
}

infix fun <T> T.unless(condition: (T) -> Boolean) = Unless(this, condition)

class ValidatorChain(private val validators: MutableList<Validator<*>> = ArrayList()) {
    infix fun first(validator: Validator<*>) = then(validator)

    infix fun then(validator: Validator<*>): ValidatorChain {
        validators.add(validator)
        return this
    }

    infix fun finally(block: () -> Unit) {
        if (validators.all { it.validate() }) block()
    }
}

data class Validator<out T>(val field: T, private val errorText: String, private val condition: (T) -> Boolean, private val onFail: (String) -> Unit) {
    fun validate() = condition(field).apply { if (!this) onFail(errorText) }
}

fun TextInputLayout.validate(errorText: String, condition: (String) -> Boolean) = Validator(this, errorText, { condition(text) }, { error = errorText; requestFocus() })

fun <T> T.validate(errorText: String, condition: (T) -> Boolean, onFail: (String) -> Unit) = Validator(this, errorText, { condition(this) }, onFail)

fun validationBlock(block: ValidatorChain.() -> Unit) { ValidatorChain().block() }

/** Returns a list of all children (direct descendants) in this ViewGroup */
val ViewGroup.children: List<View> get() = (0 until childCount).map { getChildAt(it) }

/** Returns a list of all children of a specific type in this ViewGroup */
@Suppress("UNCHECKED_CAST")
inline fun <reified T : View> ViewGroup.children() = children.filter { it is T } as List<T>

/** Returns a list of all descendants in this ViewGroup */
val ViewGroup.descendants: List<View> get() = children + children<ViewGroup>().flatMap { it.descendants }

/** Returns a list of all descendants of a specific type in this ViewGroup */
@Suppress("UNCHECKED_CAST")
inline fun <reified T : View> ViewGroup.descendants() = descendants.filter { it is T } as List<T>

/** Performs the provided function on all of this ViewGroup's descendants of a specific type */
inline fun <reified T : View> ViewGroup.modifyDescendants(mod: (T) -> Unit) = descendants<T>().forEach { mod(it) }

/** View binding, for scopes not covered by Kotlin's Android Extensions */
fun <V : View> Dialog.bind(id: Int): Binder<Dialog, V> = Binder { it.findViewById(id) }

fun <V : View> ViewGroup.bind(id: Int): Binder<ViewGroup, V> = Binder { it.findViewById(id) }

@Suppress("UNCHECKED_CAST")
class Binder<in T, out V : View>(private val finder: (T) -> View?) : ReadOnlyProperty<T, V> {
    private var cachedView: V? = null
    override fun getValue(thisRef: T, property: KProperty<*>): V {
        if (cachedView == null) {
            val v = finder(thisRef) ?: throw RuntimeException("Unable to bind ${property.name}; findViewById returned null.")
            cachedView = v as V
        }
        return cachedView!!
    }
}

fun Int.toDp(context: Context) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.displayMetrics)
fun Float.toDp(context: Context) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, context.displayMetrics)

fun View.onDoubleTap(listener: () -> Unit) {
    val detector = GestureDetectorCompat(context, object : GestureDetector.OnGestureListener {
        override fun onShowPress(p0: MotionEvent?) = Unit
        override fun onSingleTapUp(p0: MotionEvent?): Boolean = true
        override fun onDown(p0: MotionEvent?): Boolean = true
        override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean = true
        override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean = true
        override fun onLongPress(p0: MotionEvent?) = Unit
    })

    detector.setOnDoubleTapListener(object : GestureDetector.OnDoubleTapListener {
        override fun onDoubleTapEvent(p0: MotionEvent?) = true
        override fun onSingleTapConfirmed(p0: MotionEvent?) = true
        override fun onDoubleTap(p0: MotionEvent?): Boolean {
            listener()
            return true
        }
    })

    this.onTouch { _, event -> detector.onTouchEvent(event) }
}

fun <E> Iterable<E>.disjunctiveUnion(other: Iterable<E>): Set<E> = (this - other).union(other - this)
