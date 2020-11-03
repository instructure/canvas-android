package com.instructure.teacher.view.grade_slider

import android.graphics.Rect
import android.widget.SeekBar

class ShowSliderGradeEvent(val seekBar: SeekBar?, val assigneeId: Long?, val description: String, val isTutorialTip: Boolean = false)

class ShowLongHoldStartEvent()