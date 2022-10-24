/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.student.util

import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.graphics.drawable.LayerDrawable
import android.os.Build
import com.instructure.student.R
import com.instructure.student.activity.LoginActivity
import com.instructure.student.router.RouteMatcher
import com.instructure.canvasapi2.models.Bookmark
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ColorUtils
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.lightColor

object ShortcutUtils {

    /**
     * Generates a bookmark for api 26+. Returns false if the bookmark could not be generated.
     */
    @TargetApi(Build.VERSION_CODES.O)
    fun generateShortcut(context: Context, bookmark: Bookmark): Boolean {

        val shortcutManager = context.getSystemService(ShortcutManager::class.java)
        if(shortcutManager?.isRequestPinShortcutSupported == true) {
            val launchIntent = Intent(context, LoginActivity::class.java)
            launchIntent.action = "com.android.launcher.action.INSTALL_SHORTCUT"
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            launchIntent.putExtra(Const.BOOKMARK, bookmark.name)
            launchIntent.putExtra(Const.URL, bookmark.url)

            val color = RouteMatcher.getContextFromUrl(bookmark.url).lightColor

            val pinShortcutInfo = ShortcutInfo.Builder(context, bookmark.url)
                    .setShortLabel(bookmark.name!!)
                    .setIntent(launchIntent)
                    .setIcon(Icon.createWithBitmap(generateLayeredBitmap(context, color)))
                    .build()

            val successIntent = shortcutManager.createShortcutResultIntent(pinShortcutInfo)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, successIntent, PendingIntent.FLAG_IMMUTABLE)
            shortcutManager.requestPinShortcut(pinShortcutInfo, pendingIntent.intentSender)
            return true
        }

        return false
    }

    /**
     * Generates a layered bitmap with the background icon and foreground bookmark icon colored to match the course/group color.
     */
    private fun generateLayeredBitmap(context: Context, color: Int): Bitmap? {
        try {
            val background = BitmapUtilities.getBitmapFromDrawable(context, R.mipmap.ic_shortcut_background)
            val foreground = ColorUtils.colorIt(color, BitmapUtilities.getBitmapFromDrawable(context, R.drawable.ic_navigation_bookmarks))

            val padding = (background.height / 2.8).toInt()

            val layers = arrayOfNulls<Drawable>(2)
            layers[0] = BitmapDrawable(context.resources, background)
            layers[1] = BitmapDrawable(context.resources, BitmapUtilities.addPaddingToBitmap(foreground, padding))
            val layerDrawable = LayerDrawable(layers)

            val bitmap = Bitmap.createBitmap(layerDrawable.intrinsicWidth, layerDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            layerDrawable.setBounds(0, 0, canvas.width, canvas.height)
            layerDrawable.draw(canvas)

            return bitmap
        } catch (e: Throwable) {
            return null
        }
    }
}
