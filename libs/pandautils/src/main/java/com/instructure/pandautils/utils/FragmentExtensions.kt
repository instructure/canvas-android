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
 */
@file:Suppress("unused")

package com.instructure.pandautils.utils

import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.instructure.pandautils.R
import java.io.Serializable
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/** Show a toast with a default length of Toast.LENGTH_SHORT */
fun Fragment.toast(messageResId: Int, length: Int = Toast.LENGTH_SHORT) { if(context != null) Toast.makeText(context, messageResId, length).show() }

fun Fragment.getModuleItemId(): Long? {
    return arguments?.getLong(Const.MODULE_ITEM_ID, -1)?.takeUnless { it < 0 } ?: parentFragment?.getModuleItemId()
}

val Fragment.peekingFragment: Fragment?
    get() {
        val fm = activity?.supportFragmentManager ?: return null
        val stackSize = fm.backStackEntryCount
        if (stackSize > 1) {
            val fragmentTag = fm.getBackStackEntryAt(stackSize - 2).name
            return fm.findFragmentByTag(fragmentTag)
        }
        return null
    }

/**
 * Dismisses an existing instance of the specified DialogFragment class. This only works for
 * dialogs tagged with the class's simple name.
 */
inline fun <reified D : DialogFragment> FragmentManager.dismissExisting() {
    (findFragmentByTag(D::class.java.simpleName) as? D)?.dismissAllowingStateLoss()
}

fun <T : Fragment> T.withArgs(argBlock: Bundle.() -> Unit): T {
    val args = arguments ?: Bundle()
    argBlock(args)
    arguments = args
    return this
}

fun <T : Fragment> T.withArgs(bundle: Bundle): T {
    nonNullArgs.putAll(bundle)
    return this
}

/** Gets the fragment's existing args bundle if it exists, or creates and attaches a new bundle if it doesn't */
val Fragment.nonNullArgs: Bundle
    get() = arguments ?: Bundle().apply { this@nonNullArgs.arguments = this }

val Fragment.isTablet: Boolean
    get() = requireContext().resources.getBoolean(R.bool.isDeviceTablet)

/** Convenience delegates for fragment arguments */
class IntArg(val default: Int = 0, val key: String? = null) : ReadWriteProperty<Fragment, Int> {
    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: Int) = thisRef.nonNullArgs.putInt(key ?: property.name, value)
    override fun getValue(thisRef: Fragment, property: KProperty<*>) = thisRef.arguments?.getInt(key ?: key ?: property.name, default) ?: default
}

class BooleanArg(val default: Boolean = false, val key: String? = null) : ReadWriteProperty<Fragment, Boolean> {
    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: Boolean) = thisRef.nonNullArgs.putBoolean(key ?: property.name, value)
    override fun getValue(thisRef: Fragment, property: KProperty<*>) = thisRef.arguments?.getBoolean(key ?: key ?: property.name, default) ?: default
}

class LongArg(val default: Long = 0L, val key: String? = null) : ReadWriteProperty<Fragment, Long> {
    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: Long) = thisRef.nonNullArgs.putLong(key ?: property.name, value)
    override fun getValue(thisRef: Fragment, property: KProperty<*>) = thisRef.arguments?.getLong(key ?: key ?: property.name, default) ?: default
}

class NLongArg(val default: Long? = null, val key: String? = null) : ReadWriteProperty<Fragment, Long?> {
    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: Long?) {
        val keyName = key ?: property.name
        if (value == null) {
            thisRef.nonNullArgs.remove(keyName)
        } else {
            thisRef.nonNullArgs.putLong(keyName, value)
        }
    }
    override fun getValue(thisRef: Fragment, property: KProperty<*>): Long? {
        val keyName = key ?: property.name
        return if (thisRef.arguments?.containsKey(keyName) == true) {
            thisRef.requireArguments().getLong(keyName, 0L)
        } else {
            default
        }
    }
}

class LongArrayArg(val default: LongArray = longArrayOf(), val key: String? = null) : ReadWriteProperty<Fragment, LongArray> {
    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: LongArray) = thisRef.nonNullArgs.putLongArray(key ?: key ?: property.name, value)
    override fun getValue(thisRef: Fragment, property: KProperty<*>) = thisRef.arguments?.getLongArray(key ?: key ?: property.name) ?: default
}

class FloatArg(val default: Float = 0f, val key: String? = null) : ReadWriteProperty<Fragment, Float> {
    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: Float) = thisRef.nonNullArgs.putFloat(key ?: property.name, value)
    override fun getValue(thisRef: Fragment, property: KProperty<*>) = thisRef.arguments?.getFloat(key ?: property.name, default) ?: default
}

class DoubleArg(val default: Double = 0.0, val key: String? = null) : ReadWriteProperty<Fragment, Double> {
    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: Double) = thisRef.nonNullArgs.putDouble(key ?: property.name, value)
    override fun getValue(thisRef: Fragment, property: KProperty<*>) = thisRef.arguments?.getDouble(key ?: property.name, default) ?: default
}

