# com.ngc.seaside.parent
The seaside gradle parent plugin provides a base gradle build for all seaside java projects. This plugin will ensure
your bundles are named correctly, provide an OSGi enabled jar file (bundle), generate javadocs jar, generate sources jar
and provide the default configuration for deploying these artifacts to Nexus.

## This plugin applies the 'com.ngc.seaside.repository' plugin, which by default requires the following properties in
your gradle.properties file (usually ~/.gradle/gradle.properties):
* nexusConsolidated : url to the maven public download site usually a proxy to maven central and the
* nexusReleases     : url to the releases repository (required when publishing a release to Nexus)
* nexusSnapshots    : url to the snapshots repository (required when publishing a snapshot release to Nexus)
* nexusUsername     : the username to use when uploading artifacts to nexus
* nexusPassword     : the password to use when uploading artifacts to nexus
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
        classpath "org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:$sonarqubePluginVersion"
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
