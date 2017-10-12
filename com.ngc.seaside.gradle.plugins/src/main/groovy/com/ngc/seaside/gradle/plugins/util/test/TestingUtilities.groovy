package com.ngc.seaside.gradle.plugins.util.test

class TestingUtilities {

    static List<File> getTestClassPath(Class clazz) {
        URL pluginClasspathResource = clazz.classLoader.getResource("plugin-classpath.txt")
        if (pluginClasspathResource == null) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
        }

        return pluginClasspathResource.readLines().collect { new File(it) }
    }
}
