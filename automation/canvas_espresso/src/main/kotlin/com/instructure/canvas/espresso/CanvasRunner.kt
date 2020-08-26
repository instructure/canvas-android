package com.instructure.canvas.espresso

import com.instructure.canvas.espresso.mockCanvas.MockCanvasInterceptor
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.espresso.InstructureRunner
import okhttp3.OkHttpClient

open class CanvasRunner : InstructureRunner() {
    override fun setupHttpClient() : OkHttpClient? {
        val client = CanvasRestAdapter.okHttpClient
            .newBuilder()
            .addInterceptor(MockCanvasInterceptor())
            .build()
        CanvasRestAdapter.client = client
        return client
    }
}
