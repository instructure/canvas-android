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
package com.instructure.pandautils.utils

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.CanvasColor
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.BooleanPref
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.GsonMapPref
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.PrefManager
import com.instructure.canvasapi2.utils.weave.resumeSafely
import com.instructure.pandautils.R
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Response
import kotlin.math.absoluteValue

private const val PREFERENCE_FILE_NAME = "color_keeper_prefs"

@Suppress("unused")
object ColorKeeper : PrefManager(PREFERENCE_FILE_NAME) {

    /** The default color **/
    @JvmStatic var defaultColor: Int = 0

    private var cachedThemedColors: Map<String, ThemedColor> by GsonMapPref(String::class.java, ThemedColor::class.java)

    /** Whether or not colors have been synced from the API before **/
    var previouslySynced by BooleanPref()

    var darkTheme = false

    /**
     * Gets the themed color associated with the given [CanvasContext] if it exists, otherwise generates a new color
     * This should be used directly  only in special cases where we need a [ThemedColor] object instead of a specific color.
     * To get course color use [CanvasContext.color]
     * Only direct usage is in widgets, because there we handle light/dark theme differently
     * **/
    fun getOrGenerateColor(canvasContext: CanvasContext?): ThemedColor {
        return when (canvasContext) {
            is Course -> cachedThemedColors.getOrElse(canvasContext.contextId) { generateColor(canvasContext) }
            is Group -> {
                cachedThemedColors.getOrElse(canvasContext.contextId) {
                    val courseContextId = CanvasContext.makeContextId(CanvasContext.Type.COURSE, canvasContext.courseId)
                    val groupDefaultColor = ContextCompat.getColor(ContextKeeper.appContext, R.color.backgroundDark)
                    cachedThemedColors.getOrElse(courseContextId) { ThemedColor(groupDefaultColor) }
                }
            }
            else -> ThemedColor(ThemePrefs.primaryColor) // defaultColor is already themed so we don't need 3 different colors
        }
    }

    /**
     * Gets the themed color associated with the given [User] if it exists, otherwise generates a new color
     * This should be used directly  only in special cases where we need a [ThemedColor] object instead of a specific color.
     * To get user color use [User.studentColor]
     * **/
    fun getOrGenerateUserColor(user: User?): ThemedColor {
       return if (user == null) {
           ThemedColor(ThemePrefs.primaryColor)
       } else {
           cachedThemedColors.getOrElse(user.contextId) { generateUserColor(user) }
       }
    }

    val userColors = listOf(
        R.color.studentBlue,
        R.color.studentPurple,
        R.color.studentPink,
        R.color.studentRed,
        R.color.studentOrange,
        R.color.studentGreen
    )

    val courseColors = listOf(
        R.color.courseColor1light,
        R.color.courseColor2light,
        R.color.courseColor3light,
        R.color.courseColor4light,
        R.color.courseColor5light,
        R.color.courseColor6light,
        R.color.courseColor7light,
        R.color.courseColor8light,
        R.color.courseColor9light,
        R.color.courseColor10light,
        R.color.courseColor11light,
        R.color.courseColor12light
    )

    private fun generateUserColor(user: User): ThemedColor {
        val index = user.id.absoluteValue % userColors.size
        val color = ContextCompat.getColor(ContextKeeper.appContext, userColors[index.toInt()])
        val themedColor = createThemedColor(color)
        cachedThemedColors += user.contextId to themedColor
        return themedColor
    }

    /** Adds all colors in the given [CanvasColor] object to the color cache **/
    @JvmStatic fun addToCache(canvasColor: CanvasColor?) {
        canvasColor?.colors?.let { colors ->
            cachedThemedColors += colors.mapValues {
                val color = parseColor(it.value)
                createThemedColor(color)
            }
        }
    }

    /** Associates a new color with a contextId **/
    @JvmStatic fun addToCache(contextId: String, @ColorInt color: Int) {
        cachedThemedColors += contextId to createThemedColor(color)
    }

