# com.ngc.seaside.repository
The seaside repository plugin assists in automatically configuring gradle repositories.

## Requirements
By default, this plugin requires the following properties to be set in your gradle.properties file (usually 
~/.gradle/gradle.properties):
* nexusConsolidated : url to the maven public download site usually a proxy to maven central and the
* nexusReleases     : url to the releases repository (required only when publishing a release to Nexus)
* nexusSnapshots    : url to the snapshots repository (required only when publishing a snapshot release to Nexus)
* nexusUsername     : the username to use when uploading artifacts to nexus (required only when publishing)
* nexusPassword     : the password to use when uploading artifacts to nexus (required only when publishing)

## Using this plugin
The defaults of this plugin create a maven local repository and a remote repository with the url taken from the
nexusConsolidated property. This plugin works with both the maven and maven-publish plugins. If either are applied to
the project and their corresponding uploadArchives/publish tasks are set to run, this plugin will also create an
upload repository with the url taken from either nexusReleases or nexusSnapshots depending on the project version.
The username and password will also be set for the upload repository taken from nexusUsername and nexusPassword
respectively.

```java
buildscript {
    repositories {
        mavenLocal()

        maven {
            url nexusConsolidated
        }
    }

    dependencies {
        classpath "com.ngc.seaside:gradle.plugins:$seasidePluginsVersion"
    }
}

apply plugin: 'com.ngc.seaside.repository'

group = 'com.ngc.seaside'
version = '1.0-SNAPSHOT'

```