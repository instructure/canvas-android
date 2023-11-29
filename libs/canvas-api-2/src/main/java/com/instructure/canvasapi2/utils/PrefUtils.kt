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
@file:Suppress("unused")

package com.instructure.canvasapi2.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import androidx.annotation.ColorRes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

/**
 * [PrefManager] is a small [SharedPreferences] wrapper that aims to provide a type-safe,
 * convenient, and centralized approach to key-value persistence. No need to define dozens
 * of string constants for key names, spin up a new [SharedPreferences] instance for
 * every get/set, or figure out how to obtain a Context object in utility methods. [PrefManager]
 * leverages the power of delegated properties in Kotlin to specify the key name, value type,
 * getter, setter, and default value in a single line.
 *
 * Generally, concrete implementations of this class should be objects (rather than subclasses),
 * and generally there should only exist one PrefManager per backing preference file. The name
 * of preference file should be passed as the sole constructor parameter of [PrefManager].
 *
 * To create preference properties inside a [PrefManager], use any delegate of type [Pref], such
 * as [StringPref], [BooleanPref], [IntPref], etc. For example:
 *
 * ```
 * var myPref by StringPref()
 * ```
 *
 * For most delegate classes the property name is used as the [SharedPreferences] key name, and
 * a sane value is used as the default value. In the example above the key name is 'myPref'
 * and the default value is an empty String. However, most delegates also allow you to manually
 * specify the key name and default value:
 *
 * ```
 * var myPref by StringPref("default value", "other_key_name")
 * ```
 *
 * You may also create your own delegate classes by subclassing [Pref] and implementing
 * the 'getValue()' and 'setValue()' extension functions.
 *
 * Here is an example of a complete, simple PrefManager implementation:
 *
 * ```
 * object MyPrefs : PrefManager("my_prefs_file_name") {
 *
 *     var name by StringPref()
 *
 *     var age by IntPref()
 *
 *     override fun onClearPrefs() { } // Required override. See [onClearPrefs]
 *
 * }
 * ```
 *
 * In another file you could then access these shared preferences like so:
 *
 * ```
 * val userAge = MyPrefs.age
 * val userName = MyPrefs.name
 * MyPrefs.age = 25
 * MyPrefs.name = "Awesome Dev Guy"
 * ```
 */
@SuppressLint("CommitPrefEdits")
abstract class PrefManager(prefFileName: String) : PrefRepoInterface {

    /* The [SharedPreferences] instance which serves as the core persistence mechanism */
    internal val prefs by lazy {
        ContextKeeper.appContext.getSharedPreferences(prefFileName, Context.MODE_PRIVATE)
    }

    /* A [SharedPreferences.Editor] instance of [prefs], used for adding/updating values */
    internal val editor by lazy { prefs.edit() }

    val delegates = arrayListOf<Pref<*>>()

    internal fun registerDelegate(delegate: Pref<*>) {
        delegates += delegate
    }

    /**
     * Because [PrefManager] does not require its properties to be directly bound to the backing
     * [SharedPreferences] instance through [Pref] delegates, simply clearing the shared prefs
     * cannot guarantee that all properties will be properly reset. This function provides the
     * opportunity to manually clear properties or perform other logic, and will be executed
     * when either [clearPrefs] or [safeClearPrefs] are invoked.
     *
     * NOTE: properties preserved during the [safeClearPrefs] process will be unaffected by any
     * changes made during the execution of this function.
     */
    protected open fun onClearPrefs() {}

    /**
     * Clears this PrefManager's backing [SharedPreferences] file. This is only guaranteed to clear
     * properties which have been delegated to an instance of the [Pref] class. Which properties
     * are reset is determined by the specific [PrefManager] implementation.
     * See [onClearPrefs] for more info.
     */
    fun clearPrefs() {
        onClearPrefs()
        delegates.forEach { it.onClear() }
        editor.clear().apply()
    }

