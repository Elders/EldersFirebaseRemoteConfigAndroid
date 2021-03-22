package com.elders.EldersFirebaseRemoteConfig

import android.content.Context
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * A type that represents an application update.
 */
@Serializable
data class ApplicationUpdate(

    /**
     * The version of the update.
     */
    @Serializable(ApplicationUpdateVersionSerializer::class)
    var version: Version,

    /**
     * The URL at which the update can be downloaded.
     */
    var download: String
) {

    /**
     * A type representing a version in the following format: `X.Y.Z.a`
     * - X is the major number (required)
     * - Y is the minor number (required)
     * - Z is the patch number (required)
     * - a is the build number (optional)
     */

    @Serializable
    data class Version(val rawValue: String) {

        val major: Int
        val minor: Int
        val patch: Int
        val build: Int?

        init {

            val components = rawValue.split(".").mapNotNull { it.toIntOrNull() }
            require(components.count() in 3..4)

            this.major = components[0]
            this.minor = components[1]
            this.patch = components[2]

            if (components.count() == 4) {

                this.build = components[3]
            }
            else {

                this.build = null
            }
        }

        operator fun compareTo(other: Version): Int {

            val major = this.major.compareTo(other.major)
            if (major != 0) {

                return major
            }

            val minor = this.minor.compareTo(other.minor)
            if (minor != 0) {

                return minor
            }

            val patch = this.patch.compareTo(other.patch)
            if (patch != 0) {

                return patch
            }

            val tb = this.build ?: 0
            val ob = other.build ?: 0

            return tb.compareTo(ob)
        }
    }

    /**
     * Returns whenever the update can be applied to the current app.
     * This function compares whenever the application `CFBundleVersion` is less than the one in the receiver and returns `true`. Otherwise returns `false`.
     */
    fun isApplicable(context: Context): Boolean {

        return try {

            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val applicationVersion = Version(packageInfo.versionName)
            applicationVersion < this.version
        }
        catch (e: Throwable) { false }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = ApplicationUpdate.Version::class)
private object ApplicationUpdateVersionSerializer : KSerializer<ApplicationUpdate.Version> {

    override fun deserialize(decoder: Decoder): ApplicationUpdate.Version {

        return ApplicationUpdate.Version(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: ApplicationUpdate.Version) {

        encoder.encodeString(value.toString())
    }
}
