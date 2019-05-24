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
@file:Suppress("unused")

package com.instructure.student.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder


abstract class BasicRecyclerAdapter<T : Any, C : BasicItemCallback>(val callback: C) :
    RecyclerView.Adapter<ViewHolder>() {

    val registeredBinders: MutableMap<Class<out T>, BasicItemBinder<out T, C>> = mutableMapOf()

    inline fun <reified I : T, reified B : BasicItemBinder<I, C>> register(binder: B) {
        binder.viewType = registeredBinders[I::class.java]?.viewType ?: registeredBinders.size
        registeredBinders[I::class.java] = binder
    }

    abstract fun registerBinders()

    /** The comparator used to compare items of different subtypes */
    open val baseComparator: Comparator<T> = Comparator { _, _ -> -1 }

    val collapsedGroups: MutableSet<Long> = mutableSetOf()

    var data: List<T> = emptyList()
        set(value) {
            field = value
            updateExpandedItems()
        }

    private var items: List<T> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private fun updateExpandedItems() {
        var expanded = true
        items = data.filter {
            val binder = getBinder(it)
            when (binder.bindBehavior) {
                is BasicItemBinder.Header -> {
                    expanded = binder.getItemId(it) !in collapsedGroups
                    true
                }
                else -> expanded
            }
        }
    }

    init {
        @Suppress("LeakingThis")
        registerBinders()
    }

    fun clear() {
        data = emptyList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return registeredBinders.values.first { it.viewType == viewType }.createViewHolder(parent.context, parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        performBind(items[position], holder)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <I : T> getBinder(item: I): BasicItemBinder<I, C> =
        registeredBinders[item::class.java] as? BasicItemBinder<I, C>
                ?: throw IllegalStateException("No binder registered for ${item::class.java.name}")

    private fun performBind(item: T, holder: ViewHolder) {
        val binder = getBinder(item)
        when (val behavior = binder.bindBehavior) {
            is BasicItemBinder.Item -> behavior.onBind(item, holder.itemView, callback)
            is BasicItemBinder.Header -> {
                if (behavior.collapsible) {
                    holder.itemView.setOnClickListener {
                        val groupId = binder.getItemId(item)
                        val expanded = groupId in collapsedGroups
                        if (expanded) collapsedGroups -= groupId else collapsedGroups += groupId
                        behavior.onExpand(item, !expanded, callback)
                        updateExpandedItems()
                    }
                } else {
                    behavior.onBind(item, holder.itemView, true, callback)
                }
            }
            is BasicItemBinder.NoBind -> Unit // Do nothing
            else -> throw IllegalArgumentException("Unknown bind behavior: ${binder.bindBehavior::class.java.name}")
        }
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        val item = items[position]
        return getBinder(item).viewType
    }

}

abstract class BasicItemBinder<T : Any, C : BasicItemCallback> {

    var viewType: Int = 0

    @get:LayoutRes
    abstract val layoutResId: Int

    /**
     * Returns an ID for the specified item. By default this returns a negative value that is assigned to
     * this [BasicItemBinder] when registered with an adapter. In most cases this should be overridden to provide a
     * unique ID value for the given item, but the default behavior can safely be used for items that are expected to
     * appear no more than once in the list (e.g. an inline loading indicator or error view).
     */
    open fun getItemId(item: T): Long = -viewType.toLong()

    fun createViewHolder(context: Context, parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(context).inflate(layoutResId, parent, false)
        initView(view)
        return object : ViewHolder(view) {}
    }

    open fun initView(view: View) = Unit

    open val comparator: Comparator<T> = Comparator { _, _ -> -1 }

    abstract val bindBehavior: BindBehavior<T, C>

    // region BindBehavior

    /**
     * A base class for defining bind behavior. Ideally this would be a sealed class, but due to generics we instead use
     * inner classes to aid in type inference.
     */
    open class BindBehavior<T, C> internal constructor()

    inner class NoBind : BindBehavior<T, C>()

    inner class Item(
        val onBind: (item: T, view: View, callback: C) -> Unit
    ) : BindBehavior<T, C>()

    inner class Header(
        val collapsible: Boolean = true,
        val onExpand: (item: T, isExpanded: Boolean, callback: C) -> Unit = { _, _, _ -> },
        val onBind: (item: T, view: View, isCollapsed: Boolean, callback: C) -> Unit
    ) : BindBehavior<T, C>()

    // endregion

}

interface BasicItemCallback