    /**
     * Supplies a base list of properties to be preserved when [safeClearPrefs] is called.
     * The default implementation provides an empty list; override this function to specify your
     * own list of properties. The list must only contain mutable properties belonging to the
     * PrefManager instance for which this function is being overridden.
     */
    open fun keepBaseProps(): List<KMutableProperty0<out Any?>> = emptyList()

    /**
     * Clears this PrefManager's backing SharedPreferences file while preserving all properties
     * passed to this function as well as all properties provided by [keepBaseProps]. This calls
     * [clearPrefs] internally and does not guarantee all non-preserved properties will be reset.
     */
    @JvmOverloads
    @Suppress("UNCHECKED_CAST")
    fun safeClearPrefs(keepProps: List<KMutableProperty0<out Any?>> = emptyList()) {
        val keeperMap = (keepProps + keepBaseProps()).associate { it as KMutableProperty0<Any?> to it.get() }
        clearPrefs()
        for ((property, value) in keeperMap) property.set(value)
    }

    private inline fun Editor.save(block: Editor.() -> Unit) { block(); apply() }

    override fun getInt(key: String, default: Int): Int = prefs.getInt(key, default)
    override fun putInt(key: String, value: Int) = editor.save { putInt(key, value) }

    override fun getBoolean(key: String, default: Boolean): Boolean = prefs.getBoolean(key, default)
    override fun putBoolean(key: String, value: Boolean) = editor.save { putBoolean(key, value) }

    override fun getString(key: String, default: String?): String? = prefs.getString(key, default)
    override fun putString(key: String, value: String) = editor.save { putString(key, value) }

    override fun getFloat(key: String, default: Float): Float = prefs.getFloat(key, default)
    override fun putFloat(key: String, value: Float) = editor.save { putFloat(key, value) }

    override fun getLong(key: String, default: Long): Long = prefs.getLong(key, default)
    override fun putLong(key: String, value: Long) = editor.save { putLong(key, value) }

    fun remove(key: String) = editor.remove(key).apply()
}

/**
 * Extend [Pref] to create concrete delegate classes for use in [PrefManager]. Subclasses are
 * required to implement a [SharedPreferences.getValue()] and [Editor.setValue()] extension
 * function to ensure data is correctly and reliably saved to and retrieved from shared prefs.
 *
 * If an implementation of this class caches values in memory and must reset them when clearing
 * shared preferences, or has other operations that must take place at the same time, this must
 * be done in [onClear].
 *
 * @param defaultValue The default value that should be retrieved for the delegated property if it has
 * not previously been set.
 * @param keyName The optional key name under which the value will be stored. This is useful
 * when converting other [SharedPreferences] implementations to [PrefManager] and the existing
 * key name does not match the desired property name. Defaults to the property name.
 */
abstract class Pref<T>(private val defaultValue: T, private val keyName: String? = null) : ReadWriteProperty<PrefManager, T> {

    open operator fun provideDelegate(thisRef: PrefManager, prop: KProperty<*>): ReadWriteProperty<PrefManager, T> {
        thisRef.registerDelegate(this)
        return this
    }

    abstract fun SharedPreferences.getValue(key: String, default: T): T
    abstract fun Editor.setValue(key: String, value: T): Editor
    override fun getValue(thisRef: PrefManager, property: KProperty<*>): T = thisRef.prefs.getValue(keyName ?: property.name, defaultValue)
    override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: T) = thisRef.editor.setValue(keyName ?: property.name, value).apply()
    abstract fun onClear()
}

/**
 * [Pref] delegate for [String] properties. May only be used in [PrefManager] implementations.
 *
 * @param defaultValue The optional value returned for the property when no value has been set
 * internally. Defaults to an empty String.
 * @param keyName The optional key name under which the property value will be stored. Defaults
 * to the property name. This is useful when converting other [SharedPreferences] implementations
 * to [PrefManager] and the required key name does not match the desired property name.
 */
class StringPref(defaultValue: String = "", keyName: String? = null) : Pref<String>(defaultValue, keyName) {
    override fun onClear() {}
    override fun SharedPreferences.getValue(key: String, default: String): String = getString(key, default) ?: default
    override fun Editor.setValue(key: String, value: String): Editor = putString(key, value)
}

