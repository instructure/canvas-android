/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.emeritus.student.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
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
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setVisible
import com.emeritus.student.R
import com.emeritus.student.activity.InternalWebViewActivity
import kotlinx.android.synthetic.main.legal.view.*
import kotlinx.coroutines.Job

@ScreenView(SCREEN_VIEW_LEGAL)
class LegalDialogStyled : AppCompatDialogFragment() {

    private var termsJob: Job? = null
    private var html: String = ""

    init {
        retainInstance = true
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(activity).inflate(R.layout.legal, null)

        view.descendants<ImageView>().forEach {
            it.setColorFilter(ThemePrefs.brandColor)
        }

        // different institutions can have different terms of service, we need to get them from the api
        termsJob = tryWeave {

            val terms = awaitApi<TermsOfService> { UserManager.getTermsOfService(it,true) }
            terms.content?.let { html = it }


            // if the institution has set terms and conditions to be "no terms", just keep the item gone
            view.termsOfUse.setVisible(html.isNotBlank())
            // now set the rest of the items visible
            view.privacyPolicy.setVisible()
            view.openSource.setVisible()
        } catch {
            // something went wrong, make everything visible
            view.descendants.forEach { it.setVisible()}
        }

        view.termsOfUse.onClick {

            val intent = InternalWebViewActivity.createIntent(activity, "http://www.canvaslms.com/policies/terms-of-use", html, getString(R.string.termsOfUse), false)
            requireContext().startActivity(intent)
            dialog?.dismiss()
        }

        view.privacyPolicy.onClick {
            val intent = InternalWebViewActivity.createIntent(activity, "https://www.instructure.com/canvas/privacy", getString(R.string.privacyPolicy), false)
            requireContext().startActivity(intent)
            dialog?.dismiss()
        }

        view.openSource.onClick {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/instructure/canvas-android"))
            requireContext().startActivity(intent)
            dialog?.dismiss()
        }

        return AlertDialog.Builder(requireContext())
                .setTitle(R.string.legal)
                .setView(view)
                .create()
    }

    override fun onDestroyView() {
        if (retainInstance) dialog?.setDismissMessage(null)
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        termsJob?.cancel()
    }

    companion object {
        val TAG = "legalDialog"
    }

}
