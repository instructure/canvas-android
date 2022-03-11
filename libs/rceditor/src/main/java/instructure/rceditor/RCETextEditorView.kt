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

package instructure.rceditor

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import android.util.AttributeSet
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.webkit.URLUtil
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.rce_color_picker.view.*
import kotlinx.android.synthetic.main.rce_controller.view.*
import kotlinx.android.synthetic.main.rce_text_editor_view.view.rce_bottomDivider as bottomDivider
import kotlinx.android.synthetic.main.rce_text_editor_view.view.rce_colorPickerWrapper as colorPickerView
import kotlinx.android.synthetic.main.rce_text_editor_view.view.rce_controller as controller
import kotlinx.android.synthetic.main.rce_text_editor_view.view.rce_webView as editor

@Suppress("unused")
class RCETextEditorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    @ColorInt
    private var themeColor = Color.BLACK

    @ColorInt
    private var buttonColor = Color.BLACK

    private var actionTypeButtonMap: Map<String, ImageButton?>

    val html: String get() = editor?.html.orEmpty()

    private val fragmentManager get() = (context as? FragmentActivity)?.supportFragmentManager

    // Let the fragments that use this view handle what to do when it's clicked
    var actionUploadImageCallback: (() -> Unit)? = null

    init {
        View.inflate(getContext(), R.layout.rce_text_editor_view, this)

        if (attrs != null) {
            val a = getContext().obtainStyledAttributes(attrs, R.styleable.RCETextEditorView, 0, 0)
            fun Float.pxToDp() = (this / Resources.getSystem().displayMetrics.density).toInt()
            if (a.hasValue(R.styleable.RCETextEditorView_rce_editor_padding)) {
                val editorPadding = a.getDimension(R.styleable.RCETextEditorView_rce_editor_padding, 0f).pxToDp()
                editor.setPadding(editorPadding, editorPadding, editorPadding, editorPadding)
            } else {
                val padStart = a.getDimension(R.styleable.RCETextEditorView_rce_editor_padding_start, 0f).pxToDp()
                val padEnd = a.getDimension(R.styleable.RCETextEditorView_rce_editor_padding_end, 0f).pxToDp()
                val padTop = a.getDimension(R.styleable.RCETextEditorView_rce_editor_padding_top, 0f).pxToDp()
                val pagBottom = a.getDimension(R.styleable.RCETextEditorView_rce_editor_padding_bottom, 0f).pxToDp()
                editor.setPadding(padStart, padTop, padEnd, pagBottom)
            }

            val controlMarginStart = a.getDimensionPixelSize(R.styleable.RCETextEditorView_rce_controls_margin_start, 0)
            val controlMarginEnd = a.getDimensionPixelSize(R.styleable.RCETextEditorView_rce_controls_margin_end, 0)

            (controller.layoutParams as ViewGroup.MarginLayoutParams).apply {
                marginStart = controlMarginStart
                marginEnd = controlMarginEnd
            }

            (bottomDivider.layoutParams as ViewGroup.MarginLayoutParams).apply {
                marginStart = controlMarginStart
                marginEnd = controlMarginEnd
            }

            (colorPickerView.layoutParams as ViewGroup.MarginLayoutParams).apply {
                marginStart = controlMarginStart
                marginEnd = controlMarginEnd
            }

            val controlsVisible = a.getBoolean(R.styleable.RCETextEditorView_rce_controls_visible, true)
            if (controlsVisible) showEditorToolbar() else hideEditorToolbar()
            a.recycle()
        }

        val viewColorMap = mapOf(
            rce_colorPickerWhite to R.color.rce_pickerWhite,
            rce_colorPickerBlack to R.color.rce_pickerBlack,
            rce_colorPickerGray to R.color.rce_pickerGray,
            rce_colorPickerRed to R.color.rce_pickerRed,
            rce_colorPickerOrange to R.color.rce_pickerOrange,
            rce_colorPickerYellow to R.color.rce_pickerYellow,
            rce_colorPickerGreen to R.color.rce_pickerGreen,
            rce_colorPickerBlue to R.color.rce_pickerBlue,
            rce_colorPickerPurple to R.color.rce_pickerPurple
        )

        viewColorMap.forEach { (view, colorRes) ->
            view.setOnClickListener {
                editor?.setTextColor(ContextCompat.getColor(context, colorRes))
                toggleColorPicker()
            }
        }

        actionTypeButtonMap = mapOf(
            "BOLD" to action_bold,
            "ITALIC" to action_italic,
            "UNDERLINE" to action_underline,
            "UNORDEREDLIST" to action_insert_bullets,
            "ORDEREDLIST" to action_insert_numbers
        )

        // Update formatting states after the block
        val postUpdateState = { block: () -> Unit ->
            block()
            editor.evaluateJavascript("javascript:RE.enabledEditingItems();", null)
        }

        action_txt_color.setOnClickListener { toggleColorPicker() }
        action_undo.setOnClickListener { postUpdateState { editor.undo() } }
        action_redo.setOnClickListener { postUpdateState { editor.redo() } }
        action_bold.setOnClickListener { postUpdateState { editor.setBold() } }
        action_italic.setOnClickListener { postUpdateState { editor.setItalic() } }
        action_underline.setOnClickListener { postUpdateState { editor.setUnderline() } }
        action_insert_bullets.setOnClickListener { postUpdateState { editor.setBullets() } }
        action_insert_numbers.setOnClickListener { postUpdateState { editor.setNumbers() } }

        actionUploadImage.setOnClickListener {
            actionUploadImageCallback?.invoke()
        }

        action_insert_link.setOnClickListener {
            RCEInsertDialog.newInstance(context.getString(R.string.rce_insertLink), themeColor, buttonColor, true)
                .setListener { url, alt ->
                    if (URLUtil.isValidUrl(url)) { // Checks if the url contains any valid schema, etc
                        editor.insertLink(url, alt)
                    } else {
                        // For now, we'll default to https always
                        editor.insertLink("https://$url", alt)
                    }
                }
                .show(fragmentManager ?: return@setOnClickListener, RCEInsertDialog::class.java.simpleName)
        }

        editor.setOnDecorationChangeListener { state, _ ->
            if (!isToolbarVisible()) showEditorToolbar()
            actionTypeButtonMap.values.forEach {
                it?.setColorFilter(Color.BLACK)
                it?.setBackgroundColor(Color.TRANSPARENT)
            }
            state.split(',').forEach {
                actionTypeButtonMap[it]?.setColorFilter(Color.WHITE)
                actionTypeButtonMap[it]?.setBackgroundColor(Color.BLACK)
            }
        }

        // Update formatting states when text changes
        editor.setOnTextChangeListener { postUpdateState {} }
        val heightInPixels = (resources.getDimension(R.dimen.rce_view_min_height)).toInt()
        editor.setEditorHeight(heightInPixels)
        if (resources.getBoolean(R.bool.isRtl))
            editor.setupRtl()
    }

    fun setOnTextChangeListener(callback: (String) -> Unit) {
        editor.setOnTextChangeListener {
            callback.invoke(it)
        }
    }

    fun setPaddingOnEditor(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0) {
        editor.setPadding(left, top, right, bottom)
    }

    fun insertImage(url: String, alt: String) {
        editor.insertImage(url, alt)
    }

    fun setHtml(
        html: String?,
        accessibilityTitle: String,
        hint: String,
        @ColorInt themeColor: Int,
        @ColorInt buttonColor: Int
    ) {
        editor.applyHtml(html.orEmpty(), accessibilityTitle)
        editor.setPlaceholder(hint)
        setThemeColor(themeColor)
        this.buttonColor = buttonColor
    }

    fun setHint(hint: String) = editor.setPlaceholder(hint)

    fun setHint(@StringRes hint: Int) = editor.setPlaceholder(context.getString(hint))

    fun hideEditorToolbar() {
        controller.visibility = View.GONE
        bottomDivider.visibility = View.GONE
        colorPickerView.visibility = View.GONE
    }

    fun showEditorToolbar() {
        controller.visibility = View.VISIBLE
        bottomDivider.visibility = View.VISIBLE
    }

    private fun isToolbarVisible(): Boolean = controller.visibility == View.VISIBLE

    /**
     * Takes care of making the label darker or lighter depending on when it's focused
     * @param label TextView label that is usually above the RCE view
     * @param focusedColor Color you want the label to be when focused
     * @param defaultColor Color you want the label to be when unfocused
     */
    fun setLabel(label: TextView, focusedColor: Int, defaultColor: Int) {
        editor.onFocusChangeListener = OnFocusChangeListener { _, focused ->
            label.setTextColor(ContextCompat.getColor(context, if (focused) focusedColor else defaultColor))
        }
    }

    fun setThemeColor(@ColorInt color: Int) {
        themeColor = color
    }

    private fun toggleColorPicker() {
        if (colorPickerView.visibility == View.VISIBLE) {
            val animator = ObjectAnimator.ofFloat(colorPickerView, "translationY", colorPickerView.height * -1f, 0f)
            animator.duration = 200
            animator.addListener(object : RCEAnimationListener() {
                override fun onAnimationFinish(animation: Animator) {
                    colorPickerView.visibility = View.INVISIBLE
                }
            })
            animator.start()
        } else {
            val animator = ObjectAnimator.ofFloat(colorPickerView, "translationY", 0f, colorPickerView.height * -1f)
            animator.duration = 230
            animator.addListener(object : RCEAnimationListener() {
                override fun onAnimationBegin(animation: Animator) {
                    colorPickerView.post {
                        colorPickerView.visibility = View.VISIBLE
                        rce_colorPickerWhite.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                    }
                }
            })
            animator.start()
        }
    }

    interface ExitDialogCallback {
        fun onPositive()
        fun onNegative()
    }

    fun showExitDialog(buttonColor: Int, callback: ExitDialogCallback?) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.rce_dialog_exit_title)
        builder.setMessage(R.string.rce_dialog_exit_message)
        builder.setPositiveButton(R.string.rce_exit) { dialog, _ ->
            dialog.dismiss()
            callback?.onPositive()
        }
        builder.setNegativeButton(R.string.rce_cancel) { dialog, _ ->
            dialog.dismiss()
            callback?.onNegative()
        }

        val alertDialog = builder.create()
        alertDialog.setOnShowListener {
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(buttonColor)
            alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(buttonColor)
        }
        alertDialog.show()
    }

    fun requestEditorFocus() = editor.focusEditor()

    //region save and restore state

    public override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState!!)

        ss.htmlState = html
        ss.contentDescription = editor.accessibilityContentDescription
        ss.themeColor = themeColor
        ss.buttonColor = buttonColor

        return ss
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)

        setHtml(state.htmlState, state.contentDescription, "", state.themeColor, state.buttonColor)
    }

    private class SavedState : View.BaseSavedState {
        internal var htmlState: String? = null
        internal var contentDescription: String = ""
        internal var themeColor = Color.BLACK
        internal var buttonColor = Color.BLACK

        internal constructor(superState: Parcelable) : super(superState)

        private constructor(`in`: Parcel) : super(`in`) {
            this.htmlState = `in`.readString()
            this.contentDescription = `in`.readString() ?: ""
            this.themeColor = `in`.readInt()
            this.buttonColor = `in`.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeString(this.htmlState)
            out.writeString(this.contentDescription)
            out.writeInt(this.themeColor)
            out.writeInt(this.buttonColor)
        }

        companion object {

            // required field that makes Parcelables from a Parcel
            @JvmField val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    //endregion
}
