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
@file:JvmName("PandaViewUtils")
@file:Suppress("unused", "FunctionName")

package com.instructure.pandautils.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.TouchDelegate
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.ViewParent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.Transformation
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.tryOrNull
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.pandautils.R
import com.instructure.pandautils.features.speedgrader.comments.SpeedGraderCommentAttachment
import kotlinx.coroutines.delay
import java.lang.reflect.Field
import java.util.Locale
import kotlin.math.hypot

/** Convenience extension for setting a click listener */
@Suppress("NOTHING_TO_INLINE")
inline fun View.onClick(noinline l: (v: View) -> Unit) {
    setOnClickListener(l)
}

/** Convenience extension for setting a long click listener */
@Suppress("NOTHING_TO_INLINE")
inline fun View.onLongClick(noinline l: (v: View?) -> Boolean) {
    setOnLongClickListener(l)
}

/** Set this view's visibility to View.VISIBLE **/
@Suppress("NOTHING_TO_INLINE")
inline fun <T : View> T.setVisible(isVisible: Boolean = true): T = apply {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}

/** Set this view's visibility to View.INVISIBLE **/
@Suppress("NOTHING_TO_INLINE")
inline fun <T : View> T.setHidden(isHidden: Boolean = true): T = apply {
    visibility = if (isHidden) View.INVISIBLE else View.VISIBLE
}

/** Set this view's visibility to View.INVISIBLE **/
@Suppress("NOTHING_TO_INLINE")
inline fun <T : View> T.setInvisible(): T = apply { visibility = View.INVISIBLE }

/** Set this view's visibility to View.GONE **/
@Suppress("NOTHING_TO_INLINE")
inline fun <T : View> T.setGone(): T = setVisible(false)

/** Set this view's visibility to View.VISIBLE **/
val View.isVisible get() = visibility == View.VISIBLE

/** Set this view's visibility to View.VISIBLE **/
val View.isInvisible get() = visibility == View.INVISIBLE

/** Set this view's visibility to View.VISIBLE **/
val View.isGone get() = visibility == View.GONE

/** Show a toast with a default length of Toast.LENGTH_SHORT */
fun View.toast(messageResId: Int, length: Int = Toast.LENGTH_SHORT) = Toast.makeText(context, messageResId, length).show()

/** Converts float DIP value to pixel value */
fun Context.DP(value: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)

/** Converts Int DIP value to pixel value */
fun Context.DP(value: Int) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics)

/** Converts float DIP value to pixel value */
fun Context.SP(value: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, resources.displayMetrics)

/** Converts Int DIP value to pixel value */
fun Context.SP(value: Int) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value.toFloat(), resources.displayMetrics)

/** Converts float Pixel value to DIP value */
fun Context.PX(px: Int) = (px / resources.displayMetrics.density).toInt()

/** Converts float Pixel value to DIP value */
fun Context.PX(px: Float) = (px / resources.displayMetrics.density).toInt()

fun Context.toast(@StringRes resId: Int, toastLength: Int = Toast.LENGTH_SHORT) = Toast.makeText(this, resId, toastLength).show()

fun Context.toast(text: String, toastLength: Int = Toast.LENGTH_SHORT) = Toast.makeText(this, text, toastLength).show()

fun EditText.onTextChanged(listener: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable) {}
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            listener(s.toString())
        }
    })
}

/**
 * Returns a non-null drawable using [ContextCompat]. Throws a [Resources.NotFoundException] if the specified
 * resource does not exist or there was another problem obtaining the drawable.
 */
fun Context.getDrawableCompat(@DrawableRes resId: Int): Drawable = ContextCompat.getDrawable(this, resId)
        ?: throw Resources.NotFoundException("Unable to obtain drawable from resource ID $resId")

/**
 * Returns true if the view's layout direction is Right-To-Left, false otherwise
 */
fun View.isRTL() = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL

/** Convenience extension property for getting the MeasureSpec size */
val Int.specSize get() = View.MeasureSpec.getSize(this)
/** Convenience extension property for getting the MeasureSpec mode */
val Int.specMode get() = View.MeasureSpec.getMode(this)

