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
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import instructure.rceditor.RCEConst.BUTTON_COLOR
import instructure.rceditor.RCEConst.HTML_ACCESSIBILITY_TITLE
import instructure.rceditor.RCEConst.HTML_CONTENT
import instructure.rceditor.RCEConst.HTML_RESULT
import instructure.rceditor.RCEConst.HTML_TITLE
import instructure.rceditor.RCEConst.THEME_COLOR
import kotlinx.android.synthetic.main.rce_color_picker.*
import kotlinx.android.synthetic.main.rce_controller.*
import kotlinx.android.synthetic.main.rce_fragment_layout.*

class RCEFragment : Fragment() {

    private var callback: RCEFragmentCallbacks? = null

    private val onColorChosen = View.OnClickListener { v ->
        when (v.id) {
            R.id.rce_colorPickerWhite -> rcEditor.setTextColor(Color.WHITE)
            R.id.rce_colorPickerBlack -> rcEditor.setTextColor(Color.BLACK)
            R.id.rce_colorPickerGray -> rcEditor.setTextColor(ContextCompat.getColor(requireContext(), R.color.rce_pickerGray))
            R.id.rce_colorPickerRed -> rcEditor.setTextColor(ContextCompat.getColor(requireContext(), R.color.rce_pickerRed))
            R.id.rce_colorPickerOrange -> rcEditor.setTextColor(ContextCompat.getColor(requireContext(), R.color.rce_pickerOrange))
            R.id.rce_colorPickerYellow -> rcEditor.setTextColor(ContextCompat.getColor(requireContext(), R.color.rce_pickerYellow))
            R.id.rce_colorPickerGreen -> rcEditor.setTextColor(ContextCompat.getColor(requireContext(), R.color.rce_pickerGreen))
            R.id.rce_colorPickerBlue -> rcEditor.setTextColor(ContextCompat.getColor(requireContext(), R.color.rce_pickerBlue))
            R.id.rce_colorPickerPurple -> rcEditor.setTextColor(ContextCompat.getColor(requireContext(), R.color.rce_pickerPurple))
        }

        toggleColorPicker()
    }

    private val onTextColor = View.OnClickListener { toggleColorPicker() }
    private val onUndo = View.OnClickListener { rcEditor.undo() }
    private val onRedo = View.OnClickListener { rcEditor.redo() }
    private val onBold = View.OnClickListener { rcEditor.setBold() }
    private val onItalic = View.OnClickListener { rcEditor.setItalic() }
    private val onUnderline = View.OnClickListener { rcEditor.setUnderline() }
    private val onInsertBulletList = View.OnClickListener { rcEditor.setBullets() }

    private val onUploadPicture = View.OnClickListener {
        val dialog = RCEInsertDialog.newInstance(
                getString(R.string.rce_insertImage),
                requireArguments().getInt(THEME_COLOR, Color.BLACK),
                requireArguments().getInt(BUTTON_COLOR, Color.BLACK))
        dialog.setListener { url, alt -> rcEditor.insertImage(url, alt) }.show(requireFragmentManager(), RCEInsertDialog::class.java.simpleName)
    }

    private val onInsertLink = View.OnClickListener {
        val dialog = RCEInsertDialog.newInstance(
                getString(R.string.rce_insertLink),
                requireArguments().getInt(THEME_COLOR, Color.BLACK),
                requireArguments().getInt(BUTTON_COLOR, Color.BLACK))
        dialog.setListener { url, alt -> rcEditor.insertLink(url, alt) }.show(requireFragmentManager(), RCEInsertDialog::class.java.simpleName)
    }

    interface RCEFragmentCallbacks {
        fun onResult(activityResult: Int, data: Intent?)
    }

