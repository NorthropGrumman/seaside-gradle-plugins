# seaside-gradle-plugins
`com.ngc.seaside.gradle.plugins` contains the core Seaside plugins used by many Gradle builds.  All core plugins are
contained in a single JAR and versioned together to make them easier to use.

All plugins can be obtained by putting the following dependency in the buildscript of your build.gradle:

```java
buildscript {
    repositories {
        ...
    }

    dependencies {
        classpath "com.ngc.seaside:gradle.plugins:$seasidePluginsVersion"
    }
}
```

## Notes on building this project
* To build, you can run: `./gradlew build`
* If you want to see which Gradle tasks are available for you to run, execute: `./gradlew tasks [--all]`
* You can always skip a part of the build process by passing the `-x` option
    * For example, if you don't want to wait for all of the functional tests to pass: `./gradlew build -xfunctionalTest`
* NB: if you're using Windows, use `gradlew.bat` instead of `./gradlew`

# Reference
[seaside-gradle-plugins wiki](http://10.207.42.137/confluence/display/SEAS/seaside-gradle-plugins+-+Core+Gradle+plugins+for+Seaside+development)
