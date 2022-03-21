package com.instructure.canvasapi2.unit

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Tasks
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.instructure.canvasapi2.utils.PrefRepoInterface
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

// Class emulates a shared preference repository
class FakeRepo : PrefRepoInterface {
    val localVals = emptyMap<String,Any>().toMutableMap()

    override fun getInt(key: String, default: Int): Int {
        return localVals.getOrDefault(key, default) as Int
    }

    override fun putInt(key: String, value: Int) {
        localVals.put(key, value)
    }

    override fun getBoolean(key: String, default: Boolean): Boolean {
        return localVals.getOrDefault(key, default) as Boolean
    }

    override fun putBoolean(key: String, value: Boolean) {
        localVals.put(key, value)
    }

    override fun getString(key: String, default: String?): String? {
        return localVals.get(key)?.toString() ?: default
    }

    override fun putString(key: String, value: String) {
        localVals.put(key, value)
    }

    override fun getFloat(key: String, default: Float): Float {
        return localVals.getOrDefault(key, default) as Float
    }

    override fun putFloat(key: String, value: Float) {
        localVals.put(key, value)
    }

    override fun getLong(key: String, default: Long): Long {
        return localVals.getOrDefault(key, default) as Long
    }

    override fun putLong(key: String, value: Long) {
        localVals.put(key, value)
    }

    fun clear() {
        localVals.clear()
    }

}

class RemoteConfigUtilsTest : Assert() {

    val localPrefs = FakeRepo()
    lateinit var config : FirebaseRemoteConfig

    @Before
    fun setUp() {
        localPrefs.clear()
        config = mockk<FirebaseRemoteConfig>(relaxed = true)

        // Reset the "initialized" field in RemoteConfigUtils
        var field = RemoteConfigUtils::class.java.getDeclaredField("initialized")
        field.isAccessible = true
        field.set(RemoteConfigUtils, false)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // Simulate a successful fetch of the values and verify that all works as expected.
    @Test
    fun `test simulated fetch`() {
        // Simulate fetched values in the FirebaseRemoteConfig object
        every {config.getString("test_bool")} returns "true"
        every {config.getString("test_string")} returns "hello"
        every {config.getString("test_float")} returns "3.14"
        every {config.getString("test_long")} returns  "101010"
        every {config.getKeysByPrefix("")} returns setOf("test_bool", "test_string", "test_float", "test_long")
        every {config.fetchAndActivate().addOnCompleteListener(any<OnCompleteListener<Boolean>>())} answers {
            val rval = Tasks.forResult(true)
            (this.args[0] as OnCompleteListener<Boolean>).onComplete(rval)
            rval
        }

        RemoteConfigUtils.initialize(config, localPrefs)

        assertEquals("true",RemoteConfigUtils.getString(RemoteConfigParam.TEST_BOOL))
        assertEquals(true, RemoteConfigUtils.getBoolean(RemoteConfigParam.TEST_BOOL))
        assertEquals("hello",RemoteConfigUtils.getString(RemoteConfigParam.TEST_STRING))
        assertEquals("3.14", RemoteConfigUtils.getString(RemoteConfigParam.TEST_FLOAT))
        assertEquals(3.14f, RemoteConfigUtils.getFloat(RemoteConfigParam.TEST_FLOAT)!!, 0.01f)
        assertEquals("101010",RemoteConfigUtils.getString(RemoteConfigParam.TEST_LONG))
        assertEquals(101010L, RemoteConfigUtils.getLong(RemoteConfigParam.TEST_LONG)!!)
    }

    // Grab vals when initialization does no fetch and verify that the "safe" vals are returned
    @Test
    fun `test default vals`() {
        RemoteConfigUtils.initialize(config, localPrefs)
        RemoteConfigParam.values().forEach {
            assertEquals(it.safeValueAsString, RemoteConfigUtils.getString(it))
        }
    }

    // Verifies that type conversions are working as expected.
    @Test
    fun `test type conversions`() {
        RemoteConfigUtils.initialize(config, localPrefs)

        localPrefs.putBoolean(RemoteConfigParam.TEST_BOOL.rc_name, true)
        assertEquals(true, RemoteConfigUtils.getBoolean(RemoteConfigParam.TEST_BOOL))
        assertEquals("true", RemoteConfigUtils.getString(RemoteConfigParam.TEST_BOOL))

        localPrefs.putFloat(RemoteConfigParam.TEST_FLOAT.rc_name, 3.14f)
        assertEquals(3.14f, RemoteConfigUtils.getFloat(RemoteConfigParam.TEST_FLOAT)!!, 0.01f)
        assertEquals("3.14", RemoteConfigUtils.getString(RemoteConfigParam.TEST_FLOAT))

        localPrefs.putLong(RemoteConfigParam.TEST_LONG.rc_name, 101)
        assertEquals(101L, RemoteConfigUtils.getLong(RemoteConfigParam.TEST_LONG))
        assertEquals("101", RemoteConfigUtils.getString(RemoteConfigParam.TEST_LONG))
    }

    @Test(expected = IllegalStateException::class)
    fun `throws on use without initialization`() {
        RemoteConfigUtils.getString(RemoteConfigParam.TEST_STRING)
    }
}
