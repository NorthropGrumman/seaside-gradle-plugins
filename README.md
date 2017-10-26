# seaside-gradle-plugins [![Build Status](http://10.207.42.137/jenkins/job/seaside-gradle-plugins/job/seaside-gradle-plugins/job/master/badge/icon)](http://10.207.42.137/jenkins/job/seaside-gradle-plugins/job/seaside-gradle-plugins/job/master/)
`com.ngc.seaside.gradle.plugins` contains the core Seaside plugins used by many Gradle builds.  All core plugins are
contained in a single JAR and versioned together to make them easier to use.

## Prerequisites
* perl-Digest-MD5
    * Install with: `sudo yum install -y perl-Digest-MD5`
    * This is a dependency for `lcov`
* pygments
    * Install with: `sudo pip install pygments`
    * You may need to install `pip`, if it's not already available
        * Install with: `sudo yum install -y python2-pip`
    * This is a dependency for `cppcheck`
    
## Notes on building this project
* To build, you can run: `./gradlew build`
* If you want to see which Gradle tasks are available for you to run, execute: `./gradlew tasks [--all]`
* You can always skip a part of the build process by passing the `-x` option
    * For example, if you don't want to wait for all of the functional tests to pass: `./gradlew build -xfunctionalTest`
* NB: if you're using Windows, use `gradlew` instead of `./gradlew`

# com.ngc.seaside:seaside.parent
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
* systemProp.sonar.host.url : url to the Sonarqube server

## Using this plugin
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
        classpath 'com.ngc.seaside:gradle.plugins:1.0'
        classpath 'org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:2.5'
    }
}

subprojects {
    apply plugin: 'com.ngc.seaside.parent'

    group = 'com.ngc.seaside'
    version = '1.0-SNAPSHOT'

}
```

## Tasks
This plugin configures the following tasks:

| Task | Description | Executed by default |
|------|-------------|---------------------|
| analyze | Runs Jacoco to compute code coverage and then runs Sonarqube | no | 
| downloadDependencies | Downloads all dependencies into the build/dependencies/ folder using maven2 layout. | no |
| dependencyUpdates | Displays a report of the project dependencies that are up-to-date, exceed the latest version found, have upgrades, or failed to be resolved. | no |
| dependencyReport | Lists all dependencies. Use -DshowTransitive=<bool> to show/hide transitive dependencies | no |
| cleanupDependencies | Remove unused dependencies from dependencies folder | no |

# com.ngc.seaside:seaside.service-distribution
The seaside gradle service distribution plugin provide the directory structure required to run a BLoCS application and all of its bundle dependencies. This plugin will then distribute and compress the bundles of files packaged, including blocs dependencies, jar files, and also resource files. 

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
        classpath 'com.ngc.seaside:gradle.plugins:2.0.4'
    }
}

apply plugin: 'com.ngc.seaside.service-distribution'

group = 'com.ngc.seaside'
version = '1.0-SNAPSHOT'

seasideDistribution {
    buildDir = 'build'
    distributionName = "${group}.${project.name}-${version}"
    distributionDir = "build/distribution/${group}.${project.name}-${version}"
    distributionDestDir = 'build/distribution/'
}

dependencies {
    bundles 'com.ngc.seacide:service.transport.api:1.1'
    blocs "com.ngc.blocs:api:${blocsCoreVersion}"
    blocs "com.ngc.blocs:file.impl.common.fileutilities:${blocsCoreVersion}"

    thirdParty 'org.hibernate.javax.persistence:hibernate-jpa-2.1-api:1.0.0.Final'
    thirdParty 'com.google.guava:guava:17.0'
    
    platform "com.ngc.blocs:service.deployment.impl.common.autodeploymentservice:${blocsCoreVersion}"
    platform "org.eclipse.equinox:equinox-common:3.6.200.v20130402-1505"
}
```
# com.ngc.seaside:seaside.application
The seaside gradle application plugin will package the project as an application.

## This plugin requires properties in your gradle.properties file (usually ~/.gradle/gradle.properties):
* nexusConsolidated : url to the maven public download site usually a proxy to maven central and the
releases and snapshots

## Using this plugin
To use the plugin you will need to add the classpath to your buildscript dependencies and then just apply the plugin.
An example is below. Note: a newer version may exist. Check the Nexus repository for the latest version.

This plugin will generate a start script that will set the value of the variable `$APP_HOME` as a system property.
The name of the system property is defined by the `appHomeVarName` property.  The value of `$APP_HOME` is the directory
that contains the application (the directory that contains bin, libs, resources, etc).  The properties
`windows.appHomeCmd` and `unix.appHomeCmd` configure how this value is computed when the scripts are executed.

Below is an example of using the application plugin.  The value of the system property `myAppHome` will be the result of
`%~dp0..` when the Windows script is executed.

