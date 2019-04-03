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

import com.instructure.canvasapi.model.CanvasModel;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A generic bridge to get around pagination when needed.
 *
 * Usage: Use this as your CanvasCallback and implement the ExhaustiveBridgeEvents callback.
 * In the ExhaustiveBridgeEvents callback add your 'next' api call until exhausted.
 * @param <T>
 */
public class ExhaustiveBridgeCallback<T extends CanvasModel> extends CanvasCallback<T[]>{

    private CanvasCallback<T[]> callback;
    private ExhaustiveBridgeEvents eventsCallback;
    private List<T> networkItems = new ArrayList<>();
    private List<T> cacheItems = new ArrayList<>();
    private Class<T> clazz;

    public interface ExhaustiveBridgeEvents {
        void performApiCallWithExhaustiveCallback(CanvasCallback callback, String nextUrl, boolean isCached);
    }

    public ExhaustiveBridgeCallback(Class<T> clazz, CanvasCallback<T[]> callback, ExhaustiveBridgeEvents exhaustiveBridgeEvents) {
        super(callback.statusDelegate);
        this.callback = callback;
        this.clazz = clazz;
        this.eventsCallback = exhaustiveBridgeEvents;

        if(eventsCallback == null) {
            throw new UnsupportedOperationException("ExhaustiveBridgeEvents cannot be null");
        }
    }

    @Override
    public void cache(T[] ts, LinkHeaders linkHeaders, Response response) {
        if (callback.isCancelled()) {
            return;
        }
        String nextURL = linkHeaders.nextURL;
        Collections.addAll(cacheItems, ts);

        if(nextURL == null) {  // Items exhaustively paginated
            T[] toArray =  cacheItems.toArray((T[]) Array.newInstance(clazz, networkItems.size()));
            callback.cache(toArray, linkHeaders, response);
        } else { // Get the next page
            eventsCallback.performApiCallWithExhaustiveCallback(this, nextURL, true);
        }
    }

    @Override
    public void firstPage(T[] ts, LinkHeaders linkHeaders, Response response) {
        if (callback.isCancelled()) {
            return;
        }
        String nextURL = linkHeaders.nextURL;
        Collections.addAll(networkItems, ts);

        if(nextURL == null) {  // Items exhaustively paginated
            T[] toArray =  networkItems.toArray((T[]) Array.newInstance(clazz, networkItems.size()));
            callback.firstPage(toArray, linkHeaders, response);
        } else { // Get the next page
            eventsCallback.performApiCallWithExhaustiveCallback(this, nextURL, false);
        }
    }

    @Override
    public boolean onFailure(RetrofitError retrofitError) {
        return callback.onFailure(retrofitError);
    }

    // region Getter & Setters

    public ExhaustiveBridgeEvents getEventsCallback() {
        return eventsCallback;
    }

    public void setEventsCallback(ExhaustiveBridgeEvents eventsCallback) {
        this.eventsCallback = eventsCallback;
    }

    // endregion
}
