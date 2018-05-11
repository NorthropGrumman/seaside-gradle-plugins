# com.ngc.seaside.release
The release plugin provides a gradle release plugin for my java gradle projects and essentially giving the ability to automate the necessary steps to release code to Nexus. This plugin will Initialize the project.version from a semantic version tag specified in the root build.gradle file of a project, prepare the build.gradle for next SNAPSHOT interation, push changes to Git remote(should work from any branch), and provide the default configuration for deploying these artifacts to Nexus.

The release plugin includes the release, releaseMajorVersion, and releaseMinorVersion task. The release task will create a tagged non-SNAPSHOT release of the current version as specified in the root build.gradle file.The releaseMajorVersion task upgrades to next major version & creates a tagged non-SNAPSHOT release. The releaseMinorVersion task will upgrade to the next minor version & creates a tagged non-SNAPSHOT release.

## Details

During the `application` phase, the plugin initializes the project.version according to the contents of a version.txt file **(e.g. 1.2.3-SNAPSHOT)**.

During the `configuration` phase, the plugin checks if any of the _releaseXXX_ tasks is called explicitly. It then upgrades the project.version and version.txt contents according to the following strategies:

    Task release: Remove -SNAPSHOT from current version (e.g. 1.2.3)
    Task releaseMajorVersion: Upgrade to next major version (e.g. 2.0.0)
    Task releaseMinorVersion: Upgrade to next minor version (e.g. 1.3.0)

During the `execution` phase, the _releaseXXX_ tasks tag the Git repository and prepare the version.txt contents for the next SNAPSHOT iteration. The tasks perform the following steps:

    Commit modified version.txt
    Tag Git repo (e.g. v1.2.3)
    Increment version number in version.txt to next SNAPSHOT (e.g. 1.2.4-SNAPSHOT)
    Again commit modified version.txt
    Optionally push changes to Git remote (works from any branch)


Note that all _releaseXXX_ tasks run non-interactively and are thus well suited for continuous integration.
## This plugin requires properties in your gradle.properties file (usually ~/.gradle/gradle.properties):
* nexusUsername     : the username to use when uploading artifacts to nexus
* nexusPassword     : the password to use when uploading artifacts to nexus
* nexusReleases     : url to the releases repository
* nexusSnapshots    : url to the snapshots repository
* nexusConsolidated : url to the maven public download site usually a proxy to maven central and the
releases and snapshots
* systemProp.sonar.host.url : url to the Sonarqube server

## Using this plugin
To use the plugin you will need to add the classpath to your buildscript dependencies and then just apply the plugin.
An example is below. Note: a newer version may exist. Check the Nexus repository for the latest version. If you want to
execute a Dry Run you can either
1. invoke Gradle with one of the release dry runs tasks: `releaseDryRun`, `releaseMinorDryRun`, `releaseMajorDryRun`
1. or configure the build.gradle so the following properties are set to false inside the seasideRelease section:
```
seasideRelease {
    push = false
    commitChanges = false
    uploadArtifacts = false
}
```

Below is an example of applying the plugin.
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
        classpath "org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:$sonarqubePluginVersion"
    }
}

subprojects {

    apply plugin: 'com.ngc.seaside.parent'
    apply plugin: 'com.ngc.seaside.release'

    group = 'com.ngc.seaside'
    version = '1.2.3-SNAPSHOT'

    seasideRelease {
        dependsOn subprojects*.build
        uploadArtifacts = true //'false' would be useful if you don't want to publish release to nexus also false for dry runs
        push = true //'false' would be useful when triggering the release task on a local repository also false for dry runs
        commitChanges = true // false also false for dry runs
        versionSuffix = '-SNAPSHOT' // '.DEV' or '' (empty) could be useful alternatives
        tagPrefix = 'v' // 'r' or '' (empty) could be useful alternatives
    }

    ext {
         junitVersion = '4.12'
         slf4jVersion = '1.7.22'
         log4jVersion = '1.2.17'
         cucumberVersion = '1.2.5'
    }
}
```

# com.ngc.seaside.release.root
The release plugin provides a gradle release plugin for my java gradle projects and essentially giving the ability to
automate the necessary steps to release code to Nexus. This plugin will Initialize the project.version from a semantic
version tag specified in the root build.gradle file of a project, prepare the build.gradle for next SNAPSHOT
interaction, push changes to Git remote(should work from any branch), and provide the default configuration for
deploying these artifacts to Nexus.

The release plugin includes the release, releaseMajorVersion, and releaseMinorVersion task. The release task will create
a tagged non-SNAPSHOT release of the current version as specified in the root build.gradle file.The releaseMajorVersion
task upgrades to next major version & creates a tagged non-SNAPSHOT release. The releaseMinorVersion task will upgrade
to the next minor version & creates a tagged non-SNAPSHOT release.

## Details
The following list contains the gradle task that are a part of the plugin with a brief description


Root Project Release tasks
--------------------------
bumpTheVersion - Will bump the version (i.e. add -SNAPSHOT) in the version file.

createReleaseTag - Create the version tag used by GitHub.

releasePush - Push the project to GitHub.

removeVersionSuffix - Define a release version (i.e. remove -SNAPSHOT) and commit it.

In the root directory of the repo you should have a versions.gradle file that looks like the following entry:
```groovy
allprojects {
    //required for the plugin
    group = 'com.ngc.seaside'

    //required for the plugin
    version = '2.0.0-SNAPSHOT'

    ext {

        //This is the first verison that contains the
        //new plugin
        seasidePluginsVersion = '2.2.3-SNAPSHOT'
    }
}
```
You will need the following sections in your build.gradle for each subproject or at least at a level where you will need
to use the plugin's tasks
```groovy
buildscript {
    //required
    ext {
        versionsFile = file('../versions.gradle')
    }
    //required
    apply from: versionsFile, to: project

}

//required
apply plugin: 'com.ngc.seaside.release.root'

subprojects {
    apply plugin: 'com.ngc.seaside.parent'
    //required
    versionSettings {
        versionFile = versionsFile
    }
}
```

Note that all _releaseXXX_ tasks run non-interactively and are thus well suited for continuous integration.
## This plugin requires properties in your gradle.properties file (usually ~/.gradle/gradle.properties):
* nexusUsername     : the username to use when uploading artifacts to nexus
* nexusPassword     : the password to use when uploading artifacts to nexus
* nexusReleases     : url to the releases repository
* nexusSnapshots    : url to the snapshots repository
* nexusConsolidated : url to the maven public download site usually a proxy to maven central and the
releases and snapshots
* systemProp.sonar.host.url : url to the Sonarqube server
