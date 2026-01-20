package com.instructure.canvas.espresso

import android.content.Context
import androidx.work.DefaultWorkerFactory
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters

/**
 * A WorkerFactory that delegates to another factory, allowing the delegate to be swapped at runtime.
 * Used to start with DefaultWorkerFactory and switch to HiltWorkerFactory once it's available.
 */
class DelegatingWorkerFactory : WorkerFactory() {
    
    @Volatile
    private var delegate: WorkerFactory = DefaultWorkerFactory

    fun setDelegate(factory: WorkerFactory) {
        delegate = factory
    }

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return delegate.createWorker(appContext, workerClassName, workerParameters)
    }
}
