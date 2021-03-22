package com.elders.EldersFirebaseRemoteConfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.serialization.json.Json

object FirebaseRemoteConfigKey {

    /**
     * The key for a required update
     */
    var requiredUpdate: String = "android_required_update"

    /**
     * The key for a recommended update
     */
    var recommendedUpdate: String = "android_recommended_update"
}

/**
 * Returns the required update, if any.
 */
val FirebaseRemoteConfig.requiredUpdate: ApplicationUpdate? get() {

    return try {

        val jsonSerializer = Json { ignoreUnknownKeys = true }
        jsonSerializer.decodeFromString( ApplicationUpdate.serializer(), this.getString(FirebaseRemoteConfigKey.requiredUpdate))
    }
    catch (e: Throwable) {

        null
    }
}

/**
 * Returns the recommended update, if any.
 */
val FirebaseRemoteConfig.recommendedUpdate: ApplicationUpdate? get() {

    return try {

        val jsonSerializer = Json { ignoreUnknownKeys = true }
        jsonSerializer.decodeFromString( ApplicationUpdate.serializer(), this.getString(FirebaseRemoteConfigKey.recommendedUpdate))
    }
    catch (e: Throwable) {

        null
    }
}