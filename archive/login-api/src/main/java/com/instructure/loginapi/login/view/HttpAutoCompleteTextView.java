/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.loginapi.login.view;

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