/** Returns this color with the specified brightness applied (valid range 0f to 1f) */
fun Int.withLuminance(alpha: Float): Int {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(this, hsl)
    hsl[2] = alpha.coerceIn(0f, 1f)
    return ColorUtils.HSLToColor(hsl)
}

/** Returns a [ColorStateList] with a single state set to this color */
fun Int.asStateList(): ColorStateList = ColorStateList.valueOf(this)

/** Returns a list of all immediate child views in this ViewGroup */
val View.children: List<View>
    get() = (this as? ViewGroup)?.let { (0 until childCount).map { getChildAt(it) } } ?: emptyList()

/** Returns a list of all immediate child views of the specified type in this ViewGroup */
inline fun <reified T : View> View.children(): List<T> = children.filterIsInstance<T>()

/** Returns a list of all views in this ViewGroup */
val View.descendants: List<View> get() = children + children<ViewGroup>().flatMap { it.descendants }

/** Returns a list of all views of the specified type in this ViewGroup */
inline fun <reified T : View> View.descendants(): List<T> = descendants.filterIsInstance<T>()

/** Returns the closest Ancestor of the specified type, or null if there are no matches */
inline fun <reified V : View> View.firstAncestorOrNull(): V? {
    var p: ViewParent? = parent
    while (p != null) {
        if (p is V) {
            return p
        } else {
            p = p.parent
        }
    }
    return null
}

/** Returns the most distant ancestor of the specified type, or null if there are no matches */
inline fun <reified V : View> View.lastAncestorOrNull(): V? {
    var p: ViewParent? = parent
    var ancestor: V? = null
    while (p != null) {
        if (p is V) ancestor = p
        p = p.parent
    }
    return ancestor
}

/** Returns a list of all non-null [MenuItem]s currently in this menu */
val Menu.items: List<MenuItem> get() = (0 until size()).mapNotNull { getItem(it) }

/**
 * Returns the vertical pixel offset of the top of this view inside the specified ViewGroup.
 * Returns 0 if the ViewGroup is not an ancestor of this view.
 */
fun View.topOffsetIn(ancestor: ViewGroup): Int {
    var offset = top
    var p: ViewParent? = parent
    while (p != null && p is ViewGroup) {
        if (p === ancestor) {
            return offset
        } else {
            offset += p.top
            p = p.parent
        }
    }
    return 0
}

/** Convenience property which wraps getLocationOnScreen() */
val View.positionOnScreen: Pair<Int, Int>
    get() {
        val arr = intArrayOf(0, 0)
        getLocationOnScreen(arr)
        return Pair(arr[0], arr[1])
    }

/**
 * [Binder] is a delegation class for manual view binding, useful for cases not covered by Kotlin
 * Android Extensions. Generally, bound view properties will not directly instantiate this class.
 * Instead, instantiation of a Binder instance should be handled by an extension function of the
 * class in which views are to be bound, e.g. Dialog.[bind].
 *
 * Example of how to use [Binder] in classes that have a bind() extension function:
 * ```
 * val myImageView by bind<ImageView>(R.id.my_image_view)
 * ```
 *
 * If your layout has multiple views with the same id but different parents:
 * ```
 * val myImageView1 by bind<ImageView>(R.id.my_image_view).withParent(R.id.parent_one)
 * val myImageView2 by bind<ImageView>(R.id.my_image_view).withParent(R.id.parent_two)
 * ```
 * Or, if you already have a reference to the parent views:
 * ```
 * val myImageView1 by bind<ImageView>(R.id.my_image_view).withParent { parentLayout1 }
 * val myImageView2 by bind<ImageView>(R.id.my_image_view).withParent { parentLayout2 }
 * ```
 *
 * For examples on how to create a bind() extension function for a new class, refer to Dialog.bind(),
 * ViewGroup.bind(), Activity.bind(), or Fragment.bind().
 *
 */