/**
 * [Pref] delegate for [String] properties. May only be used in [PrefManager] implementations.
 *
 * @param defaultValue The optional value returned for the property when no value has been set
 * internally. Defaults to an empty String.
 * @param keyName The optional key name under which the property value will be stored. Defaults
 * to the property name. This is useful when converting other [SharedPreferences] implementations
 * to [PrefManager] and the required key name does not match the desired property name.
 */
class NStringPref(defaultValue: String? = null, keyName: String? = null) : Pref<String?>(defaultValue, keyName) {
    override fun onClear() {}
    override fun SharedPreferences.getValue(key: String, default: String?): String? = getString(key, default)
    override fun Editor.setValue(key: String, value: String?): Editor = putString(key, value)
}

/**
 * [Pref] delegate for [String] properties. May only be used in [PrefManager] implementations.
 *
 * @param defaultValue The optional value returned for the property when no value has been set
 * internally. Defaults to an empty String.
 * @param keyName The optional key name under which the property value will be stored. Defaults
 * to the property name. This is useful when converting other [SharedPreferences] implementations
 * to [PrefManager] and the required key name does not match the desired property name.
 */
class StringSetPref(defaultValue: Set<String> = setOf(), keyName: String? = null) : Pref<Set<String>>(defaultValue, keyName) {
    override fun onClear() {}
    override fun SharedPreferences.getValue(key: String, default: Set<String>): Set<String> = getStringSet(key, default) ?: default
    override fun Editor.setValue(key: String, value: Set<String>): Editor = putStringSet(key, value)
}

/**
 * [Pref] delegate for [Boolean] properties. May only be used in [PrefManager] implementations.
 *
 * @param defaultValue The optional value returned for the property when no value has been set
 * internally. Defaults to false.
 * @param keyName The optional key name under which the property value will be stored. Defaults
 * to the property name. This is useful when converting other [SharedPreferences] implementations
 * to [PrefManager] and the required key name does not match the desired property name.
 */
class BooleanPref(defaultValue: Boolean = false, keyName: String? = null) : Pref<Boolean>(defaultValue, keyName) {
    override fun onClear() {}
    override fun SharedPreferences.getValue(key: String, default: Boolean) = getBoolean(key, default)
    override fun Editor.setValue(key: String, value: Boolean): Editor = putBoolean(key, value)
}

/**
 * [Pref] delegate for nullable [Boolean] properties. May only be used in [PrefManager] implementations.
 *
 * @param defaultValue The optional value returned for the property when no value has been set
 * internally. Defaults to null.
 * @param keyName The optional key name under which the property value will be stored. Defaults
 * to the property name. This is useful when converting other [SharedPreferences] implementations
 * to [PrefManager] and the required key name does not match the desired property name.
 */
class NBooleanPref(defaultValue: Boolean? = null, keyName: String? = null) : Pref<Boolean?>(defaultValue, keyName) {
    override fun onClear() {}
    override fun SharedPreferences.getValue(key: String, default: Boolean?): Boolean? {
        return if (contains(key)) getBoolean(key, false) else null
    }
    override fun Editor.setValue(key: String, value: Boolean?): Editor {
        if (value == null) remove(key) else putBoolean(key, value)
        return this
    }
}

/**
 * [Pref] delegate for [Boolean] properties to be used as feature flags. The primary difference between this and a
 * normal [BooleanPref] is that this delegate class holds a human-readable description for dev convenience as well
 * as a property reference for dynamic access.
 */
class FeatureFlagPref(val description: String, defaultValue: Boolean = false, keyName: String? = null) : Pref<Boolean>(defaultValue, keyName) {

    lateinit var property : KProperty<*>

    override operator fun provideDelegate(thisRef: PrefManager, prop: KProperty<*>): ReadWriteProperty<PrefManager, Boolean> {
        thisRef.registerDelegate(this)
        property = prop
        return this
    }

