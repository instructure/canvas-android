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
import android.widget.LinearLayout
import com.instructure.pandautils.R
import com.instructure.pandautils.databinding.ViewCanvasWebViewWrapperBinding
import com.instructure.pandautils.utils.ColorUtils
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setVisible

class CanvasWebViewWrapper @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var html: String? = null
    private var title: String? = null
    private var baseUrl: String? = null

    var themeSwitched = false
        private set

    private val binding: ViewCanvasWebViewWrapperBinding

    val webView: CanvasWebView
        get() = binding.contentWebView

    var onThemeChanged: (Boolean, String) -> Unit = { _, html ->
        changeContentTheme(html)
    }

    init {
        orientation = VERTICAL

        binding = ViewCanvasWebViewWrapperBinding.inflate(LayoutInflater.from(context), this)

        val nightModeFlags: Int = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            binding.themeSwitchButton.setVisible()
            binding.themeSwitchButton.onClick {
                html?.let { html ->
                    themeSwitched = !themeSwitched
                    changeButtonTheme()
                    onThemeChanged(themeSwitched, html)
                }
            }
        }
    }

    private fun changeButtonTheme() {
        val background = if (themeSwitched) R.color.white else R.color.backgroundLightest
        val textColor = if (themeSwitched) R.color.licorice else R.color.textDarkest

        setBackgroundColor(context.getColor(background))
        binding.themeSwitchButton.background = ColorUtils.colorIt(context.getColor(textColor), binding.themeSwitchButton.background)
        ColorUtils.colorIt(context.getColor(textColor), binding.themeSwitchIcon)

        binding.themeSwitchText.setTextColor(context.getColor(textColor))
        binding.themeSwitchText.setText(if (themeSwitched) R.string.switchToDarkMode else R.string.switchToLightMode)
    }

    private fun changeContentTheme(html: String, extraFormatting: ((String) -> String)? = null) {
        val background = if (themeSwitched) R.color.white else R.color.backgroundLightest
        val textColor = if (themeSwitched) R.color.licorice else R.color.textDarkest
        val htmlFormatColors = HtmlFormatColors(
            backgroundColorRes = background,
            textColor = textColor,
            linkColor = if (themeSwitched) R.color.electric else R.color.textInfo,
            visitedLinkColor = if (themeSwitched) R.color.barney else R.color.textAlert,
        )

        binding.contentWebView.setBackgroundColor(context.getColor(background))
        binding.contentWebView.loadHtml(html, title, baseUrl, htmlFormatColors, extraFormatting)
    }

    fun loadHtml(html: String, title: String?, baseUrl: String? = null, extraFormatting: ((String) -> String)? = null) {
        this.html = html
        this.title = title
        this.baseUrl = baseUrl

        // We will change the content theme here also for pull to refresh cases.
        changeContentTheme(html, extraFormatting)
    }

    fun loadDataWithBaseUrl(url: String?, data: String, mimeType: String?, encoding: String?, history: String?) {
        html = data
        binding.contentWebView.loadDataWithBaseURL(url, data, mimeType, encoding, history)
    }

}