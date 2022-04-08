/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.pandautils.binding

import android.graphics.Bitmap
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityNodeInfo
import android.webkit.JavascriptInterface
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.instructure.pandautils.BR
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.pandautils.views.EmptyView
import java.net.URLDecoder

@BindingAdapter(value = ["itemViewModels", "onItemsAdded", "shouldUpdate"], requireAll = false)
fun bindItemViewModels(container: ViewGroup, itemViewModels: List<ItemViewModel>?, onItemsAdded: Runnable?, shouldUpdate: Boolean?) {
    if (shouldUpdate == null || shouldUpdate || container.childCount == 0) {
        container.removeAllViews()
        itemViewModels?.forEach { item: ItemViewModel ->
            val binding: ViewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(container.context), item.layoutId, container, false)
            binding.setVariable(BR.itemViewModel, item)
            container.addView(binding.root)
        }
        onItemsAdded?.run()
    }
}

@BindingAdapter("emptyViewState")
fun bindEmptyViewState(emptyView: EmptyView, state: ViewState?) {
    when (state) {
        is ViewState.Success -> emptyView.setGone()
        is ViewState.Loading -> emptyView.setLoading()
        is ViewState.Refresh -> emptyView.setGone()
        is ViewState.Empty -> {
            emptyView.setVisible()
            state.emptyTitle?.let { emptyView.setTitleText(it) }
            state.emptyMessage?.let { emptyView.setMessageText(it) }
            state.emptyImage?.let { emptyView.setEmptyViewImage(it) }
            emptyView.setListEmpty()
        }
        is ViewState.Error -> handleErrorState(emptyView, state)
    }
}

private fun handleErrorState(emptyView: EmptyView, error: ViewState.Error) {
    if (error.errorMessage.isNullOrEmpty()) {
        emptyView.setGone()
    } else {
        emptyView.setVisible()
        emptyView.setError(error.errorMessage)
    }
}

@BindingAdapter("recyclerViewItemViewModels", "adapter", requireAll = false)
fun bindItemViewModels(recyclerView: RecyclerView, itemViewModels: List<ItemViewModel>?, bindableAdapter: BindableRecyclerViewAdapter?) {
    val adapter = bindableAdapter ?: getOrCreateAdapter(recyclerView)
    recyclerView.adapter = adapter
    adapter.updateItems(itemViewModels)
}

@BindingAdapter("refreshState")
fun bindRefreshState(swipeRefreshLayout: SwipeRefreshLayout, state: ViewState?) {
    swipeRefreshLayout.isRefreshing = state == ViewState.Refresh
}

private fun getOrCreateAdapter(recyclerView: RecyclerView): BindableRecyclerViewAdapter {
    return if (recyclerView.adapter != null && recyclerView.adapter is BindableRecyclerViewAdapter) {
        recyclerView.adapter as BindableRecyclerViewAdapter
    } else {
        val bindableRecyclerAdapter = BindableRecyclerViewAdapter()
        bindableRecyclerAdapter
    }
}

@BindingAdapter(value = ["htmlContent", "htmlTitle", "onLtiButtonPressed"], requireAll = false)
fun bindHtmlContent(webView: CanvasWebView, html: String?, title: String?, onLtiButtonPressed: OnLtiButtonPressed?) {
    webView.loadHtml(html ?: "", title ?: "")
    if (onLtiButtonPressed != null) {
        webView.addJavascriptInterface(JSInterface(onLtiButtonPressed), "accessor")
    }
}

interface OnLtiButtonPressed {
    fun onLtiButtonPressed(url: String)
}

private class JSInterface(private val onLtiButtonPressed: OnLtiButtonPressed) {

    @JavascriptInterface
    fun onLtiToolButtonPressed(id: String) {
        val ltiUrl = URLDecoder.decode(id, "UTF-8")
        onLtiButtonPressed.onLtiButtonPressed(ltiUrl)
    }
}

@BindingAdapter(value = ["imageUrl", "overlayColor"], requireAll = false)
fun bindImageWithOverlay(imageView: ImageView, imageUrl: String?, overlayColor: Int?) {
    if (overlayColor != null) {
        imageView.post {
            imageView.setCourseImage(imageUrl, overlayColor, true)
        }
    } else {
        Glide.with(imageView)
            .load(imageUrl)
            .into(imageView)
    }
}

@BindingAdapter(value = ["borderColor", "borderWidth", "backgroundColor", "borderCornerRadius"], requireAll = false)
fun addBorderToContainer(view: View, borderColor: Int?, borderWidth: Int?, backgroundColor: Int?, borderCornerRadius: Int?) {
    val border = GradientDrawable()
    val background = backgroundColor ?: 0xffffff
    val strokeColor = borderColor
            ?: 0x000000
    border.setColor(background)
    border.setStroke(borderWidth?.toPx ?: 2.toPx, strokeColor)
    border.cornerRadius = borderCornerRadius?.toPx?.toFloat() ?: 4.toPx.toFloat()
    view.background = border
}
@BindingAdapter("layout_constraintWidth_percent")
fun bindConstraintWidthPercentage(view: View, percentage: Float) {
    val params = view.layoutParams as ConstraintLayout.LayoutParams
    params.matchConstraintPercentWidth = percentage
    view.layoutParams = params
}

@BindingAdapter("imageRes")
fun bindImageResource(imageView: ImageView, @DrawableRes imageRes: Int) {
    imageView.setImageDrawable(ContextCompat.getDrawable(imageView.context, imageRes))
}

@BindingAdapter("bitmap")
fun bindBitmap(imageView: ImageView, bitmap: Bitmap?) {
    bitmap?.let {
        Glide.with(imageView)
                .load(it)
                .into(imageView)
    }
}


@BindingAdapter("accessibilityClickDescription")
fun bindAccesibilityDelegate(view: View, clickDescription: String) {
    view.accessibilityDelegate = object : View.AccessibilityDelegate() {
        override fun onInitializeAccessibilityNodeInfo(host: View?, info: AccessibilityNodeInfo?) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            info?.addAction(AccessibilityNodeInfo.AccessibilityAction(AccessibilityNodeInfo.ACTION_CLICK, clickDescription))
        }
    }
}

@BindingAdapter("android:layout_marginBottom")
fun setBottomMargin(view: View, bottomMargin: Int) {
    val layoutParams: ViewGroup.MarginLayoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin,
        layoutParams.rightMargin, bottomMargin)
    view.layoutParams = layoutParams
}

@BindingAdapter(value = ["userAvatar", "userName"], requireAll = true)
fun bindUserAvatar(imageView: ImageView, userAvatarUrl: String?, userName: String?) {
    ProfileUtils.loadAvatarForUser(imageView, userName, userAvatarUrl)
}

@BindingAdapter("accessibleTouchTarget")
fun bindAccessibleTouchTarget(view: View, accessibleTouchTarget: Boolean?) {
    if (accessibleTouchTarget == true) {
        view.accessibleTouchTarget()
    }
}