@Suppress("UNCHECKED_CAST")
class Binder<in T, out V : View>(@IdRes private val viewId: Int, private val finder: (T, Int) -> View?) : kotlin.properties.ReadOnlyProperty<T, V> {

    private var cachedView: V? = null
    private var useParent = false
    private var parentId: Int? = null
    private var parentProvider: (() -> View)? = null

    override fun getValue(thisRef: T, property: kotlin.reflect.KProperty<*>): V {
        if (cachedView == null) {
            val v: View
            if (useParent) {
                v = when {
                    parentProvider != null -> {
                        val parentView = parentProvider!!.invoke()
                        parentView.findViewById(viewId)
                                ?: throw RuntimeException("Unable to bind ${property.name}; view not found in provided parent ${parentView.javaClass.simpleName}")
                    }
                    parentId != null -> {
                        val parentView = finder(thisRef, parentId!!)
                                ?: throw RuntimeException("Unable to bind ${property.name}; could not find specified parent with id ${ContextKeeper.appContext.resources.getResourceEntryName(parentId!!)}")
                        parentView.findViewById(viewId)
                                ?: throw RuntimeException("Unable to bind ${property.name}; view not found in specified parent with id ${ContextKeeper.appContext.resources.getResourceEntryName(parentId!!)}")
                    }
                    else -> throw RuntimeException("Unable to bind ${property.name}; please provide parent view or specify parent view id")
                }
            } else {
                v = finder(thisRef, viewId) ?: throw RuntimeException("Unable to bind ${property.name}; findViewById returned null.")
            }
            cachedView = v as V
        }
        return cachedView!!
    }

    fun withParent(@IdRes parentId: Int): Binder<T, V> {
        useParent = true
        this.parentId = parentId
        return this
    }

    fun withParent(parentProvider: () -> View): Binder<T, V> {
        useParent = true
        this.parentProvider = parentProvider
        return this
    }

}

fun AttributeSet.obtainFor(view: View, styleableRes: IntArray, onAttribute: (a: TypedArray, index: Int) -> Unit) {
    val a: TypedArray = view.context.obtainStyledAttributes(this, styleableRes)
    for (i in 0 until a.indexCount) onAttribute(a, a.getIndex(i))
    a.recycle()
}

/**
 * Show the keyboard
 */
