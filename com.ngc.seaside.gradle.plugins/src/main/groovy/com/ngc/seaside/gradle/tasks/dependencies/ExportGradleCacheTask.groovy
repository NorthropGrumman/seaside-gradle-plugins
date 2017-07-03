package com.ngc.seaside.gradle.tasks.dependencies

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.util.regex.Pattern

class ExportGradleCacheTask extends DefaultTask {

    String customRepo
    /**
     * This task exports project dependencies from the gradle cache to a local maven repository
     */
    @TaskAction
    private void exportGradleCache() {

        String userHome = System.getProperty("user.home")
        String gradleUserHome = System.getenv("GRADLE_USER_HOME")
        String gradleCachePath
        if (gradleUserHome == null) {
            // User default gradle home, In case user did not set a gradleUserHome
            gradleCachePath = userHome.concat(File.separator).concat("/caches/modules-2/files-2.1")
        } else {
            gradleCachePath = gradleUserHome.concat("/caches/modules-2/files-2.1")
        }
        gradleCachePath = separatorsToSystem(gradleCachePath)

        if (customRepo == null) {
            customRepo = userHome.concat(File.separator).concat(".m2/repository")
        }
        customRepo = separatorsToSystem(customRepo)

        println("Exporting dependencies from:" + gradleCachePath + " to:" + customRepo)
        def inDirPath = gradleCachePath

        def outDirStr = customRepo

        def outDir = new File(outDirStr)

        if (!outDir.exists() && !outDir.mkdirs()) {
            throw new Exception("Can't create output repository")
        }

        def dirsToScan = new File(inDirPath)

        dirsToScan.eachFileRecurse { artifact ->

            if (!artifact.directory) {
                // Obtain parent directories to build new path srtucture
                def parent = artifact.parent.substring(inDirPath.length() + 1)

                // Transform path structure
                String[] parentSplit = parent.split(Pattern.quote(File.separator));

                def transform = parentSplit[0].split(Pattern.quote(".")).
                        inject("") { result, i -> result + File.separator + i }
                transform = transform.substring(1)

                String newArtifactParentPath = outDirStr.concat(File.separator)
                        .concat(transform).concat(File.separator)
                        .concat(parentSplit[1]).concat(File.separator)
                        .concat(parentSplit[2])

                String newArtifactPath = newArtifactParentPath.concat(File.separator).concat(artifact.name)

                // create parent directory
                def dir_ = new File(newArtifactParentPath)
                if (!dir_.exists()) {
                    dir_.mkdirs()
                }

                // save new artifact
                def outputFile = new File(newArtifactPath)
                if (!outputFile.exists()) {
                    outputFile << artifact.bytes
                }
            }

        }
    }

    private String separatorsToSystem(String path) {
        if (path == null) {
            return null
        }

        if (File.separatorChar == (char) '\\') {
            // From Windows to Linux/Mac
            return path.replace((char) '/', File.separatorChar)
        } else {
            // From Linux/Mac to Windows
            return path.replace((char) '\\', File.separatorChar)
        }
    }
}
