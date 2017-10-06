package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.tasks.release.ReleaseTask
import org.junit.Assert
import org.junit.Test

class ReleaseTaskTest {

    @Test
    void doesGetNextVersion() {
        Assert.assertEquals('1.2.4-SNAPSHOT', ReleaseTask.getNextVersion('1.2.3', '-SNAPSHOT'))
        Assert.assertEquals('2.0.1-SNAPSHOT', ReleaseTask.getNextVersion('2.0.0', '-SNAPSHOT'))
        Assert.assertEquals('2.1.1-SNAPSHOT', ReleaseTask.getNextVersion('2.1.0', '-SNAPSHOT'))
    }
}
