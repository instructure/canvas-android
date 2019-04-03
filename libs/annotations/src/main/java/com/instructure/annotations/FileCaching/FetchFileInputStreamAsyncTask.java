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

package com.instructure.annotations.FileCaching;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class FetchFileInputStreamAsyncTask extends AsyncTask<Void, Void, InputStream> {

    private static final String LOG_TAG = "SpeedGrader.FetchTask";
    private final FetchFileInputStreamCallback mCallback;
    private String mUrl;
    private final SimpleDiskCache mCache;

    public interface FetchFileInputStreamCallback {
        void onFileLoaded(InputStream fileInputStream);
    }

    private FetchFileInputStreamAsyncTask(SimpleDiskCache cache, String url, FetchFileInputStreamCallback callback) {
        mCallback = callback;
        mUrl  = url;
        mCache = cache;
    }

    public static void download(SimpleDiskCache cache, String url, FetchFileInputStreamCallback callback) {
        new FetchFileInputStreamAsyncTask(cache, url, callback).execute();
    }

    @Override
    protected InputStream doInBackground(Void... params) {
        try {
            SimpleDiskCache.InputStreamEntry entry =  mCache.getInputStream(mUrl);

            if(entry != null){
                return entry.getInputStream();
            }else{
                return downloadAndCacheFile(mUrl);
            }
        } catch (IOException e) {
            Log.d(LOG_TAG, "Download failed!" +mUrl, e);
            return null;
        }
    }

    private InputStream downloadAndCacheFile( String downloadUrl){
        InputStream result = null;
        InputStream remoteInputStream = null;
        ByteArrayOutputStream baos = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(downloadUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            //set up some things on the connection
            urlConnection.setRequestMethod("GET");

            //and connect!
            urlConnection.connect();
            connection = redirectURL(urlConnection);

            //this will be used in reading the uri from the internet
            remoteInputStream = new BufferedInputStream(connection.getInputStream());

            baos = new ByteArrayOutputStream();

            IOUtil.copy(remoteInputStream, baos);
            result = new ByteArrayInputStream(baos.toByteArray());

            // Add to cache
            mCache.put(downloadUrl, result);

        } catch (FileNotFoundException e) {
            Log.d(LOG_TAG, "File not Found Exception");
            e.printStackTrace();
        } catch (ProtocolException e) {
            Log.d(LOG_TAG, "ProtocolException : " + downloadUrl);
            e.printStackTrace();
        } catch (MalformedURLException e) {
            Log.d(LOG_TAG, "MalformedURLException" + downloadUrl);
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(LOG_TAG, "Failed to save inputStream to cache");
            e.printStackTrace();
        }finally {
            if (connection != null) {
                connection.disconnect();
            }
            IOUtil.closeStream(remoteInputStream);
            IOUtil.closeStream(baos);
        }
        return result;
    }

    @Override
    protected void onPostExecute(InputStream fileInputStream) {
        super.onPostExecute(fileInputStream);
        if(mCallback != null){
            mCallback.onFileLoaded(fileInputStream);
        }
    }

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
}
