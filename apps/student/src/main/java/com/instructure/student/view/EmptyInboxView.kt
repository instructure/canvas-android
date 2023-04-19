package com.instructure.student.view

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.instructure.pandarecycler.interfaces.EmptyInterface
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R
import com.instructure.student.databinding.EmptyInboxViewBinding

class EmptyInboxView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), EmptyInterface {

    private val binding: EmptyInboxViewBinding

    private var noConnectionText: String? = null
    private var titleText: String? = null
    private var messageText: String? = null
    private var isDisplayNoConnection = false

    init {
        binding = EmptyInboxViewBinding.inflate(LayoutInflater.from(getContext()), this, true)
    }

    override fun setLoading() {
        binding.title.setGone()
        binding.message.setGone()
        binding.image.setGone()
        binding.loading.root.announceForAccessibility(context.getString(R.string.loading))
        binding.loading.root.setVisible()
    }

    override fun setDisplayNoConnection(isNoConnection: Boolean) {
        isDisplayNoConnection = isNoConnection
    }

    override fun setListEmpty() {
        if (isDisplayNoConnection) {
            binding.loading.noConnection.text = noConnectionText
        } else {
            binding.title.text = titleText
            binding.message.text = messageText
        }
        binding.title.setVisible()
        binding.message.setVisible()
        binding.loading.root.setGone()
        binding.image.setVisible(binding.image.drawable != null)
    }

    fun getTitle(): TextView {
        return binding.title
    }

    override fun setTitleText(s: String) {
        titleText = s
        binding.title.text = titleText
    }

    override fun setTitleText(sResId: Int) {
        titleText = context.resources.getString(sResId)
        binding.title.text = titleText
    }

    fun getMessage(): TextView {
        return binding.message
    }

    override fun setMessageText(s: String) {
        messageText = s
        binding.message.text = messageText
    }

    override fun setMessageText(sResId: Int) {
        messageText = context.resources.getString(sResId)
        binding.message.text = messageText
    }

    override fun setNoConnectionText(s: String) {
        noConnectionText = s
        findViewById<TextView>(R.id.noConnection).text = noConnectionText
    }

    override fun getEmptyViewImage(): ImageView? = binding.image

    override fun setEmptyViewImage(drawable: Drawable) {
        binding.image.setImageDrawable(drawable)
    }

    override fun emptyViewText(s: String) {
        setTitleText(s)
    }

    override fun emptyViewText(sResId: Int) {
        setTitleText(sResId)
    }

    override fun emptyViewImage(drawable: Drawable) {
        setEmptyViewImage(drawable)
    }

    fun setImageVisible(visible: Boolean) {
        when (visible) {
            true -> binding.image.setVisible()
            false -> binding.image.setGone()
        }
    }

    fun changeTextSize(isCalendar: Boolean = false) = with(binding) {
        if (isCalendar) {
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            } else {
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP,12f)
                message.setTextSize(TypedValue.COMPLEX_UNIT_SP,8f)
            }
        } else if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        } else {
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        }
    }

    fun setGuidelines(imTop: Float, imBottom: Float, tiTop: Float, txLeft: Float, txRight: Float) = with(binding) {
        val iTop = imageTop.layoutParams as ConstraintLayout.LayoutParams
        iTop.guidePercent = imTop
        imageTop.layoutParams = iTop
        val iBottom = imageBottom.layoutParams as ConstraintLayout.LayoutParams
        iBottom.guidePercent = imBottom
        imageBottom.layoutParams = iBottom
        val tTop = titleTop.layoutParams as ConstraintLayout.LayoutParams
        tTop.guidePercent = tiTop
        titleTop.layoutParams = tTop
        val tLeft = textLeft.layoutParams as ConstraintLayout.LayoutParams
        tLeft.guidePercent = txLeft
        textLeft.layoutParams = tLeft
        val tRight = textRight.layoutParams as ConstraintLayout.LayoutParams
        tRight.guidePercent = txRight
        textRight.layoutParams = tRight
    }
}
