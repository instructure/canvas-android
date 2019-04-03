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

import android.annotation.TargetApi;
import android.content.Context;
import android.util.Log;

import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.Masquerading;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


public class HttpHelpers {

    /**
     * httpPut is a bare-bones implementation for writing HTTPPut requests to the CanvasAPI.
     * It is used for the old-style synchronous calls that haven't yet been converted.
     * @param putURL
     * @param context
     * @return
     */
    public static APIHttpResponse httpPut(String putURL, Context context) {
        //Explicit check for null.
        if(context == null) {
            return new APIHttpResponse();
        }

        try {
            putURL = Masquerading.addMasqueradeId(putURL, context);
            //Remove spaces from the URL
            putURL = putURL.replace(" ", "%20");
            String api_protocol = APIHelpers.loadProtocol(context);
            //Make sure the URL begins with https://
            if(!putURL.startsWith("https://") && !putURL.startsWith("http://"))
            {
                putURL = api_protocol + "://"+APIHelpers.getDomain(context)+"/"+putURL;
            }

            HttpPut get = new HttpPut(putURL);

            String token = APIHelpers.getToken(context);
            if(token != null)
            {
                String headerValue = String.format("Bearer %s", token);
                get.addHeader("Authorization",headerValue);
            }

            HttpClient client = getHttpClient(context);
            HttpResponse response = client.execute(get);

            return parseLinkHeaderResponse(response);
        } catch(Exception E) {
            return new APIHttpResponse();
        }
    }

    /**
     * externalHttpGet is a way to make unauthenticated HTTPRequests to APIs other than the CanvasAPI.
     * The ENTIRE url must be specified including domain.
     * @param context
     * @param getURL
     * @return
     */
    public static APIHttpResponse externalHttpGet(Context context, String getURL) {
        return externalHttpGet(context, getURL, false);
    }


    /**
     * externalHttpGet is a way to make  HTTPRequests to APIs other than the CanvasAPI.
     * The ENTIRE url must be specified including domain.
     * @param context
     * @param getURL
     * @param includeAuthentication whether or not the should be authenticated using the CanvasToken saved.
     * @return
     */
    public static APIHttpResponse externalHttpGet(Context context, String getURL, boolean includeAuthentication) {
        //Explicit check for null.
        if(context == null) {
            return new APIHttpResponse();
        }

        try {
            getURL = Masquerading.addMasqueradeId(getURL, context);
            //Remove spaces from the URL
            getURL = getURL.replace(" ", "%20");

            String api_protocol = APIHelpers.loadProtocol(context);
            //Make sure the URL begins with https://
            if(!getURL.startsWith("https://") && !getURL.startsWith("http://"))
            {
                getURL = api_protocol + "://"+getURL;
            }

            final HttpURLConnection urlConnection = (HttpURLConnection) new URL(getURL).openConnection();
            urlConnection.setRequestMethod("GET");

            if (includeAuthentication) {
                String token = APIHelpers.getToken(context);
                if(token != null)
                {
                    String headerValue = String.format("Bearer %s", token);
                    urlConnection.setRequestProperty("Authorization", headerValue);
                }
            }

            return parseLinkHeaderResponse(urlConnection);
        } catch(Exception e) {
            Log.e(APIHelpers.LOG_TAG, "Error externalHttpGet: " + e.getMessage());
            return new APIHttpResponse();
        }
    }

    /**
     * httpPost is  a bare-bones implementation for writing HTTPPost requests to the CanvasAPI.
     * It is used for the old-style synchronous calls that haven't yet been converted.
     * @param postURL
     * @param postVars
     * @param context
     * @return
     */
    public static APIHttpResponse httpPost(String postURL, List<BasicNameValuePair> postVars, Context context) {
        //Explicit check for null.
        if(context == null) {
            return new APIHttpResponse();
        }

        try {
            postURL = Masquerading.addMasqueradeId(postURL, context);
            //Remove spaces from the URL
            postURL = postURL.replace(" ", "%20");
            String api_protocol = APIHelpers.loadProtocol(context);
            //Make sure the URL begins with https://
            if(!postURL.startsWith("https://") && !postURL.startsWith("http://"))
            {
                postURL = api_protocol + "://"+APIHelpers.getDomain(context)+"/"+postURL;
            }

            HttpPost post = new HttpPost(postURL);
            String token = APIHelpers.getToken(context);
            if(token != null)
            {
                String headerValue = String.format("Bearer %s", token);
                post.addHeader("Authorization",headerValue);
            }

            HttpClient client = getHttpClient(context);

            if(postVars!=null)
                post.setEntity(new UrlEncodedFormEntity(postVars,"UTF-8"));

            HttpResponse response = client.execute(post);

            return parseLinkHeaderResponse(response);
        } catch(Exception E) {
            return new APIHttpResponse();
        }
    }

