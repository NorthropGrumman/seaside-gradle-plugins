# com.ngc.seaside.distribution.felixservice
The seaside felix service distribution plugin provides the directory structure required to run an OSGi BLoCS application
and all of its bundle dependencies with the Apache Felix OSGi. This plugin will create a distribution zip containing
all the required dependencies, jars, scripts and resources needed to run the distribution. 

## Using this plugin
To use the plugin you will need to add the classpath to your buildscript dependencies and then just apply the plugin.
You can add system properties and program arguments using the ``felixService`` extension. Dependencies can be added
using the ``bundles`` configuration.

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
apply plugin: 'com.ngc.seaside.distribution.felixservice'

group = 'com.ngc.seaside'
version = '1.0-SNAPSHOT'

felixService {
    systemProperties = ['foo': 'bar', 'hello': 'world']
    programArgs = ['-foo', '-bar', '-hello', 'world']
}

dependencies {
    bundles "com.ngc.seaside:service.api:$starfishVersion"
    bundles "com.ngc.seaside:service.correlation.impl.correlationservice:$starfishVersion"
    bundles "com.ngc.seaside:service.fault.impl.faultloggingservice:$starfishVersion"
    bundles "com.ngc.seaside:service.monitoring.impl.loggingmonitoringservice:$starfishVersion"
    bundles "com.ngc.seaside:service.request.impl.microservicerequestservice:$starfishVersion"
    bundles "com.ngc.seaside:service.transport.api:$starfishVersion"
    bundles "com.ngc.seaside:service.transport.impl.defaulttransportservice:$starfishVersion"
    bundles "com.ngc.seaside:service.transport.impl.topic.multicast:$starfishVersion"
    bundles "com.ngc.seaside:service.transport.impl.provider.multicast:$starfishVersion"
    bundles "com.ngc.seaside:service.transport.impl.topic.zeromq:$starfishVersion"
    bundles "com.ngc.seaside:service.transport.impl.provider.zeromq:$starfishVersion"
}
```

# com.ngc.seaside.distribution.system
The seaside system distribution plugin provides the directory structure for running a distribution composed of multiple
sub-distributions. This plugin will create a distribution zip containing start scripts and the specified
sub-distributions.

## Using this plugin
To use the plugin you will need to add the classpath to your buildscript dependencies and then just apply the plugin.
You can add dependencies using the ``distribution`` configuration. Dependencies added to this configuration
must reference zip-formatted files. The default start scripts for this plugin's distribution assume that these
sub-distributions have start scripts in their ``bin/`` folder. The default start scripts can be overridden using
the extension ``systemDistribution``.

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
apply plugin: 'com.ngc.seaside.distribution.system'

group = 'com.ngc.seaside'
version = '1.0-SNAPSHOT'

ext {
    threatEvalVersion = '2.4.0'
}

dependencies {
    distribution "com.ngc.seaside.threateval:etps.distribution:$threatEvalVersion@zip"
    distribution "com.ngc.seaside.threateval:ctps.distribution:$threatEvalVersion@zip"
    distribution "com.ngc.seaside.threateval:datps.distribution:$threatEvalVersion@zip"
    distribution "com.ngc.seaside.threateval:tps.distribution:$threatEvalVersion@zip"
}
```

# com.ngc.seaside.service-distribution
The seaside gradle service distribution plugin provide the directory structure required to run a BLoCS application and
all of its bundle dependencies. This plugin will then distribute and compress the bundles of files packaged, including
blocs dependencies, jar files, and also resource files.

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
        classpath "com.ngc.seaside:gradle.plugins:$seasidePluginsVersion"
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
    bundles "com.ngc.seacide:service.transport.api:$starfishVersion"
    blocs "com.ngc.blocs:api:$blocsCoreVersion"
    blocs "com.ngc.blocs:file.impl.common.fileutilities:$blocsCoreVersion"

    thirdParty 'org.hibernate.javax.persistence:hibernate-jpa-2.1-api:1.0.0.Final'
    thirdParty 'com.google.guava:guava:17.0'

    platform "com.ngc.blocs:service.deployment.impl.common.autodeploymentservice:${blocsCoreVersion}"
    platform "org.eclipse.equinox:equinox-common:3.6.200.v20130402-1505"
}
```