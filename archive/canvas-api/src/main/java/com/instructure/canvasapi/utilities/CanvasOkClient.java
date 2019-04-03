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

package com.instructure.canvasapi.utilities;

import com.squareup.okhttp.OkHttpClient;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okio.BufferedSink;
import retrofit.client.Header;
import retrofit.client.OkClient;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

/**
 * Wrapper class to help determine whether a response is from the network or the cache
 * Copy of OkClient because static methods aren't modifiable
 */
public class CanvasOkClient extends OkClient {
    static final int CONNECT_TIMEOUT_MILLIS = 15 * 1000; // 15s
    static final int READ_TIMEOUT_MILLIS = 20 * 1000; // 20s
    public static final String CANVAS_API_CACHE_HEADER = "Canvas-api-cache";
    public static final String CANVAS_API_CACHE_HEADER_VALUE = "is-cache";

    private static OkHttpClient generateDefaultOkHttp() {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        client.setReadTimeout(READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        return client;
    }

    private final OkHttpClient client;

    public CanvasOkClient() {
        this(generateDefaultOkHttp());
    }

    public CanvasOkClient(OkHttpClient client) {
        if (client == null) throw new NullPointerException("client == null");
        this.client = client;
    }

    public OkHttpClient getClient() {
        return client;
    }

    @Override public Response execute(Request request) throws IOException {
        return parseResponse(client.newCall(createRequest(request)).execute());
    }

    static com.squareup.okhttp.Request createRequest(Request request) {
        com.squareup.okhttp.Request.Builder builder = new com.squareup.okhttp.Request.Builder()
                .url(request.getUrl())
                .method(request.getMethod(), createRequestBody(request.getBody()));

        List<Header> headers = request.getHeaders();
        for (int i = 0, size = headers.size(); i < size; i++) {
            Header header = headers.get(i);
            String value = header.getValue();
            if (value == null) value = "";
            builder.addHeader(header.getName(), value);
        }

        return builder.build();
    }

    static Response parseResponse(com.squareup.okhttp.Response response) throws IOException {
        // If the networkResponse is null, then the response is from cache.
        // A header is added to the response, so it can be determined in the callback that it was from cache.
        boolean isCache = response.networkResponse() == null;

        return new Response(response.request().urlString(), response.code(), response.message(),
                createHeaders(response.headers(), isCache), createResponseBody(response.body()));
    }

    private static RequestBody createRequestBody(final TypedOutput body) {
        if (body == null) {
            return null;
        }
        final MediaType mediaType = MediaType.parse(body.mimeType());
        return new RequestBody() {
            @Override public MediaType contentType() {
                return mediaType;
            }

            @Override public void writeTo(BufferedSink sink) throws IOException {
                body.writeTo(sink.outputStream());
            }

            @Override public long contentLength() {
                return body.length();
            }
        };
    }

    private static TypedInput createResponseBody(final ResponseBody body) throws IOException{
        if (body.contentLength() == 0) {
            return null;
        }
        return new TypedInput() {
            @Override public String mimeType() {
                MediaType mediaType = body.contentType();
                return mediaType == null ? null : mediaType.toString();
            }

            @Override public long length() {
                try {
                    return body.contentLength();
                } catch (IOException e) {
                    e.printStackTrace();
                    return 0; //FIXME try catch
                }
            }

            @Override public InputStream in() throws IOException {
                return body.byteStream();
            }
        };
    }

    private static List<Header> createHeaders(Headers headers, boolean isCached) {
        int size = headers.size();
        List<Header> headerList = new ArrayList<Header>(size);
        for (int i = 0; i < size; i++) {
            headerList.add(new Header(headers.name(i), headers.value(i)));
        }
        if (isCached) {
            headerList.add(new Header(CANVAS_API_CACHE_HEADER, CANVAS_API_CACHE_HEADER_VALUE));
        }
        return headerList;
    }

}
