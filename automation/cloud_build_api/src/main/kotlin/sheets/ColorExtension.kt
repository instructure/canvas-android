//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//


package sheets

import com.google.api.services.sheets.v4.model.Color

// https://github.com/aosp-mirror/platform_frameworks_base/blob/5e619dce66edc07c0d75cc0b6871953bdc5e1f84/graphics/java/android/graphics/Color.java#L1383
// https://github.com/aosp-mirror/platform_frameworks_base/blob/5e619dce66edc07c0d75cc0b6871953bdc5e1f84/graphics/java/android/graphics/Color.java#L865
fun Color.parseColor(colorString: String): Color {
    if (colorString.length != 7) throw RuntimeException("Expected color length 7. Ex: #4dd0e1")

    val colorLong = java.lang.Long.parseLong(colorString.substring(1), 16) or -0x1000000

    val color = colorLong.toInt()
    val r = (color shr 16 and 0xff) / 255.0f
    val g = (color shr 8 and 0xff) / 255.0f
    val b = (color and 0xff) / 255.0f
    val a = (color shr 24 and 0xff) / 255.0f
    return this.setRed(r).setGreen(g).setBlue(b).setAlpha(a)
}