    init {
        if (arguments == null) {
            arguments = Bundle()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is RCEFragmentCallbacks) {
            callback = context
        } else {
            throw IllegalStateException("Context must implement RCEFragment.RCEFragmentCallbacks()")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.rce_fragment_layout, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        rcEditor.setPadding(10, 10, 10, 10)
        rcEditor.applyHtml(
                requireArguments().getString(HTML_CONTENT) ?: "",
                requireArguments().getString(HTML_ACCESSIBILITY_TITLE) ?: "")

        with(rceToolbar) {
            title = requireArguments().getString(HTML_TITLE)
            inflateMenu(R.menu.rce_save_menu)
            setNavigationIcon(R.drawable.ic_rce_cancel)
            setNavigationContentDescription(R.string.rce_cancel)
            setNavigationOnClickListener(View.OnClickListener {
                // Check to see if we made any changes. If we haven't, just close the fragment
                if (rcEditor.html != null && requireArguments().getString(HTML_CONTENT) != null) {
                    if (rcEditor.html == requireArguments().getString(HTML_CONTENT)) {
                        callback?.onResult(RESULT_CANCELED, null)
                        return@OnClickListener
                    }
                }
                showExitDialog()
            })

            setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
                if (item.itemId == R.id.rce_save) {
                    val data = Intent()
                    data.putExtra(HTML_RESULT, if (rcEditor.html == null)
                        requireArguments().getString(HTML_CONTENT)
                    else
                        rcEditor.html)
                    callback?.onResult(RESULT_OK, data)
                    return@OnMenuItemClickListener true
                }
                false
            })
        }

        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.rce_dimStatusBarGray)
        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        rce_colorPickerWhite.setOnClickListener(onColorChosen)
        rce_colorPickerBlack.setOnClickListener(onColorChosen)
        rce_colorPickerGray.setOnClickListener(onColorChosen)
        rce_colorPickerRed.setOnClickListener(onColorChosen)
        rce_colorPickerOrange.setOnClickListener(onColorChosen)
        rce_colorPickerYellow.setOnClickListener(onColorChosen)
        rce_colorPickerGreen.setOnClickListener(onColorChosen)
        rce_colorPickerBlue.setOnClickListener(onColorChosen)
        rce_colorPickerPurple.setOnClickListener(onColorChosen)

        action_undo.setOnClickListener(onUndo)
        action_redo.setOnClickListener(onRedo)
        action_bold.setOnClickListener(onBold)
        action_italic.setOnClickListener(onItalic)
        action_underline.setOnClickListener(onUnderline)
        action_insert_bullets.setOnClickListener(onInsertBulletList)
        actionUploadImage.setOnClickListener(onUploadPicture)
        action_insert_link.setOnClickListener(onInsertLink)
        action_txt_color.setOnClickListener(onTextColor)
    }

    private fun toggleColorPicker() {
        if (rceColorPickerWrapper.visibility == View.VISIBLE) {
            val animator = ObjectAnimator.ofFloat(rceColorPickerWrapper, "translationY", rceColorPickerWrapper.height * -1f, 0f)
            animator.duration = 200
            animator.addListener(object : RCEAnimationListener() {
                override fun onAnimationFinish(animation: Animator) {
                    rceColorPickerWrapper?.visibility = View.INVISIBLE
                }
            })
            animator.start()
        } else {
            val animator = ObjectAnimator.ofFloat(rceColorPickerWrapper, "translationY", 0f, rceColorPickerWrapper!!.height * -1f)
            animator.duration = 230
            animator.addListener(object : RCEAnimationListener() {
                override fun onAnimationBegin(animation: Animator) {
                    rceColorPickerWrapper?.post { rceColorPickerWrapper?.visibility = View.VISIBLE }
                }
            })
            animator.start()
        }
    }

    fun loadArguments(html: String?, title: String?, accessibilityTitle: String?, @ColorInt themeColor: Int, @ColorInt buttonColor: Int) {
        with (requireArguments()) {
            putString(HTML_CONTENT, html)
            putString(HTML_TITLE, title)
            putString(HTML_ACCESSIBILITY_TITLE, accessibilityTitle)
            putInt(THEME_COLOR, themeColor)
            putInt(BUTTON_COLOR, buttonColor)
        }
    }

    fun showExitDialog() {
        AlertDialog.Builder(requireContext())
                .setTitle(R.string.rce_dialog_exit_title)
                .setMessage(R.string.rce_dialog_exit_message)
                .setPositiveButton(R.string.rce_exit) { dialog, _ ->
                    dialog.dismiss()
                    callback?.onResult(RESULT_CANCELED, null)
                }
                .setNegativeButton(R.string.rce_cancel) { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
    }

    companion object {

        fun newInstance(html: String, title: String, accessibilityTitle: String, @ColorInt themeColor: Int, @ColorInt buttonColor: Int): RCEFragment {
            val fragment = RCEFragment()
            fragment.arguments = makeBundle(html, title, accessibilityTitle, themeColor, buttonColor)
            return fragment
        }

        fun newInstance(args: Bundle): RCEFragment {
            val fragment = RCEFragment()
            fragment.arguments = args
            return fragment
        }

        fun makeBundle(html: String, title: String, accessibilityTitle: String, @ColorInt themeColor: Int, @ColorInt buttonColor: Int): Bundle {
            val args = Bundle()
            args.putString(HTML_CONTENT, html)
            args.putString(HTML_TITLE, title)
            args.putString(HTML_ACCESSIBILITY_TITLE, accessibilityTitle)
            args.putInt(THEME_COLOR, themeColor)
            args.putInt(BUTTON_COLOR, buttonColor)
            return args
        }
    }
}
