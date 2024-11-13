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
 */
package com.instructure.pandautils.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.widget.Toast
import androidx.annotation.FloatRange
import com.instructure.pandautils.blueprint.BaseCanvasActivity
import androidx.exifinterface.media.ExifInterface
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.pandautils.R
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.databinding.AvatarCropActivityBinding
import com.instructure.pandautils.utils.AvatarCropActivity.Companion.createIntent
import kotlinx.parcelize.Parcelize
import java.io.File
import java.io.FileOutputStream

/**
 * Configuration class for cropping the user's avatar. An instance of this class must be passed to
 * [AvatarCropActivity.createIntent].
 *
 * @param srcUri The [Uri] of the image to be cropped. An invalid [Uri] will cause [AvatarCropActivity]
 * to throw an [IllegalArgumentException]
 */
@Parcelize
data class AvatarCropConfig(
    val srcUri: Uri,

    /** The color of visual elements in [AvatarCropActivity] including stroke and text colors */
    var elementColor: Int = Color.GRAY,

    /**
     * The desired output size of the cropped image. If the size of the source image is smaller than
     * this value, the final output size will match the source image. Default is 256.
     */
    var targetOutputSize: Int = 256,

    /** The compression quality of the output image. Valid range is 0 to 100. Default is 70. */
    var compressQuality: Int = 70,

    /** The color of the toolbar in [AvatarCropActivity]. Default is [Color.BLACK] */
    var toolbarColor: Int = Color.BLACK,

    /**
     * The color of the background in [AvatarCropActivity]. This value is also used for the fading
     * mask color. Any transparency will be ignored.
     */
    var backgroundColor: Int = Color.BLACK
) : Parcelable

/**
 * An Activity that allows the user to crop an image using pinch-zoom and drag gestures. To use this
 * Activity, first create an instance of [AvatarCropConfig] and pass it to [createIntent]. Then,
 * start the Activity using [startActivityForResult]. When the user has successfully finished the
 * crop, you may obtain a [Uri] to the output file by accessing [Intent.getData] from onActivityResult.
 *
 * The output file may safely be deleted when no longer needed.
 */
class AvatarCropActivity : BaseCanvasActivity() {

    private val binding by viewBinding(AvatarCropActivityBinding::inflate)

    /** Crop configuration. Throws an IllegalArgumentException if not present in the Activity's intent */
    private val mConfig: AvatarCropConfig by lazy {
        intent?.extras?.getParcelable<AvatarCropConfig>(KEY_CONFIG) ?: throw IllegalArgumentException("Missing or invalid config")
    }

    /** Temporary file for the source image. This will be deleted when the Activity is destroyed. */
    private val mSrcFile by lazy { File(externalCacheDir, "tmp-avatar") }

