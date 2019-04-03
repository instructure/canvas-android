/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.androidfoosball.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ImageView
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.instructure.androidfoosball.ktmodels.Table
import com.instructure.androidfoosball.ktmodels.TableSide

class QRView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    private var contents: String? = null
    private var rendered = false

    init {
        if (isInEditMode) contents = "Testing...TESTING...1...2...3"
        minimumWidth = 32f.dp(context).toInt()
        minimumHeight = 32f.dp(context).toInt()
    }

    fun setQRContents(contents: String) {
        this.contents = contents
        rendered = false
        renderContents()
    }

    fun setTableSide(table: Table, side: TableSide) {
        setQRContents("foos://com.instructure.foos/${table.id}/${if (side.isSide1) 0 else 1}")
    }

    private fun renderContents() {
        if (contents != null && measuredWidth > 0 && measuredHeight > 0 && !rendered) {
            val hints = mapOf(EncodeHintType.MARGIN to "0")
            val bm = QRCodeWriter().encode(contents, BarcodeFormat.QR_CODE, measuredWidth, measuredHeight, hints)
            val bitmap = Bitmap.createBitmap(bm.width, bm.height, Bitmap.Config.RGB_565)
            for (i in 0 until (bm.width * bm.height)) {
                val x = i % bm.width
                val y = i / bm.width
                bitmap.setPixel(x, y, if (bm[x, y]) Color.BLACK else Color.WHITE)
            }
            rendered = true
            setImageBitmap(bitmap)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        renderContents()
    }

}