    override fun onClear() {}
    override fun SharedPreferences.getValue(key: String, default: Boolean) = getBoolean(key, default)
    override fun Editor.setValue(key: String, value: Boolean): Editor = putBoolean(key, value)
}

/**
 * [Pref] delegate for a special type of [Boolean] property which resets every time the getter is called.
 * May only be used in [PrefManager] implementations.
 *
 * @param defaultValue The optional value returned for the property when no value has been set
 * internally. Defaults to false. This value is what the preference will reset to.
 * @param keyName The optional key name under which the property value will be stored. Defaults
 * to the property name. This is useful when converting other [SharedPreferences] implementations
 * to [PrefManager] and the required key name does not match the desired property name.
 */
class BooleanResetPref(private val defaultValue: Boolean = false, keyName: String? = null) : Pref<Boolean>(defaultValue, keyName) {
    override fun onClear() {}
    override fun SharedPreferences.getValue(key: String, default: Boolean) = getBoolean(key, default)
    override fun Editor.setValue(key: String, value: Boolean): Editor = putBoolean(key, value)

    override fun getValue(thisRef: PrefManager, property: KProperty<*>): Boolean {
        val value = super.getValue(thisRef, property)
        if(value != defaultValue) setValue(thisRef, property, defaultValue)
        return value
    }
}

/**
 * [Pref] delegate for [Int] properties. May only be used in [PrefManager] implementations.
 *
 * @param defaultValue The optional value returned for the property when no value has been set
 * internally. Defaults to 0.
 * @param keyName The optional key name under which the property value will be stored. Defaults
 * to the property name. This is useful when converting other [SharedPreferences] implementations
 * to [PrefManager] and the required key name does not match the desired property name.
 */
class IntPref(defaultValue: Int = 0, keyName: String? = null) : Pref<Int>(defaultValue, keyName) {
    override fun onClear() {}
    override fun SharedPreferences.getValue(key: String, default: Int) = getInt(key, default)
    override fun Editor.setValue(key: String, value: Int): Editor = putInt(key, value)
}

/**
 * [Pref] delegate for [Long] properties. May only be used in [PrefManager] implementations.
 *
 * @param defaultValue The optional value returned for the property when no value has been set
 * internally. Defaults to 0L.
 * @param keyName The optional key name under which the property value will be stored. Defaults
 * to the property name. This is useful when converting other [SharedPreferences] implementations
 * to [PrefManager] and the required key name does not match the desired property name.
 */
class LongPref(defaultValue: Long = 0L, keyName: String? = null) : Pref<Long>(defaultValue, keyName) {
    override fun onClear() {}
    override fun SharedPreferences.getValue(key: String, default: Long) = getLong(key, default)
    override fun Editor.setValue(key: String, value: Long): Editor = putLong(key, value)
}

/**
 * [Pref] delegate for [Float] properties. May only be used in [PrefManager] implementations.
 *
 * @param defaultValue The optional value returned for the property when no value has been set
 * internally. Defaults to 0f.
 * @param keyName The optional key name under which the property value will be stored. Defaults
 * to the property name. This is useful when converting other [SharedPreferences] implementations
 * to [PrefManager] and the required key name does not match the desired property name.
 */
class FloatPref(defaultValue: Float = 0f, keyName: String? = null) : Pref<Float>(defaultValue, keyName) {
    override fun onClear() {}
    override fun SharedPreferences.getValue(key: String, default: Float) = getFloat(key, default)
    override fun Editor.setValue(key: String, value: Float): Editor = putFloat(key, value)
}

/**
 * [Pref] delegate for [Int] color properties. May only be used in [PrefManager] implementations.
 *
 * @param defaultValue A required color resource identifier whose integer color value will be used
 * as the property value until the property is set.
 * @param keyName The optional key name under which the property value will be stored. Defaults
 * to the property name. This is useful when converting other [SharedPreferences] implementations
 * to [PrefManager] and the required key name does not match the desired property name.
 */
