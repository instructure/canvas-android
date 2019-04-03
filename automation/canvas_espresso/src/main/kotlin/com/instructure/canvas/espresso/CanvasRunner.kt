package com.instructure.canvas.espresso

import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.espresso.InstructureRunner
import com.instructure.espresso.ditto.DittoConfig
import okhttp3.OkHttpClient

class CanvasRunner : InstructureRunner() {
    override fun setupHttpClient() : OkHttpClient? {
        val client = DittoConfig.setupClient(CanvasRestAdapter.okHttpClient)
        CanvasRestAdapter.client = client;
        return client
    }
}
