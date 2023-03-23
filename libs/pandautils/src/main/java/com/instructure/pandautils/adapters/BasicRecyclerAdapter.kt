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

package com.instructure.pandautils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import kotlin.reflect.KProperty1


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
            val diffResult = DiffUtil.calculateDiff(BasicDiffCallback(this, field, value))
            field = value
            diffResult.dispatchUpdatesTo(this)
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
        performBind(items[position], holder, null)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        performBind(items[position], holder, payloads.getOrNull(0) as? ItemDiff<T>)
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <I : T> getBinder(item: I): BasicItemBinder<I, C> =
        registeredBinders[item::class.java] as? BasicItemBinder<I, C>
                ?: throw IllegalStateException("No binder registered for ${item::class.java.name}")

    @Suppress("UNCHECKED_CAST")
    internal fun getBinderByType(viewType: Int): BasicItemBinder<*, C> =
        registeredBinders.values.find { it.viewType == viewType }
                ?: throw IllegalStateException("No binder registered for view type '$viewType'")

    override fun onViewRecycled(holder: ViewHolder) {
        getBinderByType(holder.itemViewType).onRecycle(holder)
    }

    private fun performBind(
        item: T,
        holder: ViewHolder,
        itemDiff: ItemDiff<T>?
    ) {
        val binder = getBinder(item)
        when (val behavior = binder.bindBehavior) {
            is BasicItemBinder.Item -> behavior.onBind(holder.itemView, item, callback, itemDiff)
            is BasicItemBinder.ItemWithHolder -> behavior.onBind(holder.itemView, holder, item, callback, itemDiff)
            is BasicItemBinder.Header -> {
                if (behavior.collapsible) {
                    val groupId = binder.getItemId(item)
                    val expanded = groupId in collapsedGroups
                    holder.itemView.setOnClickListener {
                        if (expanded) collapsedGroups -= groupId else collapsedGroups += groupId
                        behavior.onExpand(item, !expanded, callback)
                        updateExpandedItems()
                    }
                    behavior.onBind(holder.itemView, item, expanded, callback, itemDiff)
                } else {
                    behavior.onBind(holder.itemView, item, true, callback, itemDiff)
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

class BasicDiffCallback<T: Any, C : BasicItemCallback>(
    val adapter: BasicRecyclerAdapter<T, C>,
    val old: List<T>,
    val new: List<T>
) : DiffUtil.Callback() {

    override fun getOldListSize() = old.size

    override fun getNewListSize() = new.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = old[oldItemPosition]
        val newItem = new[newItemPosition]
        val oldItemBinder = adapter.getBinder(oldItem)
        val newItemBinder = adapter.getBinder(newItem)
        if (oldItemBinder !== newItemBinder) return false
        return oldItemBinder.getItemId(oldItem) == newItemBinder.getItemId(newItem)
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = old[oldItemPosition]
        val newItem = new[newItemPosition]
        val binder = adapter.getBinder(oldItem)
        return binder.areContentsTheSame(oldItem, newItem)
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val oldItem = old[oldItemPosition]
        val newItem = new[newItemPosition]
        return ItemDiff(oldItem, newItem)
    }

}

abstract class BasicItemBinder<T : Any, C : BasicItemCallback>() {

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

    open fun areContentsTheSame(old: T, new: T): Boolean = old == new

    fun createViewHolder(context: Context, parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(context).inflate(layoutResId, parent, false)
        initView(view)
        return constructViewHolder(context, view)
    }

    open fun constructViewHolder(context: Context, view: View): ViewHolder {
        return object : ViewHolder(view) {}
    }

    open fun initView(view: View) = Unit

    open val comparator: Comparator<T> = Comparator { _, _ -> -1 }

    abstract val bindBehavior: BindBehavior<T, C>

    open fun onRecycle(holder: ViewHolder) {}

    // region BindBehavior

    /**
     * A base class for defining bind behavior. Ideally this would be a sealed class, but due to generics we instead use
     * inner classes to aid in type inference.
     */
    open class BindBehavior<T, C> internal constructor()

    inner class NoBind : BindBehavior<T, C>()

    inner class Item(
        val onBind: View.(item: T, callback: C, diff: ItemDiff<T>?) -> Unit
    ) : BindBehavior<T, C>()

    inner class ItemWithHolder(
        val onBind: View.(holder: ViewHolder, item: T, callback: C, diff: ItemDiff<T>?) -> Unit
    ) : BindBehavior<T, C>()

    inner class Header(
        val collapsible: Boolean = true,
        val onExpand: (item: T, isExpanded: Boolean, callback: C) -> Unit = { _, _, _ -> },
        val onBind: View.(item: T, isCollapsed: Boolean, callback: C, diff: ItemDiff<T>?) -> Unit
    ) : BindBehavior<T, C>()

    // endregion

}

class ItemDiff<T: Any>(val oldItem: T, val newItem: T) {
    fun <K> prop(prop: KProperty1<T, K>, eval: (old: K, new: K) -> Unit) {
        val oldProp = prop.get(oldItem)
        val newProp = prop.get(newItem)
        if (newProp != oldProp) eval(oldProp, newProp)
    }

    inline fun processChanges(block: ItemDiff<T>.() -> Unit) = this.block()
}

interface BasicItemCallback





