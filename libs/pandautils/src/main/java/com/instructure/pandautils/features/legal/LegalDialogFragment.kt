/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.legal

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.TermsOfService
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.R
import com.instructure.pandautils.base.BaseCanvasDialogFragment
import com.instructure.pandautils.databinding.DialogLegalBinding
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.accessibilityClassName
import com.instructure.pandautils.utils.descendants
import com.instructure.pandautils.utils.setVisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import javax.inject.Inject

@AndroidEntryPoint
class LegalDialogFragment : BaseCanvasDialogFragment() {

    @Inject
    lateinit var legalRouter: LegalRouter

    private var termsJob: Job? = null
    private var html: String = ""

    @SuppressLint("InflateParams") // Suppress lint warning about passing null during view inflation
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity()).setTitle(getString(R.string.legal))

        val binding = DialogLegalBinding.inflate(layoutInflater)

        binding.root.descendants<ImageView>().forEach {
            it.setColorFilter(ThemePrefs.brandColor)
        }

        // Different institutions can have different terms of service, we need to get them from the api
        termsJob = tryWeave {
            val terms = awaitApi<TermsOfService> { UserManager.getTermsOfService(it, true) }
            terms.content?.let { html = it }

            // If the institution has set terms and conditions to be "no terms", just keep the item gone
            binding.termsOfUse.setVisible(html.isNotBlank())
            // Now set the rest of the items visible
            binding.privacyPolicy.setVisible()
            binding.openSource.setVisible()
        } catch {
            // Something went wrong, make everything visible
            binding.root.descendants.forEach { it.setVisible() }
        }

        builder.setView(binding.root)

        val dialog = builder.create()

        binding.termsOfUse.setOnClickListener {
            legalRouter.routeToTermsOfService(html)
            dialog.dismiss()
        }

        binding.termsOfUse.accessibilityClassName(Button::class.java.name)

        binding.privacyPolicy.setOnClickListener {
            legalRouter.routeToPrivacyPolicy()
            dialog.dismiss()
        }

        binding.privacyPolicy.accessibilityClassName(Button::class.java.name)

        binding.openSource.setOnClickListener {
            legalRouter.routeToOpenSource()
            dialog.dismiss()
        }

        binding.openSource.accessibilityClassName(Button::class.java.name)

        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onDestroy() {
        super.onDestroy()
        termsJob?.cancel()
    }

    companion object {
        const val TAG = "legalDialog"
    }
}