@Suppress("DEPRECATION")
class ColorPref(@ColorRes defaultValue: Int, keyName: String? = null) : Pref<Int>(defaultValue, keyName) {
    override fun onClear() {}
    override fun SharedPreferences.getValue(key: String, default: Int) = getInt(key, ContextKeeper.appContext.resources.getColor(default))
    override fun Editor.setValue(key: String, value: Int): Editor = putInt(key, value)
}

/**
 * [Pref] delegate for arbitrary, nullable properties to be stored in SharedPreferences as
 * serialized strings using Gson. May only be used in [PrefManager] implementations.
 *
 * @param defaultValue (Optional) A default value to use until this property has been set.
 * @param keyName The optional key name under which the property value will be stored. Defaults
 * to the property name. This is useful when converting other [SharedPreferences] implementations
 * to [PrefManager] and the required key name does not match the desired property name.
 */
class GsonPref<T>(
        private val klazz: Class<T>,
        defaultValue: T? = null,
        private val keyName: String? = null,
        private val async: Boolean = true
) : Pref<T?>(defaultValue, keyName) {

    private var cachedObject: T? = null

    override fun onClear() { cachedObject = null }

    override fun SharedPreferences.getValue(key: String, default: T?): T? {
        if (cachedObject == null) {
            cachedObject = Gson().fromJson<T>(getString(key, null), klazz)
        }
        return cachedObject ?: default
    }

    override fun Editor.setValue(key: String, value: T?): SharedPreferences.Editor {
        cachedObject = value
        if (value == null) {
            putString(key, null)
        } else {
            putString(key, Gson().toJson(value) ?: return this)
        }
        return this
    }

    override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: T?) {
        if (async) {
            super.setValue(thisRef, property, value)
        } else {
            thisRef.editor.setValue(keyName ?: property.name, value).commit()
        }
    }
}

/**
 * [Pref] delegate for a list of arbitrary, nullable properties to be stored in SharedPreferences
 * as a serialized string using Gson. May only be used in [PrefManager] implementations.
 *
 * @param defaultValue (Optional) A default value to use until this property has been set.
 * @param keyName The optional key name under which the property value will be stored. Defaults
 * to the property name. This is useful when converting other [SharedPreferences] implementations
 * to [PrefManager] and the required key name does not match the desired property name.
 */
class GsonListPref<T>(
        private val klazz: Class<T>,
        defaultValue: List<T> = emptyList(),
        keyName: String? = null
) : Pref<List<T>>(defaultValue, keyName) {

    private var cachedObject: List<T>? = null

    override fun onClear() { cachedObject = null }

    override fun SharedPreferences.getValue(key: String, default: List<T>): List<T> {
        if (cachedObject == null) {
            val type = TypeToken.getParameterized(List::class.java, klazz).type
            cachedObject = Gson().fromJson<List<T>>(getString(key, null), type)
        }
        return cachedObject ?: default
    }

    override fun Editor.setValue(key: String, value: List<T>): SharedPreferences.Editor {
        cachedObject = value
        putString(key, Gson().toJson(value) ?: return this)
        return this
    }
}

/**
 * [Pref] delegate for arbitrary, non-nullable properties to be stored in SharedPreferences as
 * serialized strings using Gson. May only be used in [PrefManager] implementations.
 *
 * @param defaultValue A required default value to use until this property has been set.
 * @param keyName The optional key name under which the property value will be stored. Defaults
 * to the property name. This is useful when converting other [SharedPreferences] implementations
 * to [PrefManager] and the required key name does not match the desired property name.
 */
class NonNullGsonPref<T : Any>(defaultValue: T, keyName: String? = null) : Pref<T>(defaultValue, keyName) {

    private var cachedObject: T? = null

    override fun onClear() { cachedObject = null }

    override fun SharedPreferences.getValue(key: String, default: T): T {
        if (cachedObject == null) {
            cachedObject = Gson().fromJson<T>(getString(key, null), default.javaClass) ?: return default
        }
        return cachedObject!!
    }

    override fun Editor.setValue(key: String, value: T): SharedPreferences.Editor {
        cachedObject = value
        putString(key, Gson().toJson(value))
        return this
    }
}