```java
buildscript {
    repositories {
        mavenLocal()

        maven {
            url nexusConsolidated
        }
    }

    dependencies {
        classpath 'com.ngc.seaside:gradle.plugins:1.4.1'
    }
}

apply plugin: 'com.ngc.seaside.application'

group = 'com.ngc.seaside'
version = '1.0-SNAPSHOT'

seasideApplication {
    mainClassName = "com.ngc.seaside.service.Main"
    includeDistributionDirs = ['src/main/resources/', 'src/main/output/']
    appHomeVarName = 'myAppHome'
    appSystemProperties = [test: "System Properties Test", NG_FW_HOME: "APP_HOME_VAR", anInt: 600]
    distributionName = "myAppDist"
    installationDir = "build/distributions/myAppInstall"

    windows {
        appHomeCmd = "%~dp0.."
        //startScript = 'src/main/output/bin/start.bat'
    }
    unix {
        startScript = 'src/main/output/bin/start'
        // setting appHomeCmd here does nothing because 
        // the custom startScript will overwrite the generated start script
        // appHomeCmd = "pwd -P"
    }
}

dependencies {
    bundles 'com.ngc.seacide:service.transport.api:1.1'
    blocs "com.ngc.blocs:api:${blocsCoreVersion}"
    blocs "com.ngc.blocs:file.impl.common.fileutilities:${blocsCoreVersion}"

    thirdParty 'org.hibernate.javax.persistence:hibernate-jpa-2.1-api:1.0.0.Final'
    thirdParty 'com.google.guava:guava:17.0'
    
    platform "com.ngc.blocs:service.deployment.impl.common.autodeploymentservice:${blocsCoreVersion}"
    platform "org.eclipse.equinox:equinox-common:3.6.200.v20130402-1505"
}
```

# com.ngc.seaside:seaside.release
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
        classpath 'com.ngc.seaside:gradle.plugins:1.6.1'
        classpath 'org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:2.5'
    }
}

subprojects {
   
    apply plugin: 'com.ngc.seaside.parent'
    apply plugin: 'com.ngc.seaside.release'

    group = 'com.ngc.seaside'
    version = '1.2.3-SNAPSHOT'
    
    seasideRelease {
            dependsOn subprojects*.build
            uploadArtifacts = true //'false' would be useful if you don't want to publish release to nexus
            push = true //'false' would be useful when triggering the release task on a local repository
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

# com.ngc.seaside:seaside.bats
The seaside gradle bats plugin provides a method of running the
[bats framework](https://github.com/bats-core/bats-core) as a gradle task

## Using this plugin
To use the plugin you will need to have a directory with bats scripts. The default
is assumed to be `src/test/bats` however you can use another directory by setting
the `batsTestsDir` property. By default, the output will be displayed on the console
and written to a file located in `build/test-results/bats-tests/` and named `results.out`.
You can configure this, as well, using the `resultsFile` property. It is important to note
that `results.out` will be overwritten if you run bats again, so if you need to save the
output, rename / move the file somewhere safe.

Below is an example of using the bats plugin.
```java
buildscript {
   repositories {
      mavenLocal()

      maven {
         url nexusConsolidated
      }
   }

   dependencies {
      classpath 'com.ngc.seaside:gradle.plugins:1.6.1'
   }
}

subprojects {
   apply plugin: 'com.ngc.seaside.parent'
   apply plugin: 'com.ngc.seaside.bats'   // <- this is the minimum required

   group = 'com.ngc.seaside'
   version = '1.2.3-SNAPSHOT'

   // optionally, include something like the following
   seasideBats {
      // the results from running bats will be written here
      resultsFile = 'build/your-custom-results-file.txt'

      // we will look for bats tests (recursively) inside this directory
      // this means you can structure the tests however you want and all
      // of them will still be run
      batsTestsDir = 'src/test/my-bats-tests'
   }

   ext {
      junitVersion = '4.12'
      slf4jVersion = '1.7.22'
      log4jVersion = '1.2.17'
      cucumberVersion = '1.2.5'
   }
}
```

You can also override these properties when you run the gradle command. For example:
`gradlew runBats -PresultsFile='build/myresults.out' -PbatsTestsDir='my/bats/tests'`

# com.ngc.seaside:seaside.cpp.coverage
The seaside gradle C++ coverage plugin provides a method of running the lcov command
as a gradle task

## This plugin requires you to install the following dependencies
* perl-Digest-MD5
    * Install with: `sudo yum install -y perl-Digest-MD5`
    * This is a dependency for `lcov`
* pygments
    * Install with: `sudo pip install pygments`
    * You may need to install `pip`, if it's not already available
        * Install with: `sudo yum install -y python2-pip`
    * This is a dependency for `cppcheck`


## Using this plugin
To use this plugin, you must add compiler and linker flags to your `build.gradle` in any
in any project for which you want to generate coverage data. See
[this](http://10.207.42.137/confluence/pages/viewpage.action?pageId=13664611) page to determine
which flags are required.

*NB: This plugin must be used from a unix machine. There is no guarantee (or implication) that it
will work on Windows.*

Below is an example of using the C++ coverage plugin.
```java
buildscript {
   repositories {
      mavenLocal()

      maven {
         url nexusConsolidated
      }
   }

   dependencies {
      classpath 'com.ngc.seaside:gradle.plugins:1.6.1'
   }
}

subprojects {
   apply plugin: 'com.ngc.seaside.cpp.parent'
   apply plugin: 'com.ngc.seaside.cpp.coverage'   // <- this is the minimum required

   group = 'com.ngc.blocs.cpp'
   version = '1.0-SNAPSHOT'

   // optionally, include something like the following
   seasideCppCov {
      // the results from generating coverage data will be written here
      coverageFilePath = 'build/your/custom/coverage/output/file.txt'

      // the generated cobertura xml file will be written here
      coverageXMLPath = 'build/your/custom/xml/output/file.xml'
   }

   ext {
      gTestVersion = '1.8'
      gMockVersion = "$gTestVersion"
      boostVersion = '1_64'
      celixVersion = '2.0.0'
   }
}
```

You can also override these properties when you run the gradle command. For example:
`gradlew genFullCoverageReport -PcoverageFilePath='build/my/coverage/data.info' -PcoverageXMLPath='build/my/cobertura/output.xml'`

# Reference
[seaside-gradle-plugins wiki](http://10.207.42.137/confluence/display/SEAS/seaside-gradle-plugins+-+Core+Gradle+plugins+for+Seaside+development)
