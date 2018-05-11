# com.ngc.seaside.checkstyle
The seaside checkstyle plugin is used for adding checkstyle analysis to your project.

## Using this plugin
This plugin simply needs to be applied to your project.

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

apply plugin: 'com.ngc.seaside.checkstyle'
```

Running the following command will perform the checkstyle analysis on your project, failing the build if there are any
issues:

```
./gradlew checkstyleMain checkstyleTest -Pfail-on-checkstyle-error=true
```