/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.mobius.common.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.student.BuildConfig
import com.instructure.student.mobius.common.*
import com.spotify.mobius.*
import com.spotify.mobius.android.MobiusAndroid
import com.spotify.mobius.android.runners.MainThreadWorkRunner
import com.spotify.mobius.functions.Consumer
import kotlinx.android.extensions.LayoutContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class MobiusFragment<MODEL, EVENT, EFFECT, VIEW : MobiusView<VIEW_STATE, EVENT>, VIEW_STATE> : Fragment(), FragmentInteractions {
    var overrideInitModel: MODEL? = null

    var overrideInitViewState: VIEW_STATE? = null

    var loopMod: ((MobiusLoop.Builder<MODEL, EVENT, EFFECT>) -> MobiusLoop.Builder<MODEL, EVENT, EFFECT>)? = null

    var loop: MobiusLoop.Builder<MODEL, EVENT, EFFECT> by LateInit {
        loopMod?.invoke(it) ?: it
    }

    lateinit var controller: MobiusLoop.Controller<MODEL, EVENT>

    protected lateinit var view: VIEW

    fun getMobiusView(): VIEW = view

    private lateinit var effectHandler: EffectHandler<VIEW, EVENT, EFFECT>

    private lateinit var globalEventSource: GlobalEventSource<EVENT>

    private lateinit var update: UpdateInit<MODEL, EVENT, EFFECT>

    abstract fun makeEffectHandler(): EffectHandler<VIEW, EVENT, EFFECT>

    abstract fun makeUpdate(): UpdateInit<MODEL, EVENT, EFFECT>

    abstract fun makeView(inflater: LayoutInflater, parent: ViewGroup): VIEW

    abstract fun makePresenter(): Presenter<MODEL, VIEW_STATE>

    abstract fun makeInitModel(): MODEL

    open fun getExternalEventSources() : List<EventSource<EVENT>> = emptyList()

    private val eventSources by lazy { getExternalEventSources() }

    // FragmentInteractions override
    override val navigation: Navigation? get() = context as? Navigation

    // FragmentInteractions override
    override fun title() = ""

    // FragmentInteractions override
    override fun getFragment(): Fragment? = this

    override fun applyTheme() = view.applyTheme()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        update = makeUpdate().apply { initialized = overrideInitModel != null || overrideInitViewState != null }
        globalEventSource = GlobalEventSource(update)
        effectHandler = makeEffectHandler()
        loop = Mobius.loop(update, effectHandler)
            .effectRunner { MainThreadWorkRunner.create() }
            .eventSources(globalEventSource, *eventSources.toTypedArray())
            .logger(MobiusExceptionLogger())
            .init(update::init)
        controller = MobiusAndroid.controller(loop, overrideInitModel ?: makeInitModel())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = makeView(inflater, container!!)
        effectHandler.view = view
        val presenter = makePresenter()
        controller.connect(view.contraMap(presenter::present, requireContext(), overrideInitViewState != null))
        if (overrideInitViewState != null) {
            view.connection?.accept(overrideInitViewState!!)
            overrideInitViewState = null
        } else if (update.initialized) {
            view.connection?.accept(presenter.present(controller.model, requireContext()))
        }
        return view.containerView
    }

    override fun onStart() {
        super.onStart()
        controller.start()
        effectHandler.view = view
    }

    override fun onStop() {
        super.onStop()
        controller.stop()
    }

    override fun onDestroyView() {
        controller.disconnect()
        super.onDestroyView()
    }

    override fun onDestroy() {
        globalEventSource.dispose()
        effectHandler.cancel()
        super.onDestroy()
    }
}

abstract class UpdateInit<MODEL, EVENT, EFFECT> : Update<MODEL, EVENT, EFFECT>, Init<MODEL, EFFECT>,
    GlobalEventMapper<EVENT> {

    var initialized = false

    override fun init(model: MODEL): First<MODEL, EFFECT> {
        return if (initialized) {
            First.first(model)
        } else {
            initialized = true
            performInit(model)
        }
    }

    abstract fun performInit(model: MODEL): First<MODEL, EFFECT>
}

abstract class MobiusView<VIEW_STATE, EVENT>(layoutId: Int, inflater: LayoutInflater, val parent: ViewGroup) :
    Connectable<VIEW_STATE, EVENT>, LayoutContainer {
    val rootView: View? = inflater.inflate(layoutId, parent, false)

    override val containerView: View?
        get() = rootView

    var connection: Connection<VIEW_STATE>? = null
    var consumer: Consumer<EVENT>? = null

    protected val context: Context
        get() = parent.context

    abstract fun onConnect(output: Consumer<EVENT>)

    abstract fun render(state: VIEW_STATE)

    abstract fun onDispose()

    abstract fun applyTheme()

    override fun connect(output: Consumer<EVENT>): Connection<VIEW_STATE> {
        onConnect(output)
        consumer = output
        connection = object : Connection<VIEW_STATE> {
            override fun accept(value: VIEW_STATE) {
                render(value)
            }

            override fun dispose() {
                onDispose()
                connection = null
                consumer = null
            }
        }
        return connection!!
    }

    fun logEvent(eventName: String) {
        Analytics.logEvent(eventName)
    }

    fun logEvent(eventName: String, bundle: Bundle) {
        Analytics.logEvent(eventName, bundle)
    }

    fun logEventWithOrigin(eventName: String) {
        Analytics.logEvent(eventName, Analytics.createOriginBundle(this::class.java.simpleName))
    }
}

interface Presenter<MODEL, VIEW_STATE> {
    fun present(model: MODEL, context: Context): VIEW_STATE
}

abstract class EffectHandler<VIEW, EVENT, EFFECT> : CoroutineConnection<EFFECT>(), Connectable<EFFECT, EVENT> {
    var view: VIEW? = null

    protected var consumer = ConsumerQueueWrapper<EVENT>()

    private val connectionWrapper by lazy { ExceptionLoggerConnectionWrapper(this) }

    override fun connect(output: Consumer<EVENT>): Connection<EFFECT> {
        consumer.attach(output)
        return connectionWrapper
    }

    override fun dispose() {
        view = null
        consumer.detach()
    }

    open fun cancel() {
        cancelCoroutine()
        dispose()
    }

    fun logEvent(eventName: String) {
        Analytics.logEvent(eventName)
    }

    /**
     * Catches exceptions in the [accept] function of the provided [connection] and logs them to Crashlytics.
     * For debug builds the exception will be logged locally and then thrown.
     */
    private class ExceptionLoggerConnectionWrapper<T>(private val connection: Connection<T>) : Connection<T> {
        override fun accept(value: T) {
            try {
                connection.accept(value)
            } catch (e: Throwable) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace()
                    // Must throw as a separate message, otherwise Mobius might silently consume the exception
                    GlobalScope.launch(Dispatchers.Main) { throw e }
                } else {
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
            }
        }

        override fun dispose() {
            connection.dispose()
        }
    }
}
