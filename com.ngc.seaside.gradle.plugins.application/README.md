# com.ngc.seaside.application
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
        classpath "com.ngc.seaside:gradle.plugins:$seasidePluginsVersion"
    }
}

apply plugin: 'com.ngc.seaside.application'

group = 'com.ngc.seaside'
version = '1.0-SNAPSHOT'

seasideApplication {
    mainClassName = 'com.ngc.seaside.service.Main'
    includeDistributionDirs = ['src/main/resources/', 'src/main/output/']
    appHomeVarName = 'myAppHome'
    appSystemProperties = [test: "System Properties Test", NG_FW_HOME: "APP_HOME_VAR", anInt: 600]
    distributionName = "myAppDist"
    installationDir = "build/distributions/myAppInstall"

    windows {
        appHomeCmd = '%~dp0..'
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
    bundles "com.ngc.seacide:service.transport.api:$starfishVersion"
    blocs "com.ngc.blocs:api:$blocsCoreVersion"
    blocs "com.ngc.blocs:file.impl.common.fileutilities:$blocsCoreVersion"

    thirdParty 'org.hibernate.javax.persistence:hibernate-jpa-2.1-api:1.0.0.Final'
    thirdParty 'com.google.guava:guava:17.0'

    platform "com.ngc.blocs:service.deployment.impl.common.autodeploymentservice:$blocsCoreVersion"
    platform "org.eclipse.equinox:equinox-common:3.6.200.v20130402-1505"
}
```
