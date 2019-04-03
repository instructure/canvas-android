package com.instructure.pandautils.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.instructure.pandautils.utils.ThemePrefs

class InstAlertDialog : AppCompatDialogFragment() {

    private var listener: ((Int) -> Unit)? = null

    init {
        retainInstance = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        arguments?.getString(KEY_TITLE, null)?.let { builder.setTitle(it) }
        arguments?.getString(KEY_MESSAGE, null)?.let { builder.setMessage(it) }
        arguments?.getString(KEY_BUTTON_POSITIVE, null)?.let {
            builder.setPositiveButton(it, ::dialogClicked)
        }
        arguments?.getString(KEY_BUTTON_NEUTRAL, null)?.let {
            builder.setNeutralButton(it, ::dialogClicked)
        }
        arguments?.getString(KEY_BUTTON_NEGATIVE, null)?.let {
            builder.setNegativeButton(it, ::dialogClicked)
        }

        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ThemePrefs.buttonColor)
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.setTextColor(ThemePrefs.buttonColor)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ThemePrefs.buttonColor)
        }

        return dialog
    }

    fun show(manager: FragmentManager, callback: ((which: Int) -> Unit)? = null) {
        listener = callback
        super.show(manager, InstAlertDialog::class.java.simpleName)
    }

    private fun dialogClicked(dialog: DialogInterface, which: Int) {
        listener?.invoke(which)
        dialog.dismiss()
    }

    companion object {

        private const val KEY_TITLE = "title"
        private const val KEY_MESSAGE = "message"
        private const val KEY_BUTTON_POSITIVE = "positive"
        private const val KEY_BUTTON_NEUTRAL = "neutral"
        private const val KEY_BUTTON_NEGATIVE = "negative"

        fun newInstance(context: Context, title: Int? = null, message: Int? = null, buttonPositive: Int? = null, buttonNeutral: Int? = null, buttonNegative: Int? = null): InstAlertDialog {
            val dialog = InstAlertDialog()
            context.apply {
                dialog.arguments = getBundle(
                        title?.let { getString(it) },
                        message?.let { getString(it) },
                        buttonPositive?.let { getString(it) },
                        buttonNeutral?.let { getString(it) },
                        buttonNegative?.let { getString(it) }
                )
            }
            return dialog
        }

        fun newInstance(title: String? = null, message: String? = null, buttonPositive: String? = null, buttonNeutral: String? = null, buttonNegative: String? = null): InstAlertDialog {
            val dialog = InstAlertDialog()
            dialog.arguments = getBundle(title, message, buttonPositive, buttonNeutral, buttonNegative)
            return dialog
        }

        private fun getBundle(title: String?, message: String?, buttonPositive: String?, buttonNeutral: String?, buttonNegative: String?): Bundle {
            return Bundle().apply {
                title?.let { putString(KEY_TITLE, it) }
                message?.let { putString(KEY_MESSAGE, it) }
                buttonPositive?.let { putString(KEY_BUTTON_POSITIVE, it) }
                buttonNeutral?.let { putString(KEY_BUTTON_NEUTRAL, it) }
                buttonNegative?.let { putString(KEY_BUTTON_NEGATIVE, it) }
            }
        }
    }
}
