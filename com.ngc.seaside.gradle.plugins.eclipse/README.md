# com.ngc.seaside.eclipse.feature
The seaside Eclipse Feature plugin packages an Eclipse feature.xml file for creating an Eclipse update site.

## Using this plugin
Below is an example of using the Eclipse Feature plugin.
```java
apply plugin: 'com.ngc.seaside.eclipse.features'

eclipseFeature {
   // (optional) set the name of the archive that is created containing the feature.
   archiveName = 'some.archive-name-1.2.3.jar'
}
```

# com.ngc.seaside.eclipse.updatesite
The seaside Eclipse Update Site plugin packages a custom Eclipse update site.

## Using this plugin
Below is an example of using the Eclipse Update Site plugin.
```java
apply plugin: 'com.ngc.seaside.eclipse.updatesite'

dependencies {
    features project(path: ':some.service', configuration: 'feature')

    customPlugins project(':some.other.service')

    eclipsePlugins name: 'some.eclipse.plugin_1.2.3.v201801010000'
}

eclipseUpdateSite {
   // (optional) set the name of the archive that is created containing the update site.
   updateSiteArchiveName = 'some.archive-name-1.2.3.jar'

   // (optional) set the cache directory into which the Eclipse SDK should be downloaded and extracted.
   cacheDirectory = '/tmp'

   // (required) the download url to the linux eclipse distribution.
   linuxEclipseVersion = "eclipse-dsl-oxygen-2-linux-gtk-x86_64"

   // (required) the name of the eclipse distribution version to download on linux.
   linuxDownloadUrl = "https://nexusrepomgr.ms.northgrum.com/repository/raw-ng-repo/ceacide/${linuxEclipseVersion}.zip"

   // (required) the download url to the windows eclipse distribution.
   windowsEclipseVersion = "eclipse-dsl-oxygen-2-win32-x86_64"

   // (required) the name of the eclipse distribution version to download on windows.
   windowsDownloadUrl = "https://nexusrepomgr.ms.northgrum.com/repository/raw-ng-repo/ceacide/${windowsEclipseVersion}.zip"
}
```
