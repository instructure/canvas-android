/*
* Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.TermsOfService
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.analytics.SCREEN_VIEW_LEGAL
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.descendants
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.activities.InternalWebViewActivity
import com.instructure.teacher.databinding.DialogLegalBinding
import kotlinx.coroutines.Job

@ScreenView(SCREEN_VIEW_LEGAL)
class LegalDialog : AppCompatDialogFragment() {

    private var termsJob: Job? = null
    private var html: String = ""

    init {
        retainInstance = true
    }

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
            val intent = InternalWebViewActivity.createIntent(requireActivity(), "http://www.canvaslms.com/policies/terms-of-use", html, getString(R.string.termsOfUse), false)
            requireActivity().startActivity(intent)
            dialog.dismiss()
        }

        binding.privacyPolicy.setOnClickListener {
            val intent = InternalWebViewActivity.createIntent(requireActivity(), "https://www.instructure.com/policies/product-privacy-policy", getString(R.string.privacyPolicy), false)
            requireActivity().startActivity(intent)
            dialog.dismiss()
        }

        binding.openSource.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/instructure/canvas-android"))
            requireActivity().startActivity(intent)
            dialog.dismiss()
        }

        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onDestroyView() {
        if (retainInstance)
            dialog?.setDismissMessage(null)
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        termsJob?.cancel()
    }

    companion object {
        const val TAG = "legalDialog"
    }
}

