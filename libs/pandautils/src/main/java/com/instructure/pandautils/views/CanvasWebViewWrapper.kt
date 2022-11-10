/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
package com.instructure.pandautils.views

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.instructure.pandautils.R
import com.instructure.pandautils.databinding.ViewCanvasWebViewWrapperBinding
import com.instructure.pandautils.utils.ColorUtils
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setVisible

class CanvasWebViewWrapper @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var html: String? = null
    private var title: String? = null
    private var baseUrl: String? = null

    private var themeSwitched = false

    private val binding: ViewCanvasWebViewWrapperBinding

    init {
        binding = ViewCanvasWebViewWrapperBinding.inflate(LayoutInflater.from(context), null, false)
        addView(binding.root)

        val nightModeFlags: Int = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            binding.themeSwitchButton.setVisible()
            binding.themeSwitchButton.onClick {
                html?.let {
                    themeSwitched = !themeSwitched
                    changeContent(it)
                }
            }
        }
    }

    private fun changeContent(html: String) {
        val background = if (themeSwitched) R.color.white else R.color.backgroundLightest
        val textColor = if (themeSwitched) R.color.licorice else R.color.textDarkest
        val htmlFormatColors = HtmlFormatColors(
            backgroundColorRes = background,
            textColor = textColor,
            linkColor = if (themeSwitched) R.color.electric else R.color.textInfo,
            visitedLinkColor = if (themeSwitched) R.color.barney else R.color.textAlert,
        )

        binding.webviewWrapper.setBackgroundColor(context.getColor(background))
        binding.contentWebView.setBackgroundColor(context.getColor(background))
        binding.themeSwitchButton.background = ColorUtils.colorIt(context.getColor(textColor), binding.themeSwitchButton.background)
        ColorUtils.colorIt(context.getColor(textColor), binding.themeSwitchIcon)

        binding.themeSwitchText.setTextColor(context.getColor(textColor))
        binding.themeSwitchText.setText(if (themeSwitched) R.string.switchToDarkMode else R.string.switchToLightMode)

        binding.contentWebView.loadHtml(html, title, baseUrl, htmlFormatColors)
    }

    fun getWebView() = binding.contentWebView

    fun loadHtml(html: String, title: String?, baseUrl: String? = null) {
        this.html = html
        this.title = title
        this.baseUrl = baseUrl
        binding.contentWebView.loadHtml(html, title, baseUrl)
    }


}