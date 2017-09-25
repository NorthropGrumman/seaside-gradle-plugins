# seaside-gradle-plugins
`com.ngc.seaside.gradle.plugins` contains the core Seaside plugins used by many Gradle builds.  All core plugins are
contained in a single JAR and versioned together to make them easier to use.

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

# com.ngc.seaside:seaside.distribution
The seaside gradle distribution plugin provide the directory structure required to run a BLoCS application and all of its bundle dependencies. This plugin will then distribute and compress the bundles of files packaged, including blocs dependencies, jar files, and also resource files. 

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
        classpath 'com.ngc.seaside:gradle.plugins:1.4.1'
    }
}

apply plugin: 'com.ngc.seaside.distribution'

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
The seaside gradle appplication plugin will package the project as an application.  

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

##Details


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

You can also override these properties when you run the gradle command. For example: `gradlew release -PuploadArtifacts=false -Ppush=false -PtagPrefix="test" -PversionSuffix="-TEST"`
# Reference
[seaside-gradle-plugins wiki](http://10.207.42.43/confluence/display/SEAS/seaside-gradle-plugins+-+Core+Gradle+plugins+for+Seaside+development)