fun View.showKeyboard() {
    val imm = (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

/**
 * Hide the keyboard
 */
fun View.hideKeyboard() {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

/** Provides a view-binding delegate inside classes extending [Dialog]. See [Binder] for more information. */
inline fun <reified V : View> Dialog.bind(@IdRes id: Int): Binder<Dialog, V> = Binder(id) { dialog, viewId -> dialog.findViewById<V>(viewId) }

/** Provides a view-binding delegate inside classes extending [ViewGroup]. See [Binder] for more information. */
inline fun <reified V : View> ViewGroup.bind(@IdRes id: Int): Binder<ViewGroup, V> = Binder(id) { viewGroup, viewId -> viewGroup.findViewById<V>(viewId) }

/** Provides a view-binding delegate inside classes extending [Activity]. See [Binder] for more information. */
inline fun <reified V : View> Activity.bind(@IdRes id: Int): Binder<Activity, V> = Binder(id) { activity, viewId -> activity.findViewById<V>(viewId) }

/** Provides a view-binding delegate inside classes extending [Fragment]. See [Binder] for more information. */
fun <V : View> Fragment.bind(@IdRes id: Int): Binder<Fragment, V> = Binder(id) { it, viewId -> it.view?.findViewById(viewId) }

fun View.requestAccessibilityFocus(delay: Long = 500) {
    if (context.a11yManager.hasSpokenFeedback) {
        isFocusable = true
        isFocusableInTouchMode = true
        postDelayed({ requestFocus(); sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED) }, delay)
    }
}

/**
 * OnClickListener for checking internet connection first. If connection exits allow click,
 * otherwise show no internet dialog.
 */
fun View.onClickWithRequireNetwork(clickListener: OnClickListener) = onClick {
    if (APIHelper.hasNetworkConnection()) {
        //Allow click
        clickListener.onClick(this)
    } else {
        //show dialog
        showNoConnectionDialog(context)
    }
}

fun Fragment.noConnectionDialogWithNetworkCheck() {
    if (!APIHelper.hasNetworkConnection()) {
        showNoConnectionDialog(requireContext()) { requireActivity().onBackPressed() }
    }
}

fun showNoConnectionDialog(context: Context, actionAfterDismiss: () -> Unit = {}) {
    AlertDialog.Builder(context)
        .setTitle(R.string.noInternetConnectionTitle)
        .setMessage(R.string.noInternetConnectionMessage)
        .setCancelable(true)
        .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
        .setOnDismissListener { _ -> actionAfterDismiss() }
        .showThemed()
}

/**
 * Attempts to download and set the course image on this ImageView. The image will be center cropped,
 * desaturated, and overlaid with the specified color at 75% opacity.
 */
@JvmName("setCourseImage")
fun ImageView?.setCourseImage(course: Course?, courseColor: Int, applyColor: Boolean) {
    this.setCourseImage(course?.imageUrl, courseColor, applyColor)
}

/**
 * Attempts to download and set the course image on this ImageView. The image will be center cropped,
 * desaturated, and overlaid with the specified color at 75% opacity.
 */
@JvmName("setCourseImage")
fun ImageView?.setCourseImage(imageUrl: String?, courseColor: Int, applyColor: Boolean) {
    if (this == null) return
    if (!imageUrl.isNullOrBlank()) {
        val requestOptions = RequestOptions().apply {
            if (applyColor) {
                signature(ObjectKey("${imageUrl}:$courseColor")) // Use unique signature per url-color combo
                transform(CourseImageTransformation(courseColor))
            } else {
                transform(CenterCrop())
            }
            placeholder(ColorDrawable(courseColor))
        }
        Glide.with(context)
            .load(imageUrl)
            .apply(requestOptions)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(this)
    } else {
        setImageDrawable(ColorDrawable(courseColor))
    }
}

/** A Glide transformation to center, crop, desaturate, and colorize course images */
private class CourseImageTransformation(val overlayColor: Int) : CenterCrop() {

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val cropped = super.transform(pool, toTransform, outWidth, outHeight)
        with(Canvas(cropped)) {
            // Draw image in grayscale
            drawBitmap(cropped, 0f, 0f, grayscalePaint)
            // Draw color overlay at 84% (0xBF) opacity
            drawColor(overlayColor and 0xDFFFFFFF.toInt())
        }
        return cropped
    }

    companion object {
        private val grayscalePaint by lazy {
            val colorMatrix = ColorMatrix().apply { setSaturation(0f) }
            Paint().apply { colorFilter = ColorMatrixColorFilter(colorMatrix) }
        }
    }

}

/**
 * Attempts to load the provided Uri into this ImageView
 */
@JvmName("loadImageFromUri")
@JvmOverloads
fun ImageView?.loadUri(imageUri: Uri?, errorImageResourceId: Int = 0) {
    if (this == null) return
    imageUri?.path?.let { path ->
        if (path.contains(".svg", ignoreCase = true)) {
            SvgUtils.loadSVGImage(this, imageUri, errorImageResourceId)
        } else {
            // There was an issue with some png images not being shown correctly when coming back to an activity. Clearing the image gave a smoother
            // experience than adding a signature to force a reload. We currently use this in only a few places, this could affect performance
            // if it was used in a recycler view
            Glide.with(context).clear(this)
            Glide.with(context).load(imageUri).apply(RequestOptions.errorOf(errorImageResourceId)).into(this)
        }
        return
    }
    if (errorImageResourceId > 0) setImageResource(errorImageResourceId)
}

/**
 * Sets the specified drawable resource on this [ImageView] and tints it using the speficied [color]
 */
@JvmName("setColoredImageResource")
fun ImageView?.setColoredResource(@DrawableRes resId: Int, @ColorInt color: Int) {
    if (this == null) return
    val drawable = ColorKeeper.getColoredDrawable(context, resId, color)
    setImageDrawable(drawable)
}

/**
 * Sets the given text on this TextView. If the text is valid (i.e. non-null and non-blank) then this view's
 * visibility will be set to [View.VISIBLE], otherwise it will be set to [invalidVisibility] which defaults
 * to [View.GONE].
 */
fun TextView.setTextForVisibility(newText: String?, invalidVisibility: Int = View.GONE) {
    text = newText
    if (newText.isValid()) {
        setVisible()
    } else {
        this.visibility = invalidVisibility
    }
}

/**
 * Adds a basic avatar content description as well as a click action description which includes the
 * user's name. E.g. "John Doe Avatar button. Double-tap to view user details for John Doe."
 */
fun View.setupAvatarA11y(userName: String?) {
    contentDescription = context.getString(R.string.a11y_avatarNameFormatted, userName.orEmpty())
    setAccessibilityDelegate(object : View.AccessibilityDelegate() {
        override fun onInitializeAccessibilityNodeInfo(v: View, info: AccessibilityNodeInfo) {
            super.onInitializeAccessibilityNodeInfo(v, info)
            val description = context.getString(R.string.formattedAvatarAction, userName)
            val customClick = AccessibilityNodeInfo.AccessibilityAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK.id, description)
            info.addAction(customClick)
        }
    })
}

