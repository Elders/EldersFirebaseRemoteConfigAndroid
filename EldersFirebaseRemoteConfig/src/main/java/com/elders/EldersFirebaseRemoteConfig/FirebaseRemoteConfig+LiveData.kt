package com.elders.EldersFirebaseRemoteConfig

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import java.util.*

private val remoteConfigLiveData: MutableLiveData<Any> = MutableLiveData(UUID.randomUUID().toString())

/**
 * A LiveData that posts updates when the receiver fetch and activate updates using the `update()` function
 */
val FirebaseRemoteConfig.updates: LiveData<Any> get() = remoteConfigLiveData

/**
 * Update the remote config - this calls `fetchAndActivate` and post event trough the `updates` live data
 */
fun FirebaseRemoteConfig.update() {

    fetchAndActivate().addOnCompleteListener {

        remoteConfigLiveData.postValue(UUID.randomUUID().toString())
    }
}