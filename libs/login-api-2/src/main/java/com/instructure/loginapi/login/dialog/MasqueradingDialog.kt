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
package com.instructure.loginapi.login.dialog

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.animation.Interpolator
import androidx.annotation.FloatRange
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.instructure.canvasapi2.utils.isValid
import com.instructure.loginapi.login.R
import com.instructure.loginapi.login.databinding.DialogMasqueradingBinding
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.base.BaseCanvasDialogFragment
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.onTextChanged
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsCloseButton
import java.util.Locale


class MasqueradingDialog : BaseCanvasDialogFragment() {

    private val binding by viewBinding(DialogMasqueradingBinding::bind)

    private var callback: OnMasqueradingSet? = null
    private var preFillDomain by StringArg()
    private var isFullscreen by BooleanArg()

    interface OnMasqueradingSet {
        fun onStartMasquerading(domain: String, userId: Long)
        fun onStopMasquerading()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as? OnMasqueradingSet
                ?: targetFragment as? OnMasqueradingSet
                ?: throw IllegalStateException("Context or target fragment must implement OnMasqueradingSet")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_masquerading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.setupAsCloseButton { dismiss() }
        ViewStyler.themeToolbarLight(requireActivity(), toolbar)

        ViewStyler.themeButton(startButton)
        startButton.onClick {
            val userId = userIdInput.text.toString().toLongOrNull()
            val domain = sanitizeDomain(domainInput.text.toString())
            if (!validateDomain(domain)) {
                domainLayout.error = getString(R.string.masqueradeErrorDomain)
            } else if (userId == null) {
                userIdLayout.error = getString(R.string.masqueradeErrorUserId)
            } else {
                domainInput.isEnabled = false
                userIdInput.isEnabled = false
                startButton.setGone()
                progressBar.setVisible()
                callback?.onStartMasquerading(domain, userId)
            }
        }

        if (preFillDomain.isValid()) {
            domainInput.setText(preFillDomain)
            domainInput.isEnabled = false
        }

        setupPandaAnimation(redPanda)

        domainInput.onTextChanged { domainLayout.isErrorEnabled = false }
        userIdInput.onTextChanged { userIdLayout.isErrorEnabled = false }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            if (isFullscreen) setOnShowListener {
                window?.setBackgroundDrawable(null)
                window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            }
        }
    }

    private fun setupPandaAnimation(view: View) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (view.measuredHeight > 0) {
                    view.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    val initialRotation = view.rotation
                    val initialTranslationY = view.translationY

                    val anim = ObjectAnimator.ofFloat(view, "rotation", initialRotation, 0f).apply {
                        repeatCount = ObjectAnimator.INFINITE
                        repeatMode = ObjectAnimator.REVERSE
                        interpolator = PaddedInterpolator(EaseInOutInterpolator(), 0.3f, 0.7f)
                        duration = 3000
                    }

                    val anim2 = ObjectAnimator.ofFloat(view, "translationY", initialTranslationY, 0f).apply {
                        repeatCount = ObjectAnimator.INFINITE
                        repeatMode = ObjectAnimator.REVERSE
                        interpolator = PaddedInterpolator(EaseInOutInterpolator(bias = 0.55), 0.3f, 0.7f)
                        duration = 3000
                    }

                    AnimatorSet().apply { playTogether(anim, anim2) }.start()
                }
            }
        })
        ObjectAnimator.ofFloat()
    }

    private fun sanitizeDomain(domain: String): String {
        var url = domain.lowercase(Locale.getDefault()).replace(" ", "").substringAfter("www.")

        // If there are no periods, append .instructure.com
        if (!url.contains(".") || url.endsWith(".beta")) {
            url += ".instructure.com"
        }

        return url
    }

    private fun validateDomain(domain: String) = Patterns.DOMAIN_NAME.matcher(domain).matches()


    /**
     * An [Interpolator] which pads the provided [interpolator] with start and end values.
     */
    private class PaddedInterpolator(val interpolator: Interpolator, val start: Float, val end: Float) : Interpolator {

        val activeLength = end - start

        init {
            require(start in 0f..1f)
            require(end in 0f..1f)
            require(end > start)
        }

        override fun getInterpolation(input: Float): Float {
            return when {
                input < start -> 0f
                input > end -> 1f
                else -> interpolator.getInterpolation((input - start) / activeLength)
            }
        }

    }

    /**
     * Similar to AccelerateDecelerateInterpolator, but has a configurable [strength] and [bias].
     *
     * The [strength] value controls the rate of acceleration/deceleration and as such also controls the maximum
     * perceived 'velocity' of interpolated values. A [strength] of 0 results yields no acceleration/deceleration and
     * therefore a constant velocity. A [strength] of 1 yields infinite acceleration/deceleration and therefore an
     * infinite maximum velocity, i.e. the start value and end value instantly swap at the point of bias.
     *
     * The [bias] value controls the transition where acceleration ends and deceleration begins. A lower bias will push
     * the transition toward lower input values while a higher bias will push it toward higher input values. A [bias]
     * value of 0 yields no acceleration and will cause values to be interpolated similar to DecelerateInterpolator.
     * Conversely, a [bias] value of 1 will cause values to be interpolated similar to AccelerateInterpolator.
     */
    private class EaseInOutInterpolator(
        @FloatRange(from = 0.0, to = 1.0)
        private val bias: Double = 0.5,
        @FloatRange(from = 0.0, to = 1.0)
        private val strength: Double = 0.5
    ) : Interpolator {

        private val slope = 2.0 / (1 - strength.coerceAtMost(0.99999)) + 1

        init {
            require(bias in 0.0..1.0) { "Bias value must be between 0f and 1f. Provided value: $bias" }
            require(strength in 0.0..1.0) { "Slope value must be between 0f and 1f. Provided value: $bias" }
        }

        override fun getInterpolation(input: Float): Float {
            val x = input.toDouble()
            return when {
                input < bias -> Math.pow(x, slope) / Math.pow(bias, slope - 1)
                else -> 1 - Math.pow(1 - x, slope) / Math.pow(1 - bias, slope - 1)
            }.toFloat()
        }

    }

    companion object {
        fun show(
            fragmentManager: FragmentManager,
            domain: String = "",
            target: Fragment? = null,
            fullscreen: Boolean = true
        ) = MasqueradingDialog().apply {
            preFillDomain = domain.takeUnless { it == "siteadmin.instructure.com" }.orEmpty()
            isFullscreen = fullscreen
            target?.let { setTargetFragment(it, 1) }
            show(fragmentManager, "dialog")
        }
    }
}
