# com.ngc.seaside.cpp.coverage
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
      classpath "com.ngc.seaside:gradle.plugins:$seasidePluginsVersion"
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
