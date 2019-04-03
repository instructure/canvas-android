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

package com.instructure.loginapi.login.api;

import android.content.Context;
import android.content.pm.PackageManager;

import com.instructure.canvasapi.api.compatibility_synchronous.HttpHelpers;
import com.instructure.canvasapi.model.Tab;
import com.instructure.canvasapi.utilities.APIHelpers;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class CanvasAPI 
{

    public  static String getCandroidUserAgent(String userAgentString, Context context){
        String userAgent;
        try {
            userAgent =  userAgentString + "/" + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName + " (" + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode + ")";
        } catch (PackageManager.NameNotFoundException e) {
            userAgent = userAgentString;
        }
        return userAgent;
    }

	public static String getAssetsFile(Context context, String s)
	{
		try {

			String file = "";
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(context.getAssets().open(s)));

			// do reading
			String line = "";
			while(line != null)
			{
				file+=line;

				line = reader.readLine();
			}

			reader.close();
			return file;

		} catch (Exception e) {
			return "";
		}
	}
	
	public static Map<String,String> getAuthenticatedURL(Context context) {
	       
        String token = APIHelpers.getToken(context);
        String headerValue = null;
        if(token != null)
        {
            headerValue = String.format("Bearer %s", token);
        }
        Map<String,String> map = new HashMap<String,String>();
        map.put("Authorization", headerValue);
        return map;
    }

    public static String getLTIUrlForTab(Context context, Tab tab) {
        try {

            String result = HttpHelpers.externalHttpGet(context, tab.getExternalUrl(), true).responseBody;

            String ltiUrl = null;
            if (result != null) {
                JSONObject ltiJSON = new JSONObject(result);
                ltiUrl = ltiJSON.getString("url");
            }
    
            return ltiUrl;
        } catch (Exception E) {
            return null;
        }
    }
}