    /**
     * getHttpClient is a builder used to inject the user agent into the standard HTTPClient.
     * @param context
     * @return
     */

    private static HttpClient getHttpClient(Context context) {
        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, APIHelpers.getUserAgent(context));
        return httpclient;
    }

    /**
     * redirectURL tries its best to follow http redirects until there are no more.
     * @param urlConnection
     * @return
     */
    @TargetApi(9)
    public static HttpURLConnection redirectURL(HttpURLConnection urlConnection) {
        HttpURLConnection.setFollowRedirects(true);
        try {
            urlConnection.connect();

            String currentURL = urlConnection.getURL().toString();
            do
            {
                urlConnection.getResponseCode();
                currentURL = urlConnection.getURL().toString();
                urlConnection = (HttpURLConnection) new URL(currentURL).openConnection();
            }
            while (!urlConnection.getURL().toString().equals(currentURL));
        } catch(Exception E){}
        return urlConnection;

    }

    private static APIHttpResponse parseLinkHeaderResponse(HttpURLConnection urlConnection) {
        APIHttpResponse httpResponse = new APIHttpResponse();
        InputStream inputStream = null;
        try {
            httpResponse.responseCode = urlConnection.getResponseCode();

            // Check if response is supposed to have a body
            if (httpResponse.responseCode != 204) {
                    inputStream = urlConnection.getInputStream();
                    InputStreamReader isReader = new InputStreamReader(inputStream );
                    BufferedReader br = new BufferedReader(isReader );
                    StringBuilder sb = new StringBuilder();
                    String inputLine = "";
                    while ((inputLine = br.readLine()) != null) {
                        sb.append(inputLine);
                    }
                    String response = sb.toString();
                    httpResponse.responseBody = response;
            }

            httpResponse.linkHeaders = APIHelpers.parseLinkHeaderResponse(urlConnection.getHeaderField("link"));
        } catch (Exception e) {
            Log.e(APIHelpers.LOG_TAG, "Failed to get response: " + e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(APIHelpers.LOG_TAG, "Could not close input stream: " + e.getMessage());
                }
            }
        }

        return httpResponse;
    }

    public static String getHtml(String url) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        HttpResponse response = client.execute(request);
        return EntityUtils.toString(response.getEntity());
    }

    /**
     * parseLinkHeaderResponse is the old way of parsing the pagination URLs out of the response.
     * @param response
     * @return
     */
    private static APIHttpResponse parseLinkHeaderResponse(HttpResponse response) {
        APIHttpResponse httpResponse = new APIHttpResponse();

        //Get status code.
        httpResponse.responseCode = response.getStatusLine().getStatusCode();

        // Check if response is supposed to have a body
        if (httpResponse.responseCode != 204) {
            try
            {
                httpResponse.responseBody = EntityUtils.toString(response.getEntity());
            }
            catch(Exception E) {}
        }

        Header[] linkHeader = response.getHeaders("Link");
        for(int j = 0; j < linkHeader.length; j++) {
            HeaderElement[] elements = linkHeader[j].getElements();
            for(int i = 0; i < elements.length;i ++) {
                String first = elements[i].getName();
                String last = elements[i].getValue();

                //Seems to strip out the equals between name and value
                String url = first+"="+last;
                if(url.startsWith("<") && url.endsWith(">")) {
                    url = url.substring(1, url.length()-1);
                } else {
                    continue;
                }

                for(int k = 0; k < elements[i].getParameterCount(); k++) {
                    NameValuePair nvp = elements[i].getParameter(k);
                    if(nvp.getName().equals("rel"))
                    {
                        if(nvp.getValue().equals("prev"))
                        {
                            httpResponse.prevURL = url;
                        }
                        else if(nvp.getValue().equals("next"))
                        {
                            httpResponse.nextURL = url;
                        }
                        else if(nvp.getValue().equals("first"))
                        {
                            httpResponse.firstURL = url;
                        }
                        else if(nvp.getValue().equals("last"))
                        {
                            httpResponse.lastURL = url;
                        }
                    }
                }
            }
        }
        return httpResponse;
    }
}