/** Clears the view's content description and click action description. */
fun View.clearAvatarA11y() {
    contentDescription = ""
    setAccessibilityDelegate(null)
}

/**
 * Loads the given resource as this Toolbar's icon, assigns it the given content description, and
 * propagates its clicks to the provided function.
 */
@JvmName("setupToolbarNavButtonWithCallback")
fun Toolbar?.setupAsNavButtonWithCallback(
        @DrawableRes iconResId: Int,
        @StringRes contentDescriptionResId: Int,
        onClick: () -> Unit) {
    if (this == null) return
    setNavigationIcon(iconResId)
    setNavigationContentDescription(contentDescriptionResId)
    setNavigationOnClickListener { onClick() }
    requestAccessibilityFocus()
}

/**
 * Loads the given resource as this Toolbar's icon, assigns it the given content description, and
 * propagates its clicks to the provided function.
 */
@JvmName("setupToolbarNavButtonWithoutCallback")
fun Toolbar?.setupAsNavButtonIcon(
        @DrawableRes iconResId: Int,
        @StringRes contentDescriptionResId: Int) {
    if (this == null) return
    setNavigationIcon(iconResId)
    setNavigationContentDescription(contentDescriptionResId)
    requestAccessibilityFocus()
}

/**
 * Changes this Toolbar's icon to a back arrow. Click events on this icon are propagated to the
 * provided function
 */
@SuppressLint("PrivateResource")
@JvmName("setupToolbarBackButton")
fun Toolbar?.setupAsBackButton(onClick: () -> Unit) = setupAsNavButtonWithCallback(
        R.drawable.abc_ic_ab_back_material,
        R.string.abc_action_bar_up_description,
        onClick
)

/**
 * Changes this Toolbar's icon to a back arrow. Click events will attempt to call onBackPressed()
 * on the given fragment's activity
 */
@JvmName("setupToolbarBackButton")
fun Toolbar?.setupAsBackButton(fragment: Fragment?) = setupAsBackButton {
    fragment?.activity?.onBackPressed()
}

/**
 * Changes this Toolbar's icon to a back arrow. Click events will attempt to call onBackPressed()
 * on the given fragment's activity
 */
@JvmName("setupToolbarBackButtonAsBackPressedOnly")
fun Toolbar?.setupAsBackButtonAsBackPressedOnly(fragment: Fragment?) = setupAsBackButton {
    fragment?.activity?.onBackPressed()
}

/**
 * Changes this Toolbar's icon to a close (X) icon. Click events on this icon are propagated to the
 * provided function.
 */
@SuppressLint("PrivateResource")
@JvmName("setupToolbarCloseButton")
fun Toolbar?.setupAsCloseButton(onClick: () -> Unit) = setupAsNavButtonWithCallback(
        R.drawable.abc_ic_clear_material, R.string.close,
        onClick
)


