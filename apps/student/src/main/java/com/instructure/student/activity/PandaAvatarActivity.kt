/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.activity

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.PrefManager
import com.instructure.pandautils.analytics.SCREEN_VIEW_PANDA_AVATAR
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.util.PandaDrawables
import kotlinx.android.synthetic.main.panda_image.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date
import kotlinx.android.synthetic.main.toolbar_layout.toolbar as mToolbar


private object PandaAvatarPrefs : PrefManager(Const.NAME)

@ScreenView(SCREEN_VIEW_PANDA_AVATAR)
class PandaAvatarActivity : ParentActivity() {

    private enum class BodyPart { HEAD, BODY, LEGS }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
        setupListeners()
        Analytics.logEvent(AnalyticsEventConstants.PANDA_AVATAR_EDITOR_OPENED)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.panda_avatar_create, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun contentResId(): Int = R.layout.panda_image

    override fun showHomeAsUp(): Boolean = true

    override fun showTitleEnabled(): Boolean = true

    override fun onUpPressed() = finish()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (APIHelper.hasNetworkConnection()) {
            when (item.itemId) {
                R.id.menu_item_save_image -> {
                    if (PermissionUtils.hasPermissions(this, PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
                        Analytics.logEvent(AnalyticsEventConstants.PANDA_AVATAR_SAVED)
                        saveImageAsPNG(true, Color.TRANSPARENT, true)
                    } else {
                        requestSavePermissions()
                    }
                }
                R.id.menu_item_set_avatar -> {
                    Analytics.logEvent(AnalyticsEventConstants.PANDA_AVATAR_SET_AS_AVATAR)
                    setAsAvatar()
                }
                R.id.menu_item_share -> {
                    Analytics.logEvent(AnalyticsEventConstants.PANDA_AVATAR_SHARED)
                    saveImageAsPNG(false, Color.TRANSPARENT, false)?.let { startActivity(getShareIntent(it)) }
                }
            }
        } else {
            toast(R.string.notAvailableOffline)
        }
        return true
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun requestSavePermissions() {
        requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE), PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PermissionUtils.allPermissionsGrantedResultSummary(grantResults)) {
            if (requestCode == PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE) {
                saveImageAsPNG(true, Color.TRANSPARENT, true)
            }
        } else {
            toast(R.string.permissionDenied)
        }
    }

    private fun getShareIntent(file: File) = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, context.applicationContext.packageName + Const.FILE_PROVIDER_AUTHORITY, file))
    }

    private fun setupViews() {
        mToolbar.setTitle(R.string.pandaAvatar)
        mToolbar.setupAsBackButton { finish() }
        ViewStyler.themeToolbarColored(this, mToolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
        mToolbar.elevation = this.DP(2f)
        // Make the head and body all black
        changeHead.background = ColorKeeper.getColoredDrawable(this@PandaAvatarActivity, R.drawable.pandify_head_02, Color.BLACK)
        changeBody.background = ColorKeeper.getColoredDrawable(this@PandaAvatarActivity, R.drawable.pandify_body_11, Color.BLACK)
        loadBodyParts()
    }

    private fun loadBodyParts() {
        val head = PandaDrawables.heads[loadPart(BodyPart.HEAD)]
        imageHead.setImageResource(head.first)
        imageHead.contentDescription = getString(R.string.content_description_chosen_head, getString(head.second))

        val body = PandaDrawables.bodies[loadPart(BodyPart.BODY)]
        imageBody.setImageResource(body.first)
        imageBody.contentDescription = getString(R.string.content_description_chosen_body, getString(body.second))

        val legs = PandaDrawables.legs[loadPart(BodyPart.LEGS)]
        imageLegs.setImageResource(legs.first)
        imageLegs.contentDescription = getString(R.string.content_description_chosen_feet, getString(legs.second))
    }

    private fun setupListeners() {
        changeHead.onClick { showPartsMenu(BodyPart.HEAD) }
        changeBody.onClick { showPartsMenu(BodyPart.BODY) }
        changeLegs.onClick { showPartsMenu(BodyPart.LEGS) }
        backButton.onClick { slide(up = false) }
    }

    private fun setAsAvatar() {
        val file = saveImageAsPNG(false, ContextCompat.getColor(this, R.color.backgroundMedium), false) ?: return
        val data = Intent()
        data.putExtra(Const.PATH, file.path)
        data.putExtra(Const.SIZE, file.length())
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    private fun saveImageAsPNG(showSuccessMsg: Boolean, color: Int, saveToPictures: Boolean): File? {
        val padding = 16

        // We can set the density value to be bigger to make the images smaller if necessary
        val density = 1f

        // Make the bitmap as wide as the widest body part + padding
        val width = Math.max(Math.max((imageHead.drawable.intrinsicWidth / density).toInt(), (imageBody.drawable.intrinsicWidth / density).toInt()), (imageLegs.drawable.intrinsicWidth / density).toInt()) + padding
        // Make the bitmap as high as all the body parts together
        val height = (imageHead.drawable.intrinsicHeight / density).toInt() + (imageBody.drawable.intrinsicHeight / density).toInt() + (imageLegs.drawable.intrinsicHeight / density).toInt() + padding

        // Create a bitmap to contain all of our images
        val background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(background)
        val transPainter = Paint()
        if (color == Color.TRANSPARENT) {
            transPainter.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            canvas.drawRect(0f, 0f, background.width.toFloat(), background.height.toFloat(), transPainter)
        } else {
            transPainter.color = color
            canvas.drawRect(0f, 0f, background.width.toFloat(), background.height.toFloat(), transPainter)
        }

        var y = 8

        val headBmp = Bitmap.createScaledBitmap((imageHead.drawable as BitmapDrawable).bitmap, (imageHead.drawable.intrinsicWidth / density).toInt(), (imageHead.drawable.intrinsicHeight / density).toInt(), false)
        // Try to center the images on the screen horizontally
        var x = (width - (imageHead.drawable.intrinsicWidth / density).toInt()) / 2
        canvas.drawBitmap(headBmp, x.toFloat(), y.toFloat(), null)
        // Increment the y so we know where to draw the next body part
        y += (imageHead.drawable.intrinsicHeight / density).toInt()

        val bodyBmp = Bitmap.createScaledBitmap((imageBody.drawable as BitmapDrawable).bitmap, (imageBody.drawable.intrinsicWidth / density).toInt(), (imageBody.drawable.intrinsicHeight / density).toInt(), false)
        x = (width - (imageBody.drawable.intrinsicWidth / density).toInt()) / 2
        canvas.drawBitmap(bodyBmp, x.toFloat(), y.toFloat(), null)
        y += (imageBody.drawable.intrinsicHeight / density).toInt()

        val legsBmp = Bitmap.createScaledBitmap((imageLegs.drawable as BitmapDrawable).bitmap, (imageLegs.drawable.intrinsicWidth / density).toInt(), (imageLegs.drawable.intrinsicHeight / density).toInt(), false)
        x = (width - (imageLegs.drawable.intrinsicWidth / density).toInt()) / 2
        canvas.drawBitmap(legsBmp, x.toFloat(), y.toFloat(), null)

        val picFile = if (saveToPictures) {
            val root = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), getString(R.string.pandaAvatarsFolderName))
            File(root, "Pandafy_${Date().time}.png")
        } else {
            File(cacheDir, "panda-avatar.png")
        }

        try {
            // Save the bitmap as a png
            picFile.parentFile.mkdirs()
            FileOutputStream(picFile).use { background.compress(Bitmap.CompressFormat.PNG, 100, it) }
        } catch (e: IOException) {
            e.printStackTrace()
            toast(R.string.errorSavingAvatar)
            return null
        }

        if (showSuccessMsg) toast(R.string.avatarSuccessfullySaved)
        return picFile
    }

    private fun showPartsMenu(part: BodyPart) {
        partsOptions.visibility = View.VISIBLE
        slide(up = true)
        partsContainer.removeAllViewsInLayout()
        addParts(part)
    }

    private fun slide(up: Boolean) {
        val slide = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF,
                if (up) 5f else 0f, Animation.RELATIVE_TO_SELF, if (up) 0f else 5.2f)
        slide.duration = 400
        slide.fillAfter = true
        slide.isFillEnabled = true
        partsOptions.startAnimation(slide)
        slide.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                if (up) {
                    partsOptions.setVisible()
                } else {
                    editOptions.setVisible()
                    changeHead.requestAccessibilityFocus()
                }
            }

            override fun onAnimationRepeat(animation: Animation) = Unit

            override fun onAnimationEnd(animation: Animation) {
                partsOptions.clearAnimation()
                val lp = RelativeLayout.LayoutParams(partsOptions.width, partsOptions.height)
                if (up) lp.setMargins(0, partsOptions.width, 0, 0)
                lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                partsOptions.layoutParams = lp
                if (up) {
                    editOptions.setGone()
                } else {
                    partsOptions.visibility = View.GONE
                }
            }
        })
    }

    private fun addParts(part: BodyPart) {
        val (parts, destView, contentDescriptionFormat) = when (part) {
            BodyPart.HEAD -> Triple(PandaDrawables.heads, imageHead, R.string.content_description_chosen_head)
            BodyPart.BODY -> Triple(PandaDrawables.bodies, imageBody, R.string.content_description_chosen_body)
            BodyPart.LEGS -> Triple(PandaDrawables.legs, imageLegs, R.string.content_description_chosen_feet)
        }
        parts.forEachIndexed { index, it ->
            val imageView = ImageView(this)
            imageView.setImageResource(it.first)
            imageView.contentDescription = context.getString(it.second)
            imageView.onClick { _ ->
                imageViewAnimatedChange(destView, it.first)
                destView.contentDescription = getString(contentDescriptionFormat, getString(it.second))
                destView.announceForAccessibility(destView.contentDescription)
                savePart(part, index)
            }
            val layoutParams = LinearLayout.LayoutParams(resources.getDimension(R.dimen.scrollview_image_size).toInt(), resources.getDimension(R.dimen.scrollview_image_size).toInt())
            layoutParams.setMargins(resources.getDimension(R.dimen.scrollview_image_margin).toInt(), 0, resources.getDimension(R.dimen.scrollview_image_margin).toInt(), 0)
            imageView.layoutParams = layoutParams
            partsContainer.addView(imageView)
            if (index == 0) imageView.requestAccessibilityFocus()
        }
    }

    private fun imageViewAnimatedChange(v: ImageView, new_image: Int) {
        val animOut = AnimationUtils.loadAnimation(context, R.anim.shrink_to_middle).apply {
            interpolator = AnticipateInterpolator()
            duration = 150
        }
        val animIn = AnimationUtils.loadAnimation(context, R.anim.expand_from_middle).apply {
            interpolator = OvershootInterpolator()
            duration = 150
        }
        animOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) = Unit
            override fun onAnimationRepeat(animation: Animation) = Unit
            override fun onAnimationEnd(animation: Animation) {
                v.setImageResource(new_image)
                animIn.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) = Unit
                    override fun onAnimationRepeat(animation: Animation) = Unit
                    override fun onAnimationEnd(animation: Animation) = Unit
                })
                v.startAnimation(animIn)
            }
        })
        v.startAnimation(animOut)
    }

    private fun savePart(part: BodyPart, index: Int) = PandaAvatarPrefs.putInt(part.toString(), index)

    private fun loadPart(part: BodyPart): Int {
        val maxIndex = when(part) {
            BodyPart.HEAD -> PandaDrawables.heads.lastIndex
            BodyPart.BODY -> PandaDrawables.bodies.lastIndex
            BodyPart.LEGS -> PandaDrawables.legs.lastIndex
        }
        return PandaAvatarPrefs.getInt(part.toString()).coerceIn(0, maxIndex)
    }

    override fun onBackPressed() {
        if (partsOptions.visibility == View.VISIBLE) {
            slide(up = false)
        } else {
            super.onBackPressed()
        }
    }

}
