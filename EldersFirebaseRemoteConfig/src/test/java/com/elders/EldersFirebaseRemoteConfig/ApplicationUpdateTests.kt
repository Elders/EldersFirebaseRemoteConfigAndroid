package com.elders.EldersFirebaseRemoteConfig

import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Test

fun assertException(code: () -> Unit) {

    val assertPassed = try {

        code()
        false
    }
    catch (e: Throwable) {
        true
    }

    Assert.assertTrue(assertPassed)
}

class ApplicationUpdateTests {

    //Version raw value must exactly match 'X.Y.Z.a' where
    //- X is the major number (required)
    //- Y is the minor number (required)
    //- Z is the patch number (required)
    //- a is the build number (optional)

    @Test
    fun testInvalidVersionInitialization() {

        assertException { ApplicationUpdate.Version("1") }
        assertException { ApplicationUpdate.Version("1.2") }
        assertException { ApplicationUpdate.Version("1.2.3.4.5") }
    }

    @Test
    fun testVersionInitializationWithoutBuild() {

        val version = ApplicationUpdate.Version("1.2.3")
        Assert.assertEquals(version.major, 1)
        Assert.assertEquals(version.minor, 2)
        Assert.assertEquals(version.patch, 3)
        Assert.assertNull(version.build)
    }

    @Test
    fun testVersionInitializationWithBuild() {

        val version = ApplicationUpdate.Version("1.2.3.4")
        Assert.assertEquals(version.major, 1)
        Assert.assertEquals(version.minor, 2)
        Assert.assertEquals(version.patch, 3)
        Assert.assertEquals(version.build, 4)
    }

    @Test
    fun testVersionComparisonWithBuild() {

        Assert.assertTrue(ApplicationUpdate.Version("1.2.3.4") < ApplicationUpdate.Version("2.2.3.4"))
        Assert.assertTrue(ApplicationUpdate.Version("1.2.3.4") < ApplicationUpdate.Version("1.3.3.4"))
        Assert.assertTrue(ApplicationUpdate.Version("1.2.3.4") < ApplicationUpdate.Version("1.2.4.4"))
        Assert.assertTrue(ApplicationUpdate.Version("1.2.3.4") < ApplicationUpdate.Version("1.2.3.5"))

        Assert.assertEquals(ApplicationUpdate.Version("1.2.3.4"), ApplicationUpdate.Version("1.2.3.4"))
    }

    @Test
    fun testVersionComparisonWithoutBuild() {

        Assert.assertTrue(ApplicationUpdate.Version("1.2.3") < ApplicationUpdate.Version("2.2.3"))
        Assert.assertTrue(ApplicationUpdate.Version("1.2.3") < ApplicationUpdate.Version("1.3.3"))
        Assert.assertTrue(ApplicationUpdate.Version("1.2.3") < ApplicationUpdate.Version("1.2.4"))

        Assert.assertEquals(ApplicationUpdate.Version("1.2.3"), ApplicationUpdate.Version("1.2.3"))
    }

    @Test
    fun testVersionComparisonMixed() {

        Assert.assertTrue(ApplicationUpdate.Version("1.2.3") < ApplicationUpdate.Version("1.2.3.4"))
        Assert.assertTrue(ApplicationUpdate.Version("1.2.3") < ApplicationUpdate.Version("2.2.3.4"))
        Assert.assertTrue(ApplicationUpdate.Version("1.2.3") < ApplicationUpdate.Version("1.3.3.4"))
        Assert.assertTrue(ApplicationUpdate.Version("1.2.3") < ApplicationUpdate.Version("1.2.4.4"))

        Assert.assertNotEquals(ApplicationUpdate.Version("1.2.3"), ApplicationUpdate.Version("1.2.3.4"))
    }

    @Test
    fun testApplicationUpdateSerialization() {

        val json = "{\"version\":\"1.2.3.4\",\"download\":\"https://apps.apple.com/us/app/pr%C3%BCvit-every-day/id1544930289\"}"
        val update = Json { ignoreUnknownKeys = true }.decodeFromString(ApplicationUpdate.serializer(), json)

        Assert.assertEquals(update.version.major, 1)
        Assert.assertEquals(update.version.minor, 2)
        Assert.assertEquals(update.version.patch, 3)
        Assert.assertEquals(update.version.build, 4)
        Assert.assertEquals(update.download, "https://apps.apple.com/us/app/pr%C3%BCvit-every-day/id1544930289")
    }
}