/**
 * Changes this Toolbar's icon to a close (X) icon. Click events will attempt to call onBackPressed()
 * on the given fragment's activity
 */
@JvmName("setupToolbarCloseButton")
fun Toolbar?.setupAsCloseButton(fragment: Fragment?) = setupAsCloseButton { fragment?.activity?.onBackPressed() }

/**
 * Inflates the provided menu resource into this Toolbar and propagates menu item click events
 * to the provided callback
 *
 * Note: This clears any existing menu items. It is safe to call multiple times throughout
 * the Activity/Fragment lifecycle, but should probably not be used when it is expected that
 * the menu will also be populated by other sources.
 */
@JvmName("setupToolbarMenu")
fun Toolbar?.setMenu(@MenuRes menuResId: Int, callback: (MenuItem) -> Unit) {
    if (this == null) return
    menu.clear()
    inflateMenu(menuResId)
    setOnMenuItemClickListener { callback(it); true }
}

fun EditText.setCursorColor(@ColorInt color: Int) {
    try {
        val tvClass = TextView::class.java
        val drawableResId = tvClass.getDeclaredField("mCursorDrawableRes").apply { isAccessible = true }.getInt(this)
        val editor = tvClass.getDeclaredField("mEditor").apply { isAccessible = true }.get(this)
        val drawable = ContextCompat.getDrawable(context, drawableResId)
        drawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        val drawables = arrayOf(drawable, drawable)
        editor.javaClass.getDeclaredField("mCursorDrawable").apply { isAccessible = true }.set(editor, drawables)
    } catch (ignored: Exception) {
    }
}

fun RecyclerView.removeAllItemDecorations() {
    (0 until itemDecorationCount)
        .map { getItemDecorationAt(it) }
        .forEach { removeItemDecoration(it) }
}

fun EditText.onChangeDebounce(minLength: Int = 3, debounceDuration: Long = 400, onTextChanged: (String) -> Unit) {
    var lastText = "" // Track the previous contents to reduce duplicates
    var debounce: WeaveJob? = null
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) = Unit
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            debounce?.cancel()
            debounce = weave {
                val query = s.toString().trim().takeIf { it.length >= minLength }.orEmpty()
                delay(if (query.isBlank()) 0 else debounceDuration)
                if (query != lastText) {
                    lastText = query
                    tryOrNull { onTextChanged(query) }
                }
            }
        }
    })
}

@Suppress("PLUGIN_WARNING")
@JvmName("addToolbarSearch")
fun Toolbar?.addSearch(hintText: String? = null, @ColorInt color: Int? = null, onQueryChanged: (String) -> Unit) {
    if (this == null || menu.findItem(R.id.search) != null) return
    inflateMenu(R.menu.search)
    val searchItem = menu.findItem(R.id.search)
    val searchColor = color ?: context.getColor(R.color.textLightest)
    val toolbar = this
    with(searchItem.actionView as SearchView) {
        maxWidth = Int.MAX_VALUE
        setIconifiedByDefault(false)
        findViewById<ImageView>(R.id.search_mag_icon)?.setImageDrawable(null)
        queryHint = hintText ?: context.getString(R.string.search)
        setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            var lastQuery = "" // Track the last sent query to reduce duplicates
            var debounce: WeaveJob? = null
            override fun onQueryTextSubmit(query: String): Boolean = true
            override fun onQueryTextChange(newText: String): Boolean {
                debounce?.cancel()
                debounce = weave {
                    val query = newText.trim().takeIf { it.length > 2 }.orEmpty()
                    delay(if (query.isBlank()) 0 else 400)
                    if (query != lastQuery) {
                        lastQuery = query
                        tryOrNull { onQueryChanged(query) }
                    }
                }
                return true
            }
        })
        themeSearchView(toolbar, searchColor)
    }
}

