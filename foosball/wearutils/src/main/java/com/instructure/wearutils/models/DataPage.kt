/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.instructure.wearutils.models

import android.os.Parcel
import android.os.Parcelable


class DataPage : Parcelable {

    var type = WIN_LOSS
    var mTitle: String
    var mText: String
    var mBackgroundId: Int = 0

    constructor(title: String, text: String, backgroundId: Int, type: Int) {
        this.mTitle = title
        this.mText = text
        this.mBackgroundId = backgroundId
        this.type = type
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(this.type)
        dest.writeString(this.mTitle)
        dest.writeString(this.mText)
        dest.writeInt(this.mBackgroundId)
    }

    protected constructor(`in`: Parcel) {
        this.type = `in`.readInt()
        this.mTitle = `in`.readString()
        this.mText = `in`.readString()
        this.mBackgroundId = `in`.readInt()
    }

    companion object {

        val WIN_LOSS = 1
        val TABLE = 2

        @JvmField val CREATOR: Parcelable.Creator<DataPage> = object : Parcelable.Creator<DataPage> {
            override fun createFromParcel(source: Parcel): DataPage {
                return DataPage(source)
            }

            override fun newArray(size: Int): Array<DataPage?> {
                return arrayOfNulls(size)
            }
        }
    }
}
