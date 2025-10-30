package com.instructure.canvasapi2.utils

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

/**
 * All access to remote config data from our code must go through these enum values.
 * [rc_name] is the name of the parameter as specified in the remote config console.
 * [safeValueAsString] is the "safe to use" value for the parameter, specified in string form.  This
 * will be the value used before the actual value can be read from the remote config service.
 */
enum class RemoteConfigParam(val rc_name: String, val safeValueAsString: String) {
    DISCUSSION_REDESIGN("discussion_redesign", "false"),
    MOBILE_VERIFY_BETA_ENABLED("mobile_verify_beta_enabled", "true"),
    TEST_BOOL("test_bool", "false"),
    TEST_FLOAT("test_float", "0f"),
    TEST_LONG("test_long", "42"),
    TEST_STRING("test_string", "hey there"),
    SPEEDGRADER_V2("speedgrader_v2", "true"),
    TODO_REDESIGN("todo_redesign", "false")
}

/**
 * Singleton object for accessing remote config settings.
 *
 * The "source of truth" for remote config param values is the remote config shared preference
 * repo.  We will grab the current remote config values once at startup and update the
 * shared prefs from the result.
 */
object RemoteConfigUtils {

    lateinit var remoteConfig : FirebaseRemoteConfig
    lateinit var prefs : PrefRepoInterface
    var initialized : Boolean = false

    // Normal initializer
    fun initialize() {
        initialize(FirebaseRemoteConfig.getInstance(), RemoteConfigPrefs)
    }

    // Test initializer
    fun initialize(config: FirebaseRemoteConfig, prefs: PrefRepoInterface) {
        // Don't allow multiple initializations
        if(initialized) {
            throw IllegalStateException("RemoteConfigUtils initialized twice")
        }

        this.remoteConfig = config
        this.prefs = prefs

        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600) // once an hour allowed; you may want to adjust when debugging
                .build()
        remoteConfig.setConfigSettingsAsync(configSettings)

        initialized = true

        remoteConfig.fetchAndActivate().addOnCompleteListener() { task ->
            //println("rc: listener called.  successful=${task.isSuccessful}, updated=${task.result}")
            if(task.isSuccessful) {
                val updated = task.getResult()
                if(updated != null && updated) {
                    remoteConfig.getKeysByPrefix("").forEach() {
                        //println("rc: on completion: putting ($it, ${remoteConfig.getString(it)}) into prefs")
                        prefs.putString(it, remoteConfig.getString(it))
                        //println("rc: on completion: retrieved ${prefs.getString(it)}")
                    }
                }
            }
        }
    }

    /** Get the remote config value for a RemoteConfigParam in string form. */
    fun getString(key: RemoteConfigParam): String? {
        if(!initialized) {
            throw IllegalStateException("RemoteConfigUtils not initialized")
        }

        val result = prefs.getString(key.rc_name,  key.safeValueAsString)
        //println("RemoteConfigUtils.getString: rc_name=${key.rc_name}, default=${key.safeValueAsString}, result=$result")
        return result
    }

    //
    // We store all values in string form.  But if you know that your value should be Boolean/Long/Float,
    // these convenience methods are provided to make the conversion.
    //

    fun getBoolean(key: RemoteConfigParam) : Boolean {
        return getString(key).toBoolean()
    }

    fun getLong(key: RemoteConfigParam) : Long? {
        return getString(key)?.toLong()
    }

    fun getFloat(key: RemoteConfigParam) : Float? {
        return getString(key)?.toFloat()
    }
}