class StringArg(val default: String = "", val key: String? = null) : ReadWriteProperty<Fragment, String> {
    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: String) = thisRef.nonNullArgs.putString(key ?: property.name, value)
    override fun getValue(thisRef: Fragment, property: KProperty<*>) = thisRef.arguments?.getString(key ?: property.name, default) ?: default
}

class StringArrayArg(val default: Array<String> = emptyArray(), val key: String? = null) : ReadWriteProperty<Fragment, Array<String>> {
    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: Array<String>) = thisRef.nonNullArgs.putStringArray(key ?: property.name, value)
    override fun getValue(thisRef: Fragment, property: KProperty<*>) = thisRef.arguments?.getStringArray(key ?: property.name) ?: default
}

class NullableStringArg(val key: String? = null) : ReadWriteProperty<Fragment, String?> {
    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: String?) = thisRef.nonNullArgs.putString(key ?: property.name, value)
    override fun getValue(thisRef: Fragment, property: KProperty<*>) = thisRef.arguments?.getString(key ?: property.name)
}

class ParcelableArg<T : Parcelable>(val default: T? = null, val key: String? = null) : ReadWriteProperty<Fragment, T> {
    override fun getValue(thisRef: Fragment, property: KProperty<*>): T = thisRef.arguments?.getParcelable(key ?: property.name) ?: default ?: throw IllegalStateException("Parcelable arg '${key ?: property.name}' has not been set!")
    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) = thisRef.nonNullArgs.putParcelable(key ?: property.name, value)
}

class NullableParcelableArg<T : Parcelable>(val default: T? = null, val key: String? = null) : ReadWriteProperty<Fragment, T?> {
    override fun getValue(thisRef: Fragment, property: KProperty<*>): T? = thisRef.arguments?.getParcelable(key ?: key ?: property.name) ?: default
    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T?) = thisRef.nonNullArgs.putParcelable(key ?: key ?: property.name, value)
}

class ParcelableArrayListArg<T : Parcelable>(val default: ArrayList<T> = arrayListOf(), val key: String? = null) : ReadWriteProperty<Fragment, ArrayList<T>> {
    override fun getValue(thisRef: Fragment, property: KProperty<*>): ArrayList<T> = thisRef.arguments?.getParcelableArrayList(key ?: property.name) ?: default
    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: ArrayList<T>) = thisRef.nonNullArgs.putParcelableArrayList(key ?: property.name, value)
}

class ParcelableListArg<T : Parcelable>(val default: ArrayList<T> = arrayListOf(), val key: String? = null) : ReadWriteProperty<Fragment, List<T>> {
    override fun getValue(thisRef: Fragment, property: KProperty<*>): List<T> = thisRef.arguments?.getParcelableArrayList(key ?: property.name) ?: default
    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: List<T>) = thisRef.nonNullArgs.putParcelableArrayList(key ?: property.name, ArrayList(value))
}

@Suppress("UNCHECKED_CAST")
class SerializableArg<T : Serializable>(val default: T, val key: String? = null) : ReadWriteProperty<Fragment, T> {
    override fun getValue(thisRef: Fragment, property: KProperty<*>): T = thisRef.arguments?.getSerializable(key ?: property.name) as? T ?: default
    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) = thisRef.nonNullArgs.putSerializable(key ?: property.name, value)
}

@Suppress("UNCHECKED_CAST")
class NullableSerializableArg<T : Serializable>(val key: String? = null) : ReadWriteProperty<Fragment, T?> {
    override fun getValue(thisRef: Fragment, property: KProperty<*>): T? = thisRef.arguments?.getSerializable(key ?: property.name) as? T
    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T?) = thisRef.nonNullArgs.putSerializable(key ?: property.name, value)
}

@Suppress("UNCHECKED_CAST")
class BlindSerializableArg<T : Any?>(val default: T? = null, val key: String? = null) : ReadWriteProperty<Fragment, T?> {
    var cache: T? = null
    override fun getValue(thisRef: Fragment, property: KProperty<*>): T? = cache ?: thisRef.arguments?.getSerializable(key ?: property.name) as? T ?: default
    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T?) {
        cache = value
        thisRef.nonNullArgs.putSerializable(key ?: property.name, value as? Serializable)
    }
}

@Suppress("UNCHECKED_CAST")
class SerializableListArg<T : Serializable>(val default: List<T>, val key: String? = null)  : ReadWriteProperty<Fragment, List<T>> {
    override fun getValue(thisRef: Fragment, property: KProperty<*>): List<T> = thisRef.arguments?.getSerializable(key ?: property.name) as? List<T> ?: default
    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: List<T>) = thisRef.nonNullArgs.putSerializable(key ?: property.name, ArrayList(value))
}
@Suppress("UNCHECKED_CAST")
class SerializableMutableListArg<T : Serializable>(val default: MutableList<T>, val key: String? = null) : ReadWriteProperty<Fragment, MutableList<T>> {
    override fun getValue(thisRef: Fragment, property: KProperty<*>): MutableList<T> = thisRef.arguments?.getSerializable(key ?: property.name) as? MutableList<T> ?: default
    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: MutableList<T>) = thisRef.nonNullArgs.putSerializable(key ?: property.name, ArrayList(value))
}