    /** Associates a new color with a contextId **/
    fun addToCache(contextId: String, colorCode: String) {
        val color = parseColor(colorCode)
        cachedThemedColors += contextId to createThemedColor(color)
    }

    fun createThemedColor(@ColorInt color: Int): ThemedColor {
        // There are some custom colors that we can map to a specific dark variant. For others we use our accessibility algorithm.
        val entry = lightDarkColorMap.entries.find { it.key == color }
        if (entry != null) {
            return ThemedColor(entry.key, entry.value)
        }

        val light = ColorUtils.correctContrastForText(color, ContextKeeper.appContext.getColor(R.color.white))
        val dark = ColorUtils.correctContrastForText(color, ContextKeeper.appContext.getColor(R.color.textOnColorDark))

        return ThemedColor(light, dark)
    }

    /**
     * Generates a colored drawable
     * @param context An Android Context
     * @param resource The resource ID of the drawable to be colored
     * @param color The color that will be used to tint the drawable
     * @return The colored drawable
     */
    @JvmStatic fun getColoredDrawable(context: Context, @DrawableRes resource: Int, @ColorInt color: Int): Drawable
            = ContextCompat
            .getDrawable(context, resource)!!
            .mutate()
            .apply { setColorFilter(color, PorterDuff.Mode.SRC_ATOP) }

    /**
     * Returns a color string in hex format with no alpha
     * @param color a valid color in the form of an Integer
     * @return A hex string form of the color with no alpha
     */
    @JvmStatic fun getColorStringFromInt(color: Int, prefixWithHashTag: Boolean): String {
        var colorStr = Integer.toHexString(color)
        colorStr = colorStr.substring(colorStr.length - 6)
        return if (prefixWithHashTag && !colorStr.startsWith("#")) {
            "#$colorStr"
        } else {
            colorStr
        }
    }

    /**
     * Attempts to parse a color from a hex string.
     * @param hexColor A hex color string. May optionally be prefixed with '#'
     * @return The parsed color, or [defaultColor] if the string could not be parsed
     */
    private fun parseColor(hexColor: String): Int = try {
        val trimmedColorCode = getTrimmedColorCode(hexColor)
        ColorUtils.parseColor(trimmedColorCode, defaultColor = defaultColor)
    } catch (e: IllegalArgumentException) {
        defaultColor
    }

    // There might be cases where the color code from the response contains whitespaces.
    private fun getTrimmedColorCode(colorCode: String): String {
        return if (colorCode.contains("#")) {
            "#${colorCode.trimMargin("#")}"
        } else {
            colorCode
        }
    }

    /**
     * Generates a generic color based on the canvas context id, this will produce consistent colors for a given course
     * @param canvasContext a valid canvas context
     * @return the generated colors
     */
    private fun generateColor(canvasContext: CanvasContext): ThemedColor {
        if (canvasContext.type == CanvasContext.Type.USER || canvasContext.name.isNullOrBlank()) {
            return ThemedColor(defaultColor) // defaultColor is already themed so we don't need 3 different colors
        }

        val colorRes = when (Math.abs((canvasContext.name?.hashCode() ?: "Null Name".hashCode()) % 12)) {
            0 -> R.color.courseColor1
            1 -> R.color.courseColor2
            2 -> R.color.courseColor3
            3 -> R.color.courseColor4
            4 -> R.color.courseColor5
            5 -> R.color.courseColor6
            6 -> R.color.courseColor7
            7 -> R.color.courseColor8
            8 -> R.color.courseColor9
            9 -> R.color.courseColor10
            10 -> R.color.courseColor11
            else -> R.color.courseColor12
        }

        val color = ContextCompat.getColor(ContextKeeper.appContext, colorRes)
        val themedColor = createThemedColor(color)
        cachedThemedColors += canvasContext.contextId to themedColor
        return themedColor
    }

    override fun onClearPrefs() {}
}

