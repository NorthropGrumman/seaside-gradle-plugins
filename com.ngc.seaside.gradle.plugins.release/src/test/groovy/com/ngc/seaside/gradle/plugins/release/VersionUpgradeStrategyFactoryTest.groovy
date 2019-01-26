/*
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2019, Northrop Grumman Systems Corporation
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of
 * Northrop Grumman Systems Corporation. The intellectual and technical concepts
 * contained herein are proprietary to Northrop Grumman Systems Corporation and
 * may be covered by U.S. and Foreign Patents or patents in process, and are
 * protected by trade secret or copyright law. Dissemination of this information
 * or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from Northrop Grumman.
 */
package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.plugins.version.VersionUpgradeStrategyFactory

import org.gradle.api.GradleException
import org.junit.Assert
import org.junit.Test

class VersionUpgradeStrategyFactoryTest {

    private static final String SUFFIX = '-SNAPSHOT'

    @Test
    void doesParseVersionInfo() {
        def info = VersionUpgradeStrategyFactory.parseVersionInfo("1.2.3")
        Assert.assertEquals(1, info.major)
        Assert.assertEquals(2, info.minor)
        Assert.assertEquals(3, info.patch)
    }

    @Test(expected = GradleException)
    void doesParseVersionInfo_wrongPattern() {
        VersionUpgradeStrategyFactory.parseVersionInfo("42")
    }

    @Test
    void doesCreateMajorVersionUpgradeStrategy() {
        def strategy = VersionUpgradeStrategyFactory.createMajorVersionUpgradeStrategy(SUFFIX)
        Assert.assertEquals("2.0.0", strategy.getVersion("1.2.3-SNAPSHOT"))
    }

    @Test
    void doesCreateMajorVersionUpgradeStrategyWithoutSnapshot() {
        def strategy = VersionUpgradeStrategyFactory.createMajorVersionUpgradeStrategy(SUFFIX)
        Assert.assertEquals("2.0.0", strategy.getVersion("1.2.3"))
    }

    @Test
    void doesCreateMinorVersionUpgradeStrategy() {
        def strategy = VersionUpgradeStrategyFactory.createMinorVersionUpgradeStrategy(SUFFIX)
        Assert.assertEquals("1.3.0", strategy.getVersion("1.2.3-SNAPSHOT"))
    }

   @Test
    void doesCreateMinorVersionUpgradeStrategyWithoutSnapshot() {
        def strategy = VersionUpgradeStrategyFactory.createMinorVersionUpgradeStrategy(SUFFIX)
        Assert.assertEquals("1.3.0", strategy.getVersion("1.2.3"))
    }

    @Test
    void doesCreatePatchVersionUpgradeStrategy() {
        def strategy = VersionUpgradeStrategyFactory.createPatchVersionUpgradeStrategy(SUFFIX)
        Assert.assertEquals("1.2.4", strategy.getVersion("1.2.3-SNAPSHOT"))
    }

    @Test
    void doesCreatePatchVersionUpgradeStrategyWithoutSnapshot() {
        def strategy = VersionUpgradeStrategyFactory.createPatchVersionUpgradeStrategy(SUFFIX)
        Assert.assertEquals("1.2.4", strategy.getVersion("1.2.3"))
    }

    @Test
    void doesCreateSnapshotVersionUpgradeStrategy() {
        def strategy = VersionUpgradeStrategyFactory.createSnapshotVersionUpgradeStrategy()
        Assert.assertEquals("1.2.3-SNAPSHOT", strategy.getVersion("1.2.3-SNAPSHOT"))
    }

    @Test
    void doesCreateSnapshotVersionUpgradeStrategyWithoutSnapshot() {
        def strategy = VersionUpgradeStrategyFactory.createSnapshotVersionUpgradeStrategy()
        Assert.assertNotEquals("1.2.3-SNAPSHOT", strategy.getVersion("1.2.3"))
    }

}
