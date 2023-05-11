package com.emeritus.student.view

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.emeritus.student.R
import com.instructure.pandarecycler.interfaces.EmptyInterface
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import kotlinx.android.synthetic.main.empty_inbox_view.view.*

class EmptyInboxView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), EmptyInterface {

    private var noConnectionText: String? = null
    private var titleText: String? = null
    private var messageText: String? = null
    private var isDisplayNoConnection = false

    init {
        View.inflate(context, R.layout.empty_inbox_view, this)
    }

    override fun setLoading() {
        title.setGone()
        message.setGone()
        image.setGone()
        loading.announceForAccessibility(context.getString(R.string.loading))
        loading.setVisible()
    }

    override fun setDisplayNoConnection(isNoConnection: Boolean) {
        isDisplayNoConnection = isNoConnection
    }

    override fun setListEmpty() {
        if (isDisplayNoConnection) {
            // TODO We can move this also to commons later, and than we can use the synthetic properties.
            findViewById<TextView>(R.id.noConnection).text = noConnectionText
        } else {
            title.text = titleText
            message.text = messageText
        }
        title.setVisible()
        message.setVisible()
        loading.setGone()
        image.setVisible(image.drawable != null)
    }

    fun getTitle(): TextView {
        return title
    }

    override fun setTitleText(s: String) {
        titleText = s
        title.text = titleText
    }

    override fun setTitleText(sResId: Int) {
        titleText = context.resources.getString(sResId)
        title.text = titleText
    }

    fun getMessage(): TextView {
        return message
    }

    override fun setMessageText(s: String) {
        messageText = s
        message.text = messageText
    }

    override fun setMessageText(sResId: Int) {
        messageText = context.resources.getString(sResId)
        message.text = messageText
    }

    override fun setNoConnectionText(s: String) {
        noConnectionText = s
        findViewById<TextView>(R.id.noConnection).text = noConnectionText
    }

    override fun getEmptyViewImage(): ImageView? = image

    override fun setEmptyViewImage(drawable: Drawable) {
        image.setImageDrawable(drawable)
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
            true -> image.setVisible()
            false -> image.setGone()
        }
    }

    fun changeTextSize(isCalendar: Boolean = false) {
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

    fun setGuidelines(imTop: Float, imBottom: Float, tiTop: Float, txLeft: Float, txRight: Float) {
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
