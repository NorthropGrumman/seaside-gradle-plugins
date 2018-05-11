# com.ngc.seaside.bats
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
      classpath "com.ngc.seaside:gradle.plugins:$seasidePluginsVersion"
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