    /** WeaveJob for performing the final crop off the main thread. */
    private var mCropJob: WeaveJob? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupViews()
        obtainSourceImage()
    }

    /** Obtains a copy of the source image and saves it to [mSrcFile] in a background thread. */
    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun obtainSourceImage() {
        weave {
            inBackground { contentResolver.openInputStream(mConfig.srcUri)?.copyTo(FileOutputStream(mSrcFile)) }
            setImage()
        }
    }

    private fun setupViews() = with(binding) {
        // Listen for image loaded event so we can set the toolbar menu and update the UI
        imageView.setOnImageEventListener(object : SubsamplingScaleImageView.OnImageEventListener {
            override fun onPreviewReleased() = Unit
            override fun onReady() = Unit
            override fun onTileLoadError(p0: Exception?) = Unit
            override fun onImageLoadError(p0: Exception?) = Unit
            override fun onPreviewLoadError(p0: Exception?) = Unit
            override fun onImageLoaded() {
                toolbar.inflateMenu(R.menu.avatar_crop_menu)
                progressBar.visibility = View.GONE
            }
        })

        // Use touch events to update overlayView's animated mask fade
        imageView.setOnTouchListener { _, event ->
            overlayView.isAdjusting = when (event.action) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> false
                else -> true
            }
            super.onTouchEvent(event)
        }

        // Set up 'close' button and menu listener
        toolbar.setNavigationIcon(R.drawable.ic_close)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.save) performCrop()
            true
        }

        // Set theme
        with(mConfig) {
            toolbar.setBackgroundColor(toolbarColor)
            root.setBackgroundColor(backgroundColor)
            overlayView.overlayColor = backgroundColor
            instructionsText.setTextColor(elementColor)
            overlayView.strokeColor = elementColor
        }
    }

    /** Configures the [SubsamplingScaleImageView] and sets the source image. */
    private fun setImage() = with(binding) {
        cropRoot.visibility = View.VISIBLE
        imageView.orientation = SubsamplingScaleImageView.ORIENTATION_USE_EXIF
        imageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP)
        imageView.setMinimumDpi(64)
        imageView.setImage(ImageSource.uri(mSrcFile.absolutePath))
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun performCrop() = with(binding) {
        mCropJob = weave {
            // Show loading state
            cropRoot.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            toolbar.menu.clear()

            // Attempt to crop the file in a background thread
            val outFile = try {
                inBackground { cropFile(mSrcFile, getCropInfo()) }
            } catch (ignore: Throwable) {
                null
            }

            // Handle success/error
            if (outFile == null) {
                Toast.makeText(this@AvatarCropActivity, R.string.errorGettingPhoto, Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_CANCELED)
            } else {
                setResult(Activity.RESULT_OK, Intent().setData(Uri.parse(outFile.absolutePath)))
            }

            // All done!
            finish()
        }
    }

    /**
     * Maps the current image scale and position to a [RectF] where each coordinate represents its
     * crop boundary as a percentage (0f to 1f) of the source image dimensions.
     */
    private fun getCropInfo(): RectF = with(binding) {
        val origin = imageView.viewToSourceCoord(0f, 0f) ?: return RectF(0f, 0f, 1f, 1f)
        val dimen = imageView.viewToSourceCoord(imageView.width.toFloat(), imageView.height.toFloat()) ?: return RectF(0f, 0f, 1f, 1f)
        val (appliedWidth, appliedHeight) = when (imageView.appliedOrientation) {
            90, 270 -> imageView.sHeight to imageView.sWidth
            else -> imageView.sWidth to imageView.sHeight
        }
        return RectF(origin.x / appliedWidth,
                origin.y / appliedHeight,
                dimen.x / appliedWidth,
                dimen.y / appliedHeight)
    }

    override fun onDestroy() {
        mSrcFile.delete()
        mCropJob?.cancel()
        super.onDestroy()
    }

    /** Performs that actual image crop. This should be invoked on a background thread. */
    private fun cropFile(srcFile: File, cropInfo: RectF): File {
        // Decode the dimensions of the source image
        val bOptions = BitmapFactory.Options()
        bOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(srcFile.absolutePath, bOptions)
        bOptions.inJustDecodeBounds = false

        // Get source image orientation
        val orientationTag = ExifInterface(srcFile.absolutePath).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val sWidth = bOptions.outWidth
        val sHeight = bOptions.outHeight

        // Rotate cropInfo coordinates to match source orientation
        val adjustedCrop = when (orientationTag) {
            ExifInterface.ORIENTATION_ROTATE_90 -> RectF(cropInfo.top, 1 - cropInfo.right, cropInfo.bottom, 1 - cropInfo.left)
            ExifInterface.ORIENTATION_ROTATE_180 -> RectF(1 - cropInfo.right, 1 - cropInfo.bottom, 1 - cropInfo.left, 1 - cropInfo.top)
            ExifInterface.ORIENTATION_ROTATE_270 -> RectF(1 - cropInfo.bottom, cropInfo.left, 1 - cropInfo.top, cropInfo.right)
            else -> cropInfo
        }

        // If the source image is smaller than the target output size, just use the source size
        val minSrcDimen = Math.min(sWidth, sHeight)
        if (minSrcDimen < mConfig.targetOutputSize) {
            mConfig.targetOutputSize = sWidth
            adjustedCrop.set(0f, 0f, 1f, 1f)
        }

        // Unadjusted width of crop area in the source image
        val croppedWidth = (sWidth * adjustedCrop.width()).toInt()

        /* Reduce chances of OOM errors by finding the largest sample size possible while staying
         * above the target output size. Sample size must be a power of 2. Dropping below the target
         * output size would result in a loss of fidelity. */
        var sampleSize = 1
        while (croppedWidth / (sampleSize * 2) > mConfig.targetOutputSize) sampleSize *= 2
        bOptions.inSampleSize = sampleSize

        // Reduce memory usage by only decoding the region we need
        val decodeRect = Rect(
                (sWidth * adjustedCrop.left).toInt(),
                (sHeight * adjustedCrop.top).toInt(),
                (sWidth * adjustedCrop.right).toInt(),
                (sHeight * adjustedCrop.bottom).toInt())
        val input = BitmapRegionDecoder.newInstance(srcFile.absolutePath, false).decodeRegion(decodeRect, bOptions)

        // If decoded region is smaller than target size, adjust target size to match
        mConfig.targetOutputSize = mConfig.targetOutputSize.coerceAtMost(input.width)

        // Set up matrix
        val matrix = Matrix()
        val scale = mConfig.targetOutputSize.toFloat() / input.width
        val center = mConfig.targetOutputSize / 2f
        matrix.preScale(scale, scale)
        when (orientationTag) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f, center, center)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f, center, center)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f, center, center)
        }

        // Draw decoded region to a correctly-sized bitmap, applying scale and rotation using our matrix
        val output = Bitmap.createBitmap(mConfig.targetOutputSize, mConfig.targetOutputSize, Bitmap.Config.ARGB_8888)
        val filterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
        Canvas(output).drawBitmap(input, matrix, filterPaint)

        // Save to output file
        val outFile = File(srcFile.absolutePath + "_cropped")
        output.compress(Bitmap.CompressFormat.JPEG, mConfig.compressQuality, outFile.outputStream())
        return outFile
    }

    companion object {

        /** Key name for bundling the [AvatarCropConfig] */
        private const val KEY_CONFIG = "config"

        /**
         * Creates a new intent for [AvatarCropActivity]
         *
         * @param context An Android [Context].
         * @param config An instance of [AvatarCropConfig] which should contain a valid source [Uri].
         * @return An [Intent]
         */
        fun createIntent(context: Context, config: AvatarCropConfig): Intent {
            return Intent(context, AvatarCropActivity::class.java).apply {
                putExtra(KEY_CONFIG, config)
            }
        }
    }

}

