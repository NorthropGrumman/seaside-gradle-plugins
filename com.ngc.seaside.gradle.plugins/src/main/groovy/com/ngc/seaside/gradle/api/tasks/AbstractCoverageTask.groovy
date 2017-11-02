package com.ngc.seaside.gradle.api.tasks

import org.gradle.api.DefaultTask

class AbstractCoverageTask extends DefaultTask {

    /**
     * Finds the lcov archive location within the project classpath
     * @param filename archive filename
     * @return path to archive filename
     */
    protected String findTheReleaseArchiveFile(String filename) {
        return project.configurations.getByName("compile").filter { file ->
            return file.name.endsWith(filename)
        }.getAsPath()
    }
}
