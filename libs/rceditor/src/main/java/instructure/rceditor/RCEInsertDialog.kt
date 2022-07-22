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

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.RestrictTo
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.widget.AppCompatEditText
import instructure.rceditor.RCEUtils.increaseAlpha
import instructure.rceditor.RCEUtils.makeEditTextColorStateList

@RestrictTo(RestrictTo.Scope.LIBRARY)
class RCEInsertDialog : AppCompatDialogFragment() {

    private lateinit var urlEditText: AppCompatEditText
    private lateinit var altEditText: AppCompatEditText
    private var callback: ((url: String, alt: String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val root = LayoutInflater.from(context).inflate(R.layout.rce_dialog_insert, null)
        val errorText = root.findViewById<TextView>(R.id.errorMessage)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(root)
        builder.setTitle(arguments?.getString(TITLE))
        builder.setPositiveButton(R.string.rce_dialogDone, null) // Override listener in onShow
        builder.setNegativeButton(R.string.rce_dialogCancel) { _, _ -> dismiss() }
        val defaultColor = context?.getColor(R.color.rce_defaultTextColor) ?: Color.BLACK
        val themeColor = arguments?.getInt(THEME_COLOR, defaultColor) ?: defaultColor
        val highlightColor = increaseAlpha(themeColor)
        val colorStateList = makeEditTextColorStateList(defaultColor, themeColor)
        altEditText = root.findViewById(R.id.altEditText)
        urlEditText = root.findViewById(R.id.urlEditText)
        altEditText.highlightColor = highlightColor
        urlEditText.highlightColor = highlightColor
        altEditText.supportBackgroundTintList = colorStateList
        urlEditText.supportBackgroundTintList = colorStateList
        val dialog = builder.create()
        dialog.setOnShowListener {
            val buttonColor = arguments?.getInt(BUTTON_COLOR, defaultColor) ?: defaultColor
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(buttonColor)
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(buttonColor)
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            // Override onClick here to prevent dismissing during error checks
            button.setOnClickListener {
                if (callback != null) {
                    val isVerifyUrl = arguments?.getBoolean(VERIFY_URL, false) ?: false
                    val url = urlEditText.text.toString()
                    val alt = altEditText.text.toString()
                    if (isVerifyUrl) {
                        when {
                            url.isEmpty() -> {
                                errorText.text = getString(R.string.rce_emptyUrlError)
                                errorText.visibility = View.VISIBLE
                            }
                            url.contains("http://") -> {
                                errorText.text = getString(R.string.rce_httpNotAllowed)
                                errorText.visibility = View.VISIBLE
                            }
                            else -> {
                                callback?.invoke(url, alt)
                                dismiss()
                            }
                        }
                    } else {
                        callback?.invoke(url, alt)
                        dismiss()
                    }
                } else {
                    dismiss()
                }
            }
        }
        return dialog
    }

    fun setListener(callback: (url: String, alt: String) -> Unit): RCEInsertDialog {
        this.callback = callback
        return this
    }

    companion object {
        private const val TITLE = "title"
        private const val THEME_COLOR = "theme_color"
        private const val BUTTON_COLOR = "button_color"
        private const val VERIFY_URL = "verify_url"

        fun newInstance(title: String?, @ColorInt themeColor: Int, @ColorInt buttonColor: Int): RCEInsertDialog {
            val dialog = RCEInsertDialog()
            val args = Bundle()
            args.putString(TITLE, title)
            args.putInt(THEME_COLOR, themeColor)
            args.putInt(BUTTON_COLOR, buttonColor)
            dialog.arguments = args
            return dialog
        }

        fun newInstance(title: String?, @ColorInt themeColor: Int, @ColorInt buttonColor: Int, isVerifyUrl: Boolean): RCEInsertDialog {
            val dialog = RCEInsertDialog()
            val args = Bundle()
            args.putString(TITLE, title)
            args.putInt(THEME_COLOR, themeColor)
            args.putInt(BUTTON_COLOR, buttonColor)
            args.putBoolean(VERIFY_URL, isVerifyUrl)
            dialog.arguments = args
            return dialog
        }
    }
}
