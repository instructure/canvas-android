package com.instructure.parentapp.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.instructure.pandautils.utils.onTextChanged
import com.instructure.parentapp.R
import kotlinx.android.synthetic.main.activity_settings.*

class PairChildDialogFragment : DialogFragment() {

    private lateinit var pairChildListener: PairChildListener

    interface PairChildListener {
        fun pairChild(pairingCode: String)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        pairChildListener = context as? PairChildListener ?: throw ClassCastException("$context must implement ${PairChildListener::class.java.simpleName}")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_student_dialog, settingsActivityRoot, false)
        val editText = dialogView.findViewById<EditText>(R.id.addChildEditText)

        // Launch dialog
        val dialog = AlertDialog.Builder(requireContext(), R.style.Theme_AppCompat_Light_Dialog_Alert_addChildDialog)
                .setTitle(R.string.addChildTitle)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    pairChildListener.pairChild(editText.text.toString())
                }.setNegativeButton(android.R.string.cancel, null)
                .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = editText.text.isNotEmpty()
        }

        editText?.onTextChanged { text ->
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = text.isNotEmpty()
        }

        return dialog
    }
}