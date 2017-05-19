# The seaside parent gradle build plugin.
The gradle plugin that all seaside projects will use to provide a common build structure.

# Features
The seaside gradle parent plugin provides a base gradle build for all seaside projects. This plugin will ensure your
bundles are named correctly, provide an OSGi enabled jar file (bundle), generate javadocs jar, generate sources jar
and provide the default configuration for deploying these artifacts to Nexus.

## This plugin requires properties in your gradle.properties file (usually ~/.gradle/gradle.properties):
* nexusUsername     : the username to use when uploading artifacts to nexus
* nexusPassword     : the password to use when uploading artifacts to nexus
* nexusReleases     : url to the releases repository
* nexusSnapshots    : url to the snapshots repository
* nexusConsolidated : url to the maven public download site usually a proxy to maven central and the
releases and snapshots

# Using this plugin
To use the plugin you will need to add the classpath to your buildscript dependencies and then just apply the plugin.
An example is below. Note: a newer version may exist. Check the Nexus repository for the latest version.

```java
buildscript {
    repositories {
        mavenLocal()

        maven {
            url nexusConsolidated
        }
    }

    dependencies {
        classpath 'com.ngc.seaside:seaside.parent:1.0'
    }
}

subprojects {
    apply plugin: 'com.ngc.seaside.parent'

    group = 'com.ngc.seaside'
    version = '1.0-SNAPSHOT'

}
```