/**
 * [Pref] delegate for a Map<String, Boolean> to be stored in SharedPreferences as a serialized
 * string using Gson. May only be used in [PrefManager] implementations.
 *
 * @param defaultValue (Optional) A default value to use until this property has been set.
 * @param keyName The optional key name under which the property value will be stored. Defaults
 * to the property name. This is useful when converting other [SharedPreferences] implementations
 * to [PrefManager] and the required key name does not match the desired property name.
 */
class BooleanMapPref(
        defaultValue: HashMap<String, Boolean> = hashMapOf(),
        keyName: String? = null
) : Pref<HashMap<String, Boolean>>(defaultValue, keyName) {

    private var cachedObject: HashMap<String, Boolean>? = null

    override fun onClear() { cachedObject = null }

    override fun SharedPreferences.getValue(key: String, default: HashMap<String, Boolean>): HashMap<String, Boolean> {
        if (cachedObject == null) {
            cachedObject = Gson().fromJson(getString(key, null), default::class.java)
        }
        return cachedObject ?: default
    }

    override fun Editor.setValue(key: String, value: HashMap<String, Boolean>): Editor {
        cachedObject = value
        putString(key, Gson().toJson(value) ?: return this)
        return this
    }
}

/**
 * [Pref] delegate for a typed Map to be stored in SharedPreferences as a serialized
 * string using Gson. May only be used in [PrefManager] implementations.
 *
 * @param defaultValue (Optional) A default value to use until this property has been set.
 * @param keyName The optional key name under which the property value will be stored. Defaults
 * to the property name. This is useful when converting other [SharedPreferences] implementations
 * to [PrefManager] and the required key name does not match the desired property name.
 */
class GsonMapPref<K: Any, V : Any>(
    private val keyClass: Class<K>,
    private val valueClass: Class<V>,
    defaultValue: Map<K, V> = hashMapOf(),
    keyName: String? = null
) : Pref<Map<K, V>>(defaultValue, keyName) {

    private var cachedObject: Map<K, V>? = null

    override fun onClear() { cachedObject = null }

    override fun SharedPreferences.getValue(key: String, default: Map<K, V>): Map<K, V> {
        if (cachedObject == null) {
            val type = TypeToken.getParameterized(Map::class.java, keyClass, valueClass).type
            cachedObject = Gson().fromJson<Map<K, V>>(getString(key, null), type)
        }
        return cachedObject ?: default
    }

    override fun Editor.setValue(key: String, value: Map<K, V>): Editor {
        cachedObject = value
        putString(key, Gson().toJson(value) ?: return this)
        return this
    }
}

/**
 * [Pref] delegate for holding a set of objects to be stored in SharedPreferences as serialized strings using Gson.
 * This class is only designed to be used with scalar types, and behavior when using other types is undefined. This
 * class may only be used in [PrefManager] implementations.
 *
 * @param klazz The [KClass] of the object type to be held in this [Set]. Behavior of non-scalar types is undefined.
 * @param defaultValue (Optional) A default value that will be used until this property is assigned a new value.
 * @param keyName The optional key name under which the property value will be stored. Defaults
 * to the property name. This is useful when converting other [SharedPreferences] implementations
 * to [PrefManager] and the required key name does not match the desired property name.
 */
class SetPref<T : Any>(
    private val klazz: KClass<T>,
    defaultValue: Set<T> = setOf(),
    keyName: String? = null
) : Pref<Set<T>>(defaultValue, keyName) {

    private var cachedObject: Set<T>? = null

    override fun onClear() { cachedObject = null }

    override fun SharedPreferences.getValue(key: String, default: Set<T>): Set<T> {
        if (cachedObject == null) {
            val type = TypeToken.getParameterized(HashSet::class.java, klazz.javaObjectType).type
            cachedObject = Gson().fromJson<HashSet<T>>(getString(key, null), type)
        }
        return cachedObject ?: default
    }

    override fun Editor.setValue(key: String, value: Set<T>): SharedPreferences.Editor {
        cachedObject = value
        putString(key, Gson().toJson(value))
        return this
    }
}
