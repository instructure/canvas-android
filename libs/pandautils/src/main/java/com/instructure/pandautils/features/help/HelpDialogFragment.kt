/*
 * Copyright (C) 2021 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.pandautils.features.help

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.instructure.pandautils.base.BaseCanvasDialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrl
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.SCREEN_VIEW_HELP
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.databinding.HelpDialogBinding
import com.instructure.pandautils.mvvm.Event
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@PageView(url = "help")
@ScreenView(SCREEN_VIEW_HELP)
@AndroidEntryPoint
class HelpDialogFragment : BaseCanvasDialogFragment() {

    private val viewModel: HelpDialogViewModel by viewModels()

    @Inject
    lateinit var helpDialogFragmentBehavior: HelpDialogFragmentBehavior

    @Suppress("unused")
    @PageViewUrl
    fun makePageViewUrl() = "help.instructure.com"

    @SuppressLint("InflateParams") // Suppress lint warning about null parent when inflating layout
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.AccessibleAlertDialog).setTitle(requireContext().getString(R.string.help))

        val binding = HelpDialogBinding.inflate(LayoutInflater.from(context))
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        builder.setView(binding.root)

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        viewModel.events.observe(this, Observer { event: Event<HelpDialogAction>? ->
            event?.getContentIfNotHandled()?.let { action: HelpDialogAction ->
                handleAction(action)
            }
        })

        return dialog
    }

    private fun handleAction(action: HelpDialogAction) {
        when (action) {
            is HelpDialogAction.ReportProblem -> helpDialogFragmentBehavior.reportProblem()
            is HelpDialogAction.RateTheApp -> helpDialogFragmentBehavior.rateTheApp()
            is HelpDialogAction.AskInstructor -> helpDialogFragmentBehavior.askInstructor()
            is HelpDialogAction.Phone -> {
                // Support phone links: https://community.canvaslms.com/docs/DOC-12664-4214610054
                val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse(action.url) }
                startActivity(intent)
            }

            is HelpDialogAction.SendMail -> {
                // Support mailto links: https://community.canvaslms.com/docs/DOC-12664-4214610054
                val intent = Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse(action.url) }
                startActivity(intent)
            }

            is HelpDialogAction.OpenExternalBrowser -> {
                // Chat with Canvas Support - Doesn't seem work properly with WebViews, so we kick it out
                // to the external browser
                val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(action.url) }
                startActivity(intent)
            }
            // External URL
            is HelpDialogAction.OpenWebView -> helpDialogFragmentBehavior.openWebView(action.url, action.title)
        }
    }

    override fun onDestroyView() {
        dialog?.setDismissMessage(null)
        super.onDestroyView()
    }

    companion object {
        const val TAG = "helpDialog"

        fun show(activity: FragmentActivity): HelpDialogFragment =
            HelpDialogFragment().apply {
                show(activity.supportFragmentManager, TAG)
            }
    }
}