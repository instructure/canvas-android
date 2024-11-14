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
package com.instructure.teacher.mobius.common.ui

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.pandautils.blueprint.BaseCanvasFragment
import androidx.viewbinding.ViewBinding
import com.instructure.pandautils.utils.getFragmentActivity
import com.instructure.teacher.mobius.common.ConsumerQueueWrapper
import com.instructure.teacher.mobius.common.CoroutineConnection
import com.instructure.teacher.mobius.common.GlobalEventMapper
import com.instructure.teacher.mobius.common.GlobalEventSource
import com.instructure.teacher.mobius.common.LateInit
import com.instructure.teacher.mobius.common.contraMap
import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.EventSource
import com.spotify.mobius.First
import com.spotify.mobius.Init
import com.spotify.mobius.Mobius
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.Update
import com.spotify.mobius.android.MobiusAndroid
import com.spotify.mobius.android.runners.MainThreadWorkRunner
import com.spotify.mobius.functions.Consumer
import kotlinx.android.extensions.LayoutContainer

abstract class MobiusFragment<MODEL, EVENT, EFFECT, VIEW : MobiusView<VIEW_STATE, EVENT, BINDING>, VIEW_STATE, BINDING: ViewBinding> : BaseCanvasFragment() {
    var overrideInitModel: MODEL? = null

    var overrideInitViewState: VIEW_STATE? = null

    var loopMod: ((MobiusLoop.Builder<MODEL, EVENT, EFFECT>) -> MobiusLoop.Builder<MODEL, EVENT, EFFECT>)? = null

    var loop: MobiusLoop.Builder<MODEL, EVENT, EFFECT> by LateInit {
        loopMod?.invoke(it) ?: it
    }

    lateinit var controller: MobiusLoop.Controller<MODEL, EVENT>

    lateinit var view: VIEW

    private lateinit var effectHandler: EffectHandler<VIEW, EVENT, EFFECT>

    private lateinit var globalEventSource: GlobalEventSource<EVENT>

    private lateinit var update: UpdateInit<MODEL, EVENT, EFFECT>

    abstract fun makeEffectHandler(): EffectHandler<VIEW, EVENT, EFFECT>

    abstract fun makeUpdate(): UpdateInit<MODEL, EVENT, EFFECT>

    abstract fun makeView(inflater: LayoutInflater, parent: ViewGroup): VIEW

    abstract fun makePresenter(): Presenter<MODEL, VIEW_STATE>

    abstract fun makeInitModel(): MODEL

    open val eventSources : List<EventSource<EVENT>> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        update = makeUpdate().apply { initialized = overrideInitModel != null || overrideInitViewState != null }
        globalEventSource = GlobalEventSource(update)
        effectHandler = makeEffectHandler()
        loop = Mobius.loop(update, effectHandler)
            .effectRunner { MainThreadWorkRunner.create() }
            .eventSources(globalEventSource, *eventSources.toTypedArray())
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
        view.releaseBinding()
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

abstract class MobiusView<VIEW_STATE, EVENT, BINDING : ViewBinding>(
    layoutInflater: LayoutInflater,
    bindingInflater: (layoutInflater: LayoutInflater) -> BINDING,
    private val parent: ViewGroup
) : Connectable<VIEW_STATE, EVENT>, LayoutContainer {

    private var _binding: BINDING? = bindingInflater(layoutInflater)

    val binding: BINDING
        get() = _binding!!

    override val containerView: View?
        get() = binding.root

    var connection: Connection<VIEW_STATE>? = null

    protected val context: Context
        get() = parent.context

    protected val activity: Activity
        get() = context.getFragmentActivity()

    abstract fun onConnect(output: Consumer<EVENT>)

    abstract fun render(state: VIEW_STATE)

    abstract fun onDispose()

    override fun connect(output: Consumer<EVENT>): Connection<VIEW_STATE> {
        onConnect(output)
        connection = object : Connection<VIEW_STATE> {
            override fun accept(value: VIEW_STATE) {
                render(value)
            }

            override fun dispose() {
                onDispose()
                connection = null
            }
        }
        return connection!!
    }

    fun releaseBinding() {
        _binding = null
    }
}

interface Presenter<MODEL, VIEW_STATE> {
    fun present(model: MODEL, context: Context): VIEW_STATE
}

abstract class EffectHandler<VIEW, EVENT, EFFECT> : CoroutineConnection<EFFECT>(), Connectable<EFFECT, EVENT> {
    var view: VIEW? = null

    protected var consumer = ConsumerQueueWrapper<EVENT>()

    override fun connect(output: Consumer<EVENT>): Connection<EFFECT> {
        consumer.attach(output)
        return this
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
        // TODO
    }
}

