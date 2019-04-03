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

package com.instructure.student.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

/**
 * A custom AutoCompleteTextView that will ignore "http://" and "https://"
 * and give suggestions that ignore these strings.
 */
public class HttpAutoCompleteTextView extends AutoCompleteTextView {

	public HttpAutoCompleteTextView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public HttpAutoCompleteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public HttpAutoCompleteTextView(Context context) {
		super(context);
	}
	
	@Override
	protected void performFiltering(CharSequence text, int keyCode) {
		if (text.length() < 7) {
			super.performFiltering(text, keyCode);	
			return;
		}
		//if correct length, check to see if the string starts with a protocol
		//if so, ignore it and give a suggestion based off of the remainder of the string
		String value = text.toString();
		if (value.startsWith("http://")) {
			String protocolRemoved = value.substring(7);
			super.performFiltering(protocolRemoved, keyCode);
			return;
		}
		if (value.startsWith("https://")) {
			String protocolRemoved = value.substring(8);
			super.performFiltering(protocolRemoved, keyCode);
			return;
		}
	}
}
