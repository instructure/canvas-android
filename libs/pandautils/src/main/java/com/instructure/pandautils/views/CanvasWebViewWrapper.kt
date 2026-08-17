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
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.view.isVisible
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.R
import com.instructure.pandautils.databinding.ViewCanvasWebViewWrapperBinding
import com.instructure.pandautils.utils.ColorUtils
import com.instructure.pandautils.utils.Utils
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

    // A consumer-provided color override (e.g. NGC's design-system colors) is the default appearance.
    // Captured once so the light/dark switch can temporarily bypass it — otherwise the override would
    // always win and switching to light would leave the content unchanged.
    private var defaultHtmlFormatColors: HtmlFormatColors? = null
    private var defaultColorsCaptured = false

    // Optional consumer overrides for the theme-switch control's colors, so it can match a
    // design-system surface (e.g. NGC's semantic colors) instead of the default app colors. Null keeps
    // the default appearance. Two states are needed because the button flips between the dark content
    // (not switched) and the light content (switched to light). Setting a value re-themes immediately
    // if the button is already visible.
    var themeSwitchButtonColors: ThemeSwitchButtonColors? = null
        set(value) {
            if (field == value) return
            field = value
            reapplyButtonThemeIfVisible()
        }
    var themeSwitchButtonColorsSwitched: ThemeSwitchButtonColors? = null
        set(value) {
            if (field == value) return
            field = value
            reapplyButtonThemeIfVisible()
        }

    var themeSwitched = false
        private set

    // The night-mode flag the currently-loaded HTML was themed for. Content colors are baked into the
    // HTML rather than restyled in place, so only a change to *this* requires a reload. Any other
    // configuration change (notably rotation) must not reload, or the WebView loses its scroll position
    // and re-renders for nothing.
    private var loadedNightModeFlags: Int = currentNightModeFlags()

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
                preservingScrollPosition { onThemeChanged(themeSwitched, html) }
            }
        }

        val layoutParams = attrs?.let { LayoutParams(context, attrs) } ?: LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        val webViewLayoutParams = binding.contentWebView.layoutParams
        webViewLayoutParams.height = layoutParams.height
        binding.contentWebView.layoutParams = webViewLayoutParams
    }

    private fun changeButtonTheme() {
        val override = if (themeSwitched) themeSwitchButtonColorsSwitched else themeSwitchButtonColors
        val background = override?.surface
            ?: context.getColor(if (themeSwitched) R.color.white else R.color.backgroundLightest)
        val foreground = override?.foreground
            ?: context.getColor(if (themeSwitched) R.color.licorice else R.color.textDarkest)

        setBackgroundColor(background)
        binding.themeSwitchButton.background = ColorUtils.colorIt(foreground, binding.themeSwitchButton.background)
        ColorUtils.colorIt(foreground, binding.themeSwitchIcon)

        binding.themeSwitchText.setTextColor(foreground)
        binding.themeSwitchText.setText(if (themeSwitched) R.string.switchToDarkMode else R.string.switchToLightMode)
    }

    private fun reapplyButtonThemeIfVisible() {
        if (binding.themeSwitchButton.isVisible) changeButtonTheme()
    }

    /**
     * Re-loading is the only way to re-color content whose colors are baked into the HTML, but it resets
     * the WebView's scroll position - restore it once the reload has settled.
     */
    private fun preservingScrollPosition(reload: () -> Unit) {
        val scrollY = binding.contentWebView.scrollY
        reload()
        binding.contentWebView.postDelayed({
            binding.contentWebView.scrollTo(0, scrollY)
        }, 100)
    }

    private fun currentNightModeFlags() = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

    private fun changeContentTheme(html: String, extraFormatting: ((String) -> String)? = null) {
        // Capture the consumer's override once (before we start toggling it for theme switches).
        if (!defaultColorsCaptured) {
            defaultHtmlFormatColors = binding.contentWebView.overrideHtmlFormatColors
            defaultColorsCaptured = true
        }

        val background = if (themeSwitched) R.color.white else R.color.backgroundLightest
        val textColor = if (themeSwitched) R.color.licorice else R.color.textDarkest
        val htmlFormatColors = HtmlFormatColors(
            backgroundColorRes = background,
            textColor = textColor,
            linkColor = if (themeSwitched) R.color.electric else R.color.textInfo,
            visitedLinkColor = if (themeSwitched) R.color.barney else R.color.textMasquerade,
        )

        // When switched to light, use the consumer's light override if it provided one (so the content
        // matches the consumer's light theme); otherwise fall back to the generic light colors below.
        // When not switched, keep the consumer's default appearance.
        binding.contentWebView.overrideHtmlFormatColors = if (themeSwitched) {
            binding.contentWebView.overrideHtmlFormatColorsSwitched
        } else {
            defaultHtmlFormatColors
        }
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
        if (html.contains(getCaptionsHtmlPattern()) && Utils.isNetworkAvailable(context)) {
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
        loadedNightModeFlags = currentNightModeFlags()
        updateButtonVisibility(html)
        if (binding.themeSwitchButton.visibility == android.view.View.VISIBLE) {
            changeButtonTheme()
        }
    }

    private fun updateButtonVisibility(html: String) {
        if (currentNightModeFlags() == Configuration.UI_MODE_NIGHT_YES && html.isNotEmpty()) {
            binding.themeSwitchButton.setVisible()
        } else {
            binding.themeSwitchButton.setGone()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        // Only reload when the content's colors actually became stale, i.e. night mode flipped. Hosts that
        // declare `android:configChanges` (e.g. `orientation|screenSize`) handle those changes themselves
        // without recreating this View, so we get this callback for rotations too - reloading there would
        // re-render and scroll back to the top even though no color has changed.
        val nightModeFlags = newConfig?.let { it.uiMode and Configuration.UI_MODE_NIGHT_MASK } ?: currentNightModeFlags()
        val nightModeChanged = nightModeFlags != loadedNightModeFlags
        loadedNightModeFlags = nightModeFlags
        handleConfigurationChange(reloadContent = nightModeChanged)
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
            }
            // Content re-theming must not be gated on the manual switch button's visibility: the button
            // only ever shows in dark mode (it lets the user preview light mode), but content colors
            // (e.g. R.color.backgroundLightest) are resolved from night-qualified resources regardless
            // of system theme direction - a dark-to-light system switch must still re-bake them even
            // though the button itself becomes hidden in that case.
            if (reloadContent) {
                preservingScrollPosition { changeContentTheme(htmlContent) }
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
                // Reload content with restored theme after state is restored
                handleConfigurationChange(reloadContent = true)
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

    /**
     * Colors for the theme-switch control: [surface] is the wrapper background the button sits on
     * (so it can match the WebView content background), [foreground] tints the button's border, icon
     * and text.
     */
    data class ThemeSwitchButtonColors(@ColorInt val surface: Int, @ColorInt val foreground: Int)
}