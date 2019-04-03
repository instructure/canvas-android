package com.instructure.teacher.utils

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.DrawableCompat
import com.instructure.pandautils.utils.getDrawableCompat
import com.instructure.teacher.R
import kotlin.properties.Delegates

object MenuItemBadge {

    private var mSelectionCallback: (Int) -> Unit by Delegates.notNull()

    /**
     * update the given menu item with icon, badgeCount and style
     * @param context  use to bind onOptionsItemSelected
     * @param menu     class menuItem
     * @param icon     icon action menu
     * @param color    background color of badge
     * @param counter  counter
     */
    fun addBadge(context: Activity, menu: MenuItem?, iconResId: Int, badgeColor: Int, menuIconColor: Int, counter: Int, contentDescription: String, callback: (Int) -> Unit) {
        if (menu == null) return

        mSelectionCallback = callback

        val backgroundCircle = ShapeDrawable(OvalShape())

        val container: RelativeLayout = menu.setActionView(R.layout.menu_badge_counter).actionView as RelativeLayout
        val badgeCount: TextView
        val iconBadge: ImageView

        badgeCount = container.findViewById<TextView>(R.id.count_badge)
        iconBadge = container.findViewById<ImageView>(R.id.icon_badge)

        //Display icon in ImageView
        iconBadge.setImageDrawable(tintIt(context, menuIconColor, iconResId))
        iconBadge.contentDescription = contentDescription

        // Set Color
        backgroundCircle.paint.color = badgeColor
        badgeCount.background = backgroundCircle

        //Bind onOptionsItemSelected to the context
        container.setOnClickListener {
            mSelectionCallback.invoke(menu.itemId)
        }

        //Manage min value
        if (counter == 0) {
            badgeCount.visibility = View.GONE
        } else {
            var countString = counter.toString()
            if (countString.length > 1) {
                countString = "+"
            }
            badgeCount.visibility = View.VISIBLE
            badgeCount.text = countString
        }

        menu.isVisible = true
    }

    fun hide(menu: MenuItem) {
        menu.isVisible = false
    }

    private fun tintIt(context: Context, color: Int, @DrawableRes drawableResId: Int): Drawable {
        val wrappedDrawable = DrawableCompat.wrap(context.getDrawableCompat(drawableResId))
        DrawableCompat.setTint(wrappedDrawable, color)
        return wrappedDrawable
    }
}
