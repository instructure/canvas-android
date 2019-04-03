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
package com.instructure.androidfoosball.views

import android.content.Context
import android.graphics.*
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.View
import com.instructure.androidfoosball.utils.RankingUtils
import com.instructure.androidfoosball.utils.toDp
import java.text.SimpleDateFormat
import java.util.*


class EloGraphView @JvmOverloads constructor(context: Context,
                                             attrs: AttributeSet? = null
) : View(context, attrs) {

    private val LINE_COLOR = 0xFF00ACEC.toInt()
    private var timeFrame = DateUtils.DAY_IN_MILLIS * 20
    private var startTime: Long = 0
    private var maxY: Int = RankingUtils.FOOS_RANK_CEILING
    private var minY: Int = RankingUtils.FOOS_RANK_FLOOR
    private var data: List<Pair<Long, Int>> = listOf()
    private var cal: Calendar = GregorianCalendar.getInstance()
    var backgroundPaint: Paint = Paint()
    var labelDate: Date = Date()
    val gridPaint by lazy {
        Paint().apply {
            color = Color.DKGRAY
            strokeWidth = 0.5f.toDp(context)
            isAntiAlias = true
        }
    }
    val pointPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = LINE_COLOR
        }
    }
    val linePaint by lazy {
        Paint().apply {
            color = 0x6600ACEC.toInt()
            strokeWidth = 4.toDp(context)
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
    }
    val axisPaint by lazy {
        Paint().apply {
            color = Color.DKGRAY
            strokeWidth = 2.toDp(context)
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
    }
    val yAxisLabelPaint by lazy {
        Paint().apply {
            color = Color.DKGRAY
            textSize = 18.toDp(context)
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
    }
    val xAxisLabelPaint by lazy {
        Paint().apply {
            color = Color.DKGRAY
            textSize = 18.toDp(context)
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
    }
    val pointRadius by lazy { 5.toDp(context) }
    val linePath = Path()
    val dateFormat: SimpleDateFormat = SimpleDateFormat("MMMM d")

    init {
        if(isInEditMode){
            setDummyData()
        }
    }

    fun setData(dataSet: HashMap<String, Int>) {
        if(dataSet.isEmpty()) return
        val today = getBeginningOfDay(Date())
        startTime = getBeginningOfDay(Date(today.time - timeFrame)).time

        data = dataSet
                .map { Pair(it.key.toLong(), it.value) }
                .filter { it.first >= startTime }
                .sortedBy { it.first }

        if(data.isEmpty()) return

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if(data.isEmpty()) return

        //Draw data
        val yLabelWidth = yAxisLabelPaint.measureText("3000  ")
        val xLabelHeight = (xAxisLabelPaint.fontMetrics.bottom - xAxisLabelPaint.fontMetrics.top) * 1.5f
        val yLabelOffset = (yAxisLabelPaint.fontMetrics.ascent + yAxisLabelPaint.fontMetrics.descent) / -2
        val rect = RectF(yLabelWidth, xLabelHeight, width.toFloat(), height.toFloat() - xLabelHeight)
        val dayWidth = rect.width() / 21f
        val eloWidth = rect.height() / 4f
        var gridStart = rect.left

        canvas.save()
        canvas.clipRect(rect)
        val bgRect = RectF()
        for(i in 0..20) {
            bgRect.set(gridStart, rect.top, gridStart + dayWidth, rect.bottom + dayWidth)
            if (i % 2 == 0) canvas.drawRoundRect(bgRect, dayWidth / 2, dayWidth / 2, backgroundPaint.apply { color = 0xFFEEEEEE.toInt() })
            gridStart += dayWidth
        }
        canvas.restore()

        canvas.drawText("${dateFormat.format(Date(startTime))} - ${dateFormat.format(Date())}", rect.centerX(), rect.bottom + xLabelHeight - xAxisLabelPaint.fontMetrics.bottom, xAxisLabelPaint)

        var eloGridStart = rect.top
        for(i in 0..4) {
            if(i in 1..3) {
                canvas.drawLine(rect.left, eloGridStart, rect.right, eloGridStart, gridPaint)
            }
            when (i) {
                0 -> canvas.drawText("${3000 - i * 500}  ", rect.left, eloGridStart + yLabelOffset * 2, yAxisLabelPaint)
                4 -> canvas.drawText("${3000 - i * 500}  ", rect.left, eloGridStart, yAxisLabelPaint)
                else -> canvas.drawText("${3000 - i * 500}  ", rect.left, eloGridStart + yLabelOffset, yAxisLabelPaint)
            }
            eloGridStart += eloWidth
        }
        var pointX: Float
        var pointY: Float
        data.forEachIndexed { i, it ->
            pointX = (it.first - startTime).toFloat() / timeFrame
            pointX *= rect.width()
            pointX += rect.left
            pointY = (it.second - minY).toFloat() / (maxY - minY)
            pointY *= rect.height()
            pointY = rect.height() - pointY
            if(i == data.lastIndex) {
                canvas.drawCircle(pointX, pointY, pointRadius * 1.5f, pointPaint)
            }
            canvas.drawCircle(pointX, pointY, pointRadius, pointPaint)
            if(i == 0) {
                linePath.moveTo(rect.left, pointY)
            }
            linePath.lineTo(pointX, pointY)
        }
        canvas.drawPath(linePath, linePaint)

        //Draw X - axis
        canvas.drawLine(rect.left, rect.bottom, rect.right, rect.bottom, axisPaint)
        //Draw Y - axis
        canvas.drawLine(rect.left, rect.bottom, rect.left, rect.top, axisPaint)

        super.onDraw(canvas)
    }


    fun getBeginningOfDay(date: Date) : Date {
        cal.time = date

        cal.set(GregorianCalendar.HOUR_OF_DAY, 0)
        cal.set(GregorianCalendar.SECOND, 0)
        cal.set(GregorianCalendar.MILLISECOND, 0)

        return cal.time!!
    }

    fun setDummyData() {
        val today = getBeginningOfDay(Date())
        val weeksAgo = getBeginningOfDay(Date(today.time - timeFrame)).time
        val rand = Random()
        val mapThing = HashMap<String, Int>()
        kotlin.repeat(15) {
            mapThing.put((weeksAgo + rand.nextInt(timeFrame.toInt())).toString(), minY + rand.nextInt(300) + 500)
        }
        setData(mapThing)
    }
}
