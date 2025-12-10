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
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.webkit.URLUtil
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import instructure.rceditor.databinding.RceDialogAltTextBinding
import instructure.rceditor.databinding.RceTextEditorViewBinding

@Suppress("unused")
class RCETextEditorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val binding: RceTextEditorViewBinding
    
    @ColorInt
    private var themeColor = Color.BLACK

    @ColorInt
    private var buttonColor = Color.BLACK

    private var actionTypeButtonMap: Map<String, ImageButton?>

    val html: String get() = binding.rceWebView.html.orEmpty()

    private val fragmentManager get() = context.findFragmentActivity()?.supportFragmentManager

    private fun Context.findFragmentActivity(): FragmentActivity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is FragmentActivity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }

    // Let the fragments that use this view handle what to do when it's clicked
    var actionUploadImageCallback: (() -> Unit)? = null

    init {
        binding = RceTextEditorViewBinding.inflate(LayoutInflater.from(context), this, true)

        with(binding) {
            if (attrs != null) {
                val a = getContext().obtainStyledAttributes(attrs, R.styleable.RCETextEditorView, 0, 0)
                fun Float.pxToDp() = (this / Resources.getSystem().displayMetrics.density).toInt()
                if (a.hasValue(R.styleable.RCETextEditorView_rce_editor_padding)) {
                    val editorPadding = a.getDimension(R.styleable.RCETextEditorView_rce_editor_padding, 0f).pxToDp()
                    rceWebView.setPadding(editorPadding, editorPadding, editorPadding, editorPadding)
                } else {
                    val padStart = a.getDimension(R.styleable.RCETextEditorView_rce_editor_padding_start, 0f).pxToDp()
                    val padEnd = a.getDimension(R.styleable.RCETextEditorView_rce_editor_padding_end, 0f).pxToDp()
                    val padTop = a.getDimension(R.styleable.RCETextEditorView_rce_editor_padding_top, 0f).pxToDp()
                    val pagBottom = a.getDimension(R.styleable.RCETextEditorView_rce_editor_padding_bottom, 0f).pxToDp()
                    rceWebView.setPadding(padStart, padTop, padEnd, pagBottom)
                }

                val controlMarginStart = a.getDimensionPixelSize(R.styleable.RCETextEditorView_rce_controls_margin_start, 0)
                val controlMarginEnd = a.getDimensionPixelSize(R.styleable.RCETextEditorView_rce_controls_margin_end, 0)

                (rceControllerWrapper.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    marginStart = controlMarginStart
                    marginEnd = controlMarginEnd
                }

                (rceBottomDivider.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    marginStart = controlMarginStart
                    marginEnd = controlMarginEnd
                }

                (rceColorPickerWrapper.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    marginStart = controlMarginStart
                    marginEnd = controlMarginEnd
                }

                val controlsVisible = a.getBoolean(R.styleable.RCETextEditorView_rce_controls_visible, true)
                if (controlsVisible) showEditorToolbar() else hideEditorToolbar()
                a.recycle()
            }

            val viewColorMap = mapOf(
                rceColorPicker.rceColorPickerWhite to R.color.rce_pickerWhite,
                rceColorPicker.rceColorPickerBlack to R.color.rce_pickerBlack,
                rceColorPicker.rceColorPickerGray to R.color.rce_pickerGray,
                rceColorPicker.rceColorPickerRed to R.color.rce_pickerRed,
                rceColorPicker.rceColorPickerOrange to R.color.rce_pickerOrange,
                rceColorPicker.rceColorPickerYellow to R.color.rce_pickerYellow,
                rceColorPicker.rceColorPickerGreen to R.color.rce_pickerGreen,
                rceColorPicker.rceColorPickerBlue to R.color.rce_pickerBlue,
                rceColorPicker.rceColorPickerPurple to R.color.rce_pickerPurple
            )

            viewColorMap.forEach { (view, colorRes) ->
                view.setOnClickListener {
                    rceWebView.setTextColor(ContextCompat.getColor(context, colorRes))
                    toggleColorPicker()
                }
            }

            actionTypeButtonMap = mapOf(
                "BOLD" to rceController.actionBold,
                "ITALIC" to rceController.actionItalic,
                "UNDERLINE" to rceController.actionUnderline,
                "UNORDEREDLIST" to rceController.actionInsertBullets,
                "ORDEREDLIST" to rceController.actionInsertNumbers
            )

            // Update formatting states after the block
            val postUpdateState = { block: () -> Unit ->
                block()
                rceWebView.evaluateJavascript("javascript:RE.enabledEditingItems();", null)
            }

            rceController.actionTxtColor.setOnClickListener { toggleColorPicker() }
            rceController.actionUndo.setOnClickListener { postUpdateState { rceWebView.undo() } }
            rceController.actionRedo.setOnClickListener { postUpdateState { rceWebView.redo() } }
            rceController.actionBold.setOnClickListener { postUpdateState { rceWebView.setBold() } }
            rceController.actionItalic.setOnClickListener { postUpdateState { rceWebView.setItalic() } }
            rceController.actionUnderline.setOnClickListener { postUpdateState { rceWebView.setUnderline() } }
            rceController.actionInsertBullets.setOnClickListener { postUpdateState { rceWebView.setBullets() } }
            rceController.actionInsertNumbers.setOnClickListener { postUpdateState { rceWebView.setNumbers() } }

            rceController.actionUploadImage.setOnClickListener {
                actionUploadImageCallback?.invoke()
            }

            rceController.actionInsertLink.setOnClickListener {
                rceWebView.getSelectedText {
                    RCEInsertDialog.newInstance(context.getString(R.string.rce_insertLink), themeColor, buttonColor, true, it)
                        .setListener { url, alt ->
                            if (URLUtil.isValidUrl(url)) { // Checks if the url contains any valid schema, etc
                                rceWebView.insertLink(url, alt)
                            } else {
                                // For now, we'll default to https always
                                rceWebView.insertLink("https://$url", alt)
                            }
                        }
                        .show(fragmentManager ?: return@getSelectedText, RCEInsertDialog::class.java.simpleName)
                }
            }

            rceWebView.setOnDecorationChangeListener { state, _ ->
                if (!isToolbarVisible()) showEditorToolbar()
                actionTypeButtonMap.values.forEach {
                    it?.setColorFilter(context.getColor(R.color.rce_defaultTextColor))
                    it?.setBackgroundColor(Color.TRANSPARENT)
                }
                state.split(',').forEach {
                    actionTypeButtonMap[it]?.setColorFilter(Color.WHITE)
                    actionTypeButtonMap[it]?.setBackgroundColor(Color.BLACK)
                }
            }

            // Update formatting states when text changes
            rceWebView.setOnTextChangeListener { postUpdateState {} }
            val heightInPixels = (resources.getDimension(R.dimen.rce_view_min_height)).toInt()
            rceWebView.setEditorHeight(heightInPixels)
            if (resources.getBoolean(R.bool.isRtl))
                rceWebView.setupRtl()
        }
    }

    fun setOnTextChangeListener(callback: (String) -> Unit) {
        binding.rceWebView.setOnTextChangeListener {
            callback.invoke(it)
        }
    }

    fun setPaddingOnEditor(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0) {
        binding.rceWebView.setPadding(left, top, right, bottom)
    }

    fun insertImage(activity: Activity, imageUrl: String) = with(binding) {
        showAltTextDialog(activity, { altText ->
            rceWebView.insertImage(imageUrl, altText)
        }, {
            rceWebView.insertImage(imageUrl, "")
        })
    }

    fun insertImage(url: String, alt: String) {
        binding.rceWebView.insertImage(url, alt)
    }

    private fun showAltTextDialog(activity: Activity, onPositiveClick: (String) -> Unit, onNegativeClick: () -> Unit) {
        val dialogBinding = RceDialogAltTextBinding.inflate(LayoutInflater.from(context), null, false)
        val altTextInput = dialogBinding.altText

        var buttonClicked = false

        val altTextDialog = AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.rce_dialogAltText))
            .setView(dialogBinding.root)
            .setPositiveButton(activity.getString(android.R.string.ok)) { _, _ ->
                buttonClicked = true
                onPositiveClick(altTextInput.text.toString())
            }
            .setNegativeButton(activity.getString(android.R.string.cancel)) { _, _ ->
                buttonClicked = true
                onNegativeClick()
            }
            .setOnDismissListener {
                if (!buttonClicked) {
                    onNegativeClick()
                }
            }
            .create().apply {
                setOnShowListener {
                    (it as? AlertDialog)?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = false
                }
            }

        altTextInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                altTextDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !s.isNullOrEmpty()
            }
        })

        altTextDialog.show()
    }

    fun setHtml(
        html: String?,
        accessibilityTitle: String,
        hint: String,
        @ColorInt themeColor: Int,
        @ColorInt dialogButtonsColor: Int
    ) {
        binding.rceWebView.applyHtml(html.orEmpty(), accessibilityTitle)
        binding.rceWebView.setPlaceholder(hint)
        this.themeColor = themeColor
        this.buttonColor = dialogButtonsColor
    }

    fun setHint(hint: String) = binding.rceWebView.setPlaceholder(hint)

    fun setHint(@StringRes hint: Int) = binding.rceWebView.setPlaceholder(context.getString(hint))

    fun hideEditorToolbar() = with(binding) {
        rceControllerWrapper.visibility = View.GONE
        rceBottomDivider.visibility = View.GONE
        rceColorPickerWrapper.visibility = View.GONE
    }

    fun showEditorToolbar() = with(binding) {
        rceControllerWrapper.visibility = View.VISIBLE
        rceBottomDivider.visibility = View.VISIBLE
    }

    private fun isToolbarVisible(): Boolean = binding.rceControllerWrapper.visibility == View.VISIBLE

    /**
     * Takes care of making the label darker or lighter depending on when it's focused
     * @param label TextView label that is usually above the RCE view
     * @param focusedColor Color you want the label to be when focused
     * @param defaultColor Color you want the label to be when unfocused
     */
    fun setLabel(label: TextView, focusedColor: Int, defaultColor: Int) {
        binding.rceWebView.onFocusChangeListener = OnFocusChangeListener { _, focused ->
            label.setTextColor(ContextCompat.getColor(context, if (focused) focusedColor else defaultColor))
        }
    }

    private fun toggleColorPicker() = with(binding) {
        if (rceColorPickerWrapper.visibility == View.VISIBLE) {
            val animator = ObjectAnimator.ofFloat(rceColorPickerWrapper, "translationY", rceColorPickerWrapper.height * -1f, 0f)
            animator.duration = 200
            animator.addListener(object : RCEAnimationListener() {
                override fun onAnimationFinish(animation: Animator) {
                    rceColorPickerWrapper.visibility = View.INVISIBLE
                }
            })
            animator.start()
        } else {
            val animator = ObjectAnimator.ofFloat(rceColorPickerWrapper, "translationY", 0f, rceColorPickerWrapper.height * -1f)
            animator.duration = 230
            animator.addListener(object : RCEAnimationListener() {
                override fun onAnimationBegin(animation: Animator) {
                    rceColorPickerWrapper.post {
                        rceColorPickerWrapper.visibility = View.VISIBLE
                        rceColorPicker.rceColorPickerWhite.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
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

    fun requestEditorFocus() = binding.rceWebView.focusEditor()

    //region save and restore state

    public override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState!!)

        ss.htmlState = html
        ss.contentDescription = binding.rceWebView.accessibilityContentDescription
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
