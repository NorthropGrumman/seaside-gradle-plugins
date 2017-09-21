package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.tasks.release.SeasideReleaseTask
import org.junit.Assert
import org.junit.Test

class SeasideReleaseTaskTest {

    @Test
    void doesGetNextVersion() {
        Assert.assertEquals('1.2.4-SNAPSHOT', SeasideReleaseTask.getNextVersion('1.2.3', '-SNAPSHOT'))
        Assert.assertEquals('2.0.1-SNAPSHOT', SeasideReleaseTask.getNextVersion('2.0.0', '-SNAPSHOT'))
        Assert.assertEquals('2.1.1-SNAPSHOT', SeasideReleaseTask.getNextVersion('2.1.0', '-SNAPSHOT'))
    }
}
