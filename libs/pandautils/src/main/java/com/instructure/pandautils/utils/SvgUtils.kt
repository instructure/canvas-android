/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */

package com.instructure.pandautils.utils

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.target.Target
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import java.io.IOException
import java.io.InputStream

object SvgUtils {
    fun loadSVGImage(imageView: ImageView, imageUri: Uri, errorImageResourceId: Int) {
        Glide.with(imageView.context)
                .`as`(PictureDrawable::class.java)
                .apply(RequestOptions.errorOf(errorImageResourceId))
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(SvgSoftwareLayerSetter())
                .load(imageUri)
                .into(imageView)
    }
}

/**
 * Svg Glide Module provided by Glide samples:
 * https://github.com/bumptech/glide/blob/v4.1.1/samples/svg/src/main/java/com/bumptech/glide/samples/svg/SvgModule.java
 */
@GlideModule
class SvgModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.register(SVG::class.java, PictureDrawable::class.java, SvgDrawableTranscoder())
                .append(InputStream::class.java, SVG::class.java, SvgDecoder())
    }

    // Disable manifest parsing to avoid adding similar modules twice.
    override fun isManifestParsingEnabled() = false
}

/**
 * Svg ResourceTranscoder provided by Glide samples:
 * https://github.com/bumptech/glide/blob/v4.1.1/samples/svg/src/main/java/com/bumptech/glide/samples/svg/SvgDrawableTranscoder.java
 *
 * Convert the [SVG]'s internal representation to an Android-compatible one ([Picture][android.graphics.Picture]).
 */
internal class SvgDrawableTranscoder : ResourceTranscoder<SVG, PictureDrawable> {
    override fun transcode(toTranscode: Resource<SVG>, options: Options): Resource<PictureDrawable>? {
        val picture = toTranscode.get().renderToPicture()
        val drawable = PictureDrawable(picture)
        return SimpleResource(drawable)
    }
}

/**
 * Svg RequestListener provided by Glide samples:
 * https://github.com/bumptech/glide/blob/v4.1.1/samples/svg/src/main/java/com/bumptech/glide/samples/svg/SvgSoftwareLayerSetter.java
 *
 * Listener which updates the [ImageView] to be software rendered,
 * because [SVG][com.caverock.androidsvg.SVG]/[Picture][android.graphics.Picture]
 * can't render on a hardware backed [Canvas][android.graphics.Canvas].
 */
internal class SvgSoftwareLayerSetter : RequestListener<PictureDrawable> {
    override fun onResourceReady(
        resource: PictureDrawable,
        model: Any,
        target: Target<PictureDrawable>?,
        dataSource: DataSource,
        isFirstResource: Boolean
    ): Boolean {
        (target as? ImageViewTarget)?.view?.setLayerType(ImageView.LAYER_TYPE_SOFTWARE, null)
        return false
    }

    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<PictureDrawable>,
        isFirstResource: Boolean
    ): Boolean {
        (target as? ImageViewTarget)?.view?.setLayerType(ImageView.LAYER_TYPE_NONE, null)
        return false
    }
}

/**
 * Svg ResourceDecoder provided by Glide samples:
 * https://github.com/bumptech/glide/blob/v4.1.1/samples/svg/src/main/java/com/bumptech/glide/samples/svg/SvgDecoder.java
 */
internal class SvgDecoder : ResourceDecoder<InputStream, SVG> {
    override fun handles(source: InputStream, options: Options): Boolean = true

    @Throws(IOException::class)
    override fun decode(source: InputStream, width: Int, height: Int, options: Options): Resource<SVG>? {
        try {
            val svg = SVG.getFromInputStream(source)
            svg.documentWidth = width.toFloat()
            svg.documentHeight = height.toFloat()
            return SimpleResource(svg)
        } catch (e: SVGParseException) {
            throw IOException("Cannot load SVG from stream", e)
        }
    }
}