/**
 * An fading overlay view with a circular cutout designed to be used in [AvatarCropActivity].
 */
class AvatarCropOverlay @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /** Mask fade level (out of 255) used during image adjustment. Higher values are more opaque. */
    private val mMaskFade = 148 // out of 255

    /** Id of the source view */
    private var mSourceViewId: Int = 0

    /** Current fade animation progress */
    private var mFadeProgress = 0f

    /** The source view. Cutout placement is automatically calculated from the position of this view. */
    private val mSourceView: View by lazy {
        (parent as? ViewGroup)?.findViewById<View>(mSourceViewId) ?: throw IllegalArgumentException("Invalid source ID!")
    }

    /** Path of the stroke around the edge of the cutout */
    private var mStrokePath = Path()

    /** Paint used to draw the stroke */
    private var mStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }

    /** Fade out interpolator */
    private val mFadeOutInterpolator = CliffInterpolator(0.66f)

    /** The overlay color. Transparency value is ignored. */
    var overlayColor = Color.BLACK
        set(value) {
            field = value or 0xFF000000.toInt() // Set to full opacity
            invalidate()
        }

    /**
     * Whether or not the user is actively adjusting the source image. Changing this value will
     * trigger fade in/out animations.
     */
    var isAdjusting = false
        set(value) {
            /* Fade-out uses different interpolation than fade-in, so we set fade-in progress to
             * match fade-out progress when we switch */
            if (value && !field) mFadeProgress = mFadeOutInterpolator.getInterpolation(mFadeProgress)
            field = value
            invalidate()
        }

    /** Width of the stroke around the edge of the cutout */
    var strokeWidth: Float
        get() = mStrokePaint.strokeWidth
        set(value) {
            mStrokePaint.strokeWidth = value
            invalidate()
        }

    /** Color of the stroke around the edge of the cutout */
    var strokeColor: Int
        get() = mStrokePaint.color
        set(value) {
            mStrokePaint.color = value
            invalidate()
        }

    init {
        // Apply XML attributes
        if (attrs != null) {
            val defaultStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, resources.displayMetrics)
            val a = context.obtainStyledAttributes(attrs, R.styleable.AvatarCropOverlay)
            strokeWidth = a.getDimension(R.styleable.AvatarCropOverlay_aco_strokeWidth, defaultStrokeWidth)
            strokeColor = a.getColor(R.styleable.AvatarCropOverlay_aco_strokeColor, 0xFF888888.toInt())
            mSourceViewId = a.getResourceId(R.styleable.AvatarCropOverlay_aco_sourceView, 0)
            overlayColor = a.getColor(R.styleable.AvatarCropOverlay_aco_overlayColor, overlayColor)
            a.recycle()
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        // Set up cutout path to match source view
        if (mSourceViewId > 0) {
            mStrokePath.rewind()
            val x = mSourceView.left.toFloat()
            val y = mSourceView.top.toFloat()
            val w = mSourceView.width.toFloat()
            val h = mSourceView.height.toFloat()
            val centerX = x + w / 2f
            val centerY = y + h / 2f
            val radius = Math.min(w, h) / 2f
            mStrokePath.addCircle(centerX, centerY, radius, Path.Direction.CW)
        }
    }

    @Suppress("DEPRECATION")
    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.clipPath(mStrokePath, Region.Op.DIFFERENCE)
        when {
            isAdjusting -> {
                if (mFadeProgress > 0f) {
                    val currentFade = mMaskFade + (mFadeProgress * (255 - mMaskFade)).toInt()
                    canvas.drawColor(overlayColor and (currentFade shl 24))
                    mFadeProgress = ( mFadeProgress - 0.12f).coerceAtLeast(0f)
                    invalidate()
                } else {
                    canvas.drawColor(overlayColor and (mMaskFade shl 24))
                }
            }
            mFadeProgress < 1f -> {
                val currentFade = mMaskFade + (mFadeOutInterpolator.getInterpolation(mFadeProgress) * (255 - mMaskFade)).toInt()
                canvas.drawColor(overlayColor and (currentFade shl 24))
                mFadeProgress = (mFadeProgress + 0.013f).coerceAtMost(1f)
                invalidate()
            }
            else -> canvas.drawColor(overlayColor)
        }
        canvas.restore()
        canvas.drawPath(mStrokePath, mStrokePaint)
    }
}


/**
 * Interpolates values on a 'cliff'. Values from 0f to the cliff are interpolated as 0f, and values
 * from the cliff to 1f are interpolated linearly from 0f to 1f.
 *
 * @param cliff A cliff value between 0f and 1f
 */
private class CliffInterpolator(
        @FloatRange(from = 0.0, to = 1.0)
        var cliff: Float
) : Interpolator {
    override fun getInterpolation(input: Float) = ((input - cliff) * (1f / (1 - cliff))).coerceIn(0f..1f)
}