@Suppress("unused", "DEPRECATION")
object ColorApiHelper {

    const val K5_DEFAULT_COLOR = "#394B58"

    /**
     * Sets a new color to the api and caches the result
     * @param canvasContext canvasContext
     * @param newColor the new color to set
     */
    fun setNewColor(canvasContext: CanvasContext, newColor: Int, onColorSet: (color: Int, success: Boolean) -> Unit) {
        UserManager.setColors(object : StatusCallback<CanvasColor>() {
            override fun onResponse(response: Response<CanvasColor>, linkHeaders: LinkHeaders, type: ApiType) {
                if (type.isAPI) {
                    ColorKeeper.addToCache(canvasContext.contextId, newColor)
                    onColorSet(newColor, true)
                }
            }

            override fun onFail(call: Call<CanvasColor>?, error: Throwable, response: Response<*>?) = onColorSet(newColor, false)
        }, canvasContext.contextId, newColor)
    }

    /**
     * Attempts to pull and cache colors from the API.
     */
    private fun performSync(onSynced: (success: Boolean) -> Unit) {
        UserManager.getColors(object : StatusCallback<CanvasColor>() {
            override fun onResponse(response: Response<CanvasColor>, linkHeaders: LinkHeaders, type: ApiType) {
                if (type == ApiType.API) {
                    ColorKeeper.addToCache(response.body())
                    onSynced(true)
                }
            }

            override fun onFail(call: Call<CanvasColor>?, error: Throwable, response: Response<*>?) = onSynced(false)
        }, true)
    }

    suspend fun awaitSync(): Boolean = suspendCancellableCoroutine { cr -> performSync { cr.resumeSafely(it) } }
}

// There are some custom colors that we can map to a specific dark variant. For others we use our accessibility algorithm.
// Since the student colors in the Parent app are using some of these colors we don't add them, because that would be a duplicate entry in the map.
private val lightDarkColorMap: Map<Int, Int> = mapOf(
    ContextKeeper.appContext.getColor(R.color.courseColor1light) to ContextKeeper.appContext.getColor(R.color.courseColor1dark),
    ContextKeeper.appContext.getColor(R.color.courseColor2light) to ContextKeeper.appContext.getColor(R.color.courseColor2dark),
    ContextKeeper.appContext.getColor(R.color.courseColor3light) to ContextKeeper.appContext.getColor(R.color.courseColor3dark),
    ContextKeeper.appContext.getColor(R.color.courseColor4light) to ContextKeeper.appContext.getColor(R.color.courseColor4dark),
    ContextKeeper.appContext.getColor(R.color.courseColor5light) to ContextKeeper.appContext.getColor(R.color.courseColor5dark),
    ContextKeeper.appContext.getColor(R.color.courseColor6light) to ContextKeeper.appContext.getColor(R.color.courseColor6dark),
    ContextKeeper.appContext.getColor(R.color.courseColor7light) to ContextKeeper.appContext.getColor(R.color.courseColor7dark),
    ContextKeeper.appContext.getColor(R.color.courseColor8light) to ContextKeeper.appContext.getColor(R.color.courseColor8dark),
    ContextKeeper.appContext.getColor(R.color.courseColor9light) to ContextKeeper.appContext.getColor(R.color.courseColor9dark),
    ContextKeeper.appContext.getColor(R.color.courseColor10light) to ContextKeeper.appContext.getColor(R.color.courseColor10dark),
    ContextKeeper.appContext.getColor(R.color.courseColor11light) to ContextKeeper.appContext.getColor(R.color.courseColor11dark),
    ContextKeeper.appContext.getColor(R.color.courseColor12light) to ContextKeeper.appContext.getColor(R.color.courseColor12dark)
)

data class ThemedColor(
    @ColorInt val light: Int,
    @ColorInt val dark: Int = light,
) {
    @ColorInt
    fun color() = if (ColorKeeper.darkTheme) dark else light
}