fun SearchView.themeSearchView(
    toolbar: Toolbar,
    searchColor: Int,
) {
    toolbar.colorSearchViewBackButton(searchColor)
    findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)?.apply {
        setColorFilter(searchColor, PorterDuff.Mode.SRC_ATOP)
    }
    findViewById<EditText>(R.id.search_src_text)?.apply {
        setTextColor(searchColor)
        setCursorColor(searchColor)
        setHintTextColor(ColorUtils.setAlphaComponent(searchColor, 0x66))
        setCompoundDrawables(null, null, null, null)
    }
}

private fun Toolbar.colorSearchViewBackButton(searchColor: Int) {
    try {
        val backIcon: Field = javaClass.getDeclaredField("mCollapseIcon")
        backIcon.isAccessible = true
        val backDrawable = backIcon.get(this) as? Drawable
        backDrawable?.setTint(searchColor)
    } catch (e: java.lang.Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
    }
}

fun Toolbar?.closeSearch() : Boolean {
    return this?.menu?.findItem(R.id.search)?.collapseActionView() == true
}

val Attachment.iconRes: Int
    get() = when {
        contentType == null -> R.drawable.ic_attachment
        contentType!!.startsWith("image") -> R.drawable.ic_image
        contentType!!.startsWith("video") -> R.drawable.ic_media
        contentType!!.startsWith("audio") -> R.drawable.ic_audio
        else -> when (filename!!.substringAfterLast('.', "").lowercase(Locale.getDefault())) {
            "doc", "docx" -> R.drawable.ic_document
            "txt" -> R.drawable.ic_document
            "rtf" -> R.drawable.ic_document
            "pdf" -> R.drawable.ic_pdf
            "xls" -> R.drawable.ic_document
            "zip", "tar", "7z", "apk", "jar", "rar" -> R.drawable.ic_attachment
            else -> R.drawable.ic_attachment
        }
    }

val SpeedGraderCommentAttachment.iconRes: Int
    get() = when {
        contentType.startsWith("image") -> R.drawable.ic_image
        contentType.startsWith("video") -> R.drawable.ic_media
        contentType.startsWith("audio") -> R.drawable.ic_audio
        else -> when (title.substringAfterLast('.', "").lowercase(Locale.getDefault())) {
            "doc", "docx" -> R.drawable.ic_document
            "txt" -> R.drawable.ic_document
            "rtf" -> R.drawable.ic_document
            "pdf" -> R.drawable.ic_pdf
            "xls" -> R.drawable.ic_document
            "zip", "tar", "7z", "apk", "jar", "rar" -> R.drawable.ic_attachment
            else -> R.drawable.ic_attachment
        }
    }

val Int.toDp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.toPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

private const val ACCESSIBILITY_MIN_DIMENSION_IN_DP = 48

fun View.accessibleTouchTarget() {
    post {
        val delegateArea = Rect()
        getHitRect(delegateArea)

        val accessibilityMin = context.DP(ACCESSIBILITY_MIN_DIMENSION_IN_DP)

        val height = delegateArea.bottom - delegateArea.top
        if (accessibilityMin > height) {
            // Add +1 px just in case min - height is odd and will be rounded down
            val addition = ((accessibilityMin - height) / 2).toInt() + 1
            delegateArea.top -= addition
            delegateArea.bottom += addition
        }

        val width = delegateArea.right - delegateArea.left
        if (accessibilityMin > width) {
            // Add +1 px just in case min - width is odd and will be rounded down
            val addition = ((accessibilityMin - width) / 2).toInt() + 1
            delegateArea.left -= addition
            delegateArea.right += addition
        }

        val parentView = parent as? View
        parentView?.touchDelegate = TouchDelegate(delegateArea, this)
    }
}

// Starts a fade out/fade in animation for the view and executes the specified action while the view is not showing.
fun View.fadeAnimationWithAction(action: () -> Unit) {
    val fadeOutAnim = AlphaAnimation(1.0f, 0.0f)
    fadeOutAnim.duration = 250
    fadeOutAnim.interpolator = DecelerateInterpolator()
    fadeOutAnim.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) = Unit

        override fun onAnimationEnd(animation: Animation?) {
            action()

            val fadeInAnim = AlphaAnimation(0.0f, 1.0f)
            fadeInAnim.duration = 250
            fadeInAnim.interpolator = DecelerateInterpolator()
            startAnimation(fadeInAnim)
        }

        override fun onAnimationRepeat(animation: Animation?) = Unit
    })

    startAnimation(fadeOutAnim)
}

