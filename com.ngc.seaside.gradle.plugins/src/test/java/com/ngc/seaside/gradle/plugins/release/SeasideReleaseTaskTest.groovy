package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.tasks.release.SeasideReleaseTask
import org.junit.Assert
import org.junit.Test

class SeasideReleaseTaskTest {

    @Test
    void doesGetNextVersion() {
        Assert.assertEquals('1.2.4-SNAPSHOT', SeasideReleaseTask.getNextVersion('1.2.3', '-SNAPSHOT'))
    }
}
