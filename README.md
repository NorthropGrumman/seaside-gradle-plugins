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
| downloadDependencies | Downloads all dependencies into a local directory specified by -PcustomRepo=\<name> using maven2 layout | no | 
| exportGradleCache | Copies all gradle downloaded dependencies to a custom local repository specified by -PcustomRepo=\<name> using maven2 layout | no | 

# com.ngc.seaside:seaside.distribution
The seaside gradle distribution plugin provide the directory structure required to run a BLoCS application and all of its bundle dependencies.This plugin will then distribute and compress the bundles of files packaged, including blocs dependencies, jar files, and also resource files. 

## This plugin requires properties in your gradle.properties file (usually ~/.gradle/gradle.properties):
* nexusConsolidated : url to the maven public download site usually a proxy to maven central and the
releases and snapshots

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
        classpath 'com.ngc.seaside:gradle.plugins:1.1-SNAPSHOT'
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
# Reference
[seaside-gradle-plugins wiki](http://10.207.42.42:8080/display/SEAS/seaside-gradle-plugins+-+Core+Gradle+plugins+for+Seaside+development)