/**
 * Load model into ImageView as a circle image using Glide
 *
 * @param model - Any object supported by Glide (Uri, File, Bitmap, String, resource id as Int, ByteArray, and Drawable)
 * @param placeholder - Placeholder drawable
 * @param onFailure - Called when an exception occurs during a load
 */
@SuppressLint("CheckResult")
fun <T> ImageView.loadCircularImage(
    model: T,
    placeholder: Int? = null,
    onFailure: (() -> Unit)? = null
) {
    Glide.with(context)
        .asBitmap()
        .load(model)
        .apply { placeholder?.let { placeholder(it) } }
        .apply(RequestOptions.circleCropTransform())
        .addListener(object : RequestListener<Bitmap> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Bitmap>,
                isFirstResource: Boolean
            ): Boolean {
                onFailure?.invoke()
                return true
            }

            override fun onResourceReady(
                resource: Bitmap,
                model: Any,
                target: Target<Bitmap>?,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }
        })
        .into(this)
}


fun Animation.addListener(onStart: (Animation?) -> Unit = {}, onEnd: (Animation?) -> Unit = {}, onRepeat: (Animation?) -> Unit = {}) {
    this.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) {
            onStart(animation)
        }

        override fun onAnimationEnd(animation: Animation?) {
            onEnd(animation)
        }

        override fun onAnimationRepeat(animation: Animation?) {
            onRepeat(animation)
        }
    })
}

fun View.expand(duration: Long = 300) {
    if (visibility == View.VISIBLE) return

    this.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    val targetHeight = this.measuredHeight

    this.layoutParams.height = 0
    this.visibility = View.VISIBLE
    val animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            this@expand.layoutParams.height = if (interpolatedTime == 1f)
                ViewGroup.LayoutParams.WRAP_CONTENT
            else
                (targetHeight * interpolatedTime).toInt()
            this@expand.requestLayout()
        }

        override fun willChangeBounds(): Boolean = true
    }

    animation.duration = duration

    this.startAnimation(animation)
}

fun View.collapse(duration: Long = 300) {
    if (visibility == View.GONE) return

    val initialHeight = this.measuredHeight

    val animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            if (interpolatedTime == 1f) {
                this@collapse.visibility = View.GONE
            } else {
                this@collapse.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                this@collapse.requestLayout()
            }
        }

        override fun willChangeBounds(): Boolean = true
    }

    animation.duration = duration

    this.startAnimation(animation)
}

fun View.animateCircularBackgroundColorChange(endColor: Int, image: ImageView, duration: Long = 400L) {
    if (!isLaidOut) return

    val w = measuredWidth
    val h = measuredHeight

    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    draw(canvas)

    image.setImageBitmap(bitmap)
    image.setVisible(true)

    val finalRadius = hypot(w.toFloat(), h.toFloat())

    val anim = ViewAnimationUtils.createCircularReveal(this, w / 2, h / 2, 0f, finalRadius)

    anim.duration = duration
    anim.doOnStart {
        setBackgroundColor(endColor)
    }
    anim.doOnEnd {
        image.setImageDrawable(null)
        image.setVisible(false)
    }

    anim.start()
}

fun View.showSnackbar(
    @StringRes message: Int,
    @StringRes actionTextRes: Int? = R.string.retry,
    @ColorRes actionTextColor: Int? = R.color.white,
    actionCallback: (() -> Unit)? = null
) {
    val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_SHORT)

    actionTextColor?.let { snackbar.setActionTextColor(context.getColor(it)) }

    actionTextRes?.let { textRes ->
        actionCallback?.let {
            snackbar.setAction(textRes) { it() }
        }
    }

    snackbar.show()
    snackbar.view.requestAccessibilityFocus(1000)
}
