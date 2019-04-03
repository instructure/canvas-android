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

package com.instructure.canvasapi.api.compatibility_synchronous;

import com.instructure.canvasapi.utilities.LinkHeaders;

/**
 * Our old way of doing LinkHeaders. Provided for synchronous APIs.
 * @see com.instructure.canvasapi.utilities.LinkHeaders
 */
public class APIHttpResponse
{	
	public String responseBody;

	public int responseCode;

	//used for pagination
	public LinkHeaders linkHeaders;
	@Deprecated
	public String prevURL;
	@Deprecated
	public String nextURL;
	@Deprecated
	public String lastURL;
	@Deprecated
	public String firstURL;
}