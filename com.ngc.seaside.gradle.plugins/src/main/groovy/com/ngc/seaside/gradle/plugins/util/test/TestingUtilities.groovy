package com.ngc.seaside.gradle.plugins.util.test

import java.nio.file.Paths

class TestingUtilities {
    static List<File> getTestClassPath(Class c) {
        URL r = getThePluginClassPathResource(c)
        throwIfTheClasspathResourceIsNotFound(r)
        return createNewFileForEachItemInClasspath(r)
    }

    private static URL getThePluginClassPathResource(Class c) {
        return c.classLoader.getResource("plugin-classpath.txt")
    }

    private static void throwIfTheClasspathResourceIsNotFound(URL r) {
        if (!r)
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
    }

    private static List<File> createNewFileForEachItemInClasspath(URL r) {
        return r.readLines().collect { new File(it) }
    }

    static File turnListIntoPath(String... list) {
        return Paths.get(list.flatten().join(File.separator)).toFile()
    }
}
