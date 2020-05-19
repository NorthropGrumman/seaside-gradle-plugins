/*
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
