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
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.core.view.isVisible
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.R
import com.instructure.pandautils.databinding.ViewCanvasWebViewWrapperBinding
import com.instructure.pandautils.utils.ColorUtils
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import java.io.File

open class CanvasWebViewWrapper @JvmOverloads constructor(
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
        binding.themeSwitchButton.onClick {
            html?.let { html ->
                themeSwitched = !themeSwitched
                changeButtonTheme()
                onThemeChanged(themeSwitched, html)
            }
        }

        val layoutParams = attrs?.let { LayoutParams(context, attrs) } ?: LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        val webViewLayoutParams = binding.contentWebView.layoutParams
        webViewLayoutParams.height = layoutParams.height
        binding.contentWebView.layoutParams = webViewLayoutParams
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
            visitedLinkColor = if (themeSwitched) R.color.barney else R.color.textMasquerade,
        )

        binding.contentWebView.setBackgroundColor(context.getColor(background))
        binding.contentWebView.loadHtml(html, title, baseUrl, htmlFormatColors, extraFormatting)
        postDelayed({
            binding.contentWebView.clearHistory()
        }, 100)
    }

    fun loadHtml(html: String, title: String?, baseUrl: String? = null, extraFormatting: ((String) -> String)? = null) {
        this.html = html
        this.title = title

        // This is needed for captions to work in offline studio videos.
        // We need to use the user files path for base url in this case.
        if (html.contains(getCaptionsHtmlPattern())) {
            binding.contentWebView.settings.allowUniversalAccessFromFileURLs = true
            this.baseUrl = getUserFilesPath()
        } else {
            this.baseUrl = baseUrl
        }

        initVisibility(html)

        // We will change the content theme here also for pull to refresh cases.
        changeContentTheme(html, extraFormatting)
    }

    fun loadDataWithBaseUrl(url: String?, data: String, mimeType: String?, encoding: String?, history: String?) {
        html = data

        // This is needed for captions to work in offline studio videos.
        // We need to use the user files path for base url in this case.
        val baseUrl = if (data.contains(getCaptionsHtmlPattern())) {
            binding.contentWebView.settings.allowUniversalAccessFromFileURLs = true
            getUserFilesPath()
        } else {
            url
        }

        initVisibility(data)
        val formattedHtml = formatHtml(data)
        binding.contentWebView.loadDataWithBaseURL(baseUrl, formattedHtml, mimeType, encoding, history)
    }

    private fun initVisibility(html: String) {
        updateButtonVisibility(html)
        if (binding.themeSwitchButton.visibility == android.view.View.VISIBLE) {
            changeButtonTheme()
        }
    }

    private fun updateButtonVisibility(html: String) {
        val nightModeFlags: Int = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES && html.isNotEmpty()) {
            binding.themeSwitchButton.setVisible()
        } else {
            binding.themeSwitchButton.setGone()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        handleConfigurationChange()
    }

    fun setHtmlContent(content: String?) {
        this.html = content
    }

    fun setThemeSwitched(switched: Boolean) {
        this.themeSwitched = switched
    }

    fun handleConfigurationChange(reloadContent: Boolean = true) {
        html?.let { htmlContent ->
            updateButtonVisibility(htmlContent)
            if (binding.themeSwitchButton.isVisible) {
                changeButtonTheme()
                if (reloadContent) {
                    changeContentTheme(htmlContent)
                }
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return SavedState(superState).apply {
            this.themeSwitched = this@CanvasWebViewWrapper.themeSwitched
            this.html = this@CanvasWebViewWrapper.html
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        when (state) {
            is SavedState -> {
                super.onRestoreInstanceState(state.superState)
                themeSwitched = state.themeSwitched
                html = state.html
            }
            else -> super.onRestoreInstanceState(state)
        }
    }

    private class SavedState : BaseSavedState {
        var themeSwitched: Boolean = false
        var html: String? = null

        constructor(superState: Parcelable?) : super(superState)

        private constructor(parcel: Parcel) : super(parcel) {
            themeSwitched = parcel.readInt() == 1
            html = parcel.readString()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(if (themeSwitched) 1 else 0)
            out.writeString(html)
        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(parcel: Parcel) = SavedState(parcel)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }
    }

    private fun formatHtml(data: String): String {
        val textDarkest = colorResToHexString(if (themeSwitched) R.color.licorice else R.color.textDarkest)
        val textDark = colorResToHexString(if (themeSwitched) R.color.ash else R.color.textDark)
        val backgroundInfo = colorResToHexString(if (themeSwitched) R.color.electric else R.color.backgroundInfo)
        val backgroundMedium = colorResToHexString(if (themeSwitched) R.color.tiara else R.color.backgroundMedium)
        val backgroundLight = colorResToHexString(if (themeSwitched) R.color.porcelain else R.color.backgroundLight)
        val backgroundLightest = colorResToHexString(if (themeSwitched) R.color.white else R.color.backgroundLightest)

        val result = data
            .replace("{\$TEXT_DARKEST$}", textDarkest)
            .replace("{\$TEXT_DARK$}", textDark)
            .replace("{\$BACKGROUND_INFO$}", backgroundInfo)
            .replace("{\$BACKGROUND_MEDIUM$}", backgroundMedium)
            .replace("{\$BACKGROUND_LIGHT$}", backgroundLight)
            .replace("{\$BACKGROUND_LIGHTEST$}", backgroundLightest)

        return result
    }

    private fun colorResToHexString(@ColorRes colorRes: Int): String {
        return "#" + Integer.toHexString(context.getColor(colorRes)).substring(2)
    }

    private fun getCaptionsHtmlPattern(): String {
        return """<track kind="captions" src="${getUserFilesPath()}"""
    }

    private fun getUserFilesPath(): String {
        return "file://${File(context.filesDir, ApiPrefs.user?.id.toString()).absolutePath}"
    }
}