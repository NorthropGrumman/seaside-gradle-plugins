# com.ngc.seaside.eclipse.base
The seaside base Eclipse plugin downloads and installs an eclipse distribution to be used by other plugins.
This plugin requires you to set versions and download urls for the eclipse distribution.

Usually, you will not need to explicitly apply this plugin, since other eclipse plugins already it.

## Using this plugin
Below is an example of using the eclipse base plugin. 
```groovy
apply plugin: 'com.ngc.seaside.eclipse.base'
eclipseDistribution {
   linuxVersion = 'eclipse-dsl-photon-R-linux-gtk-x86_64'
   windowsVersion = 'eclipse-dsl-photon-R-win32-x86_64'
   linuxDownloadUrl = ...
   windowsDownloadUrl = ...
   enablePluginsRepository() // creates a directory repository pointing to the downloaded eclipse distribution plugins
}
```

# com.ngc.seaside.eclipse.updatesite
The seaside Eclipse Update Site plugin packages a custom Eclipse update site.

This plugin allows you to specify plugin dependencies, features, and categories for your update site.
The plugin applies the base eclipse plugin, so you will need to set versions and download urls for the
eclipse distribution.

## Using this plugin
Below is an example of using the Eclipse Update Site plugin.
```groovy
apply plugin: 'com.ngc.seaside.eclipse.updatesite'
eclipseDistribution { ... }
eclipseUpdateSite {
   def feature1 = feature {
      id = 'com.ngc.seaside.systemdescriptor.feature'
      label = 'JellyFish SystemDescriptor DSL'
      version = project.version
      providerName = 'Northrop Grumman Corporation'
      description {
         url = 'http://www.systemdescriptor.seaside.ngc.com/description'
         text = 'This is the JellyFish System Descriptor Domain Specific Language Eclipse plugin.'
      }
      copyright {
         url = 'http://www.systemdescriptor.seaside.ngc.com/copyright'
         text = project.resources.text.fromFile(project.file('src/main/resources/license.txt')).asString()
      }
      license {
         url = 'http://www.systemdescriptor.seaside.ngc.com/license'
         text = copyright.text
      }
      plugin {
         id = 'com.ngc.seaside.systemdescriptor'
         version = '0.0.0'
         unpack = false
      }
   }
   def feature2 = feature { ... }
   category {
      name = 'system_descriptor_category_id'
      label = 'System Descriptor Plugin'
      description = 'Eclipse Plugin for the Seaside System Descriptor'
      features feature1, feature2
   }
}
```

# com.ngc.seaside.eclipse.pw
The seaside Eclipse p2 plugin manages remote p2 repositories.

This plugin allows you to access remote p2 repositories and leverage feature and plugins in external update sites.
The plugin applies the base eclipse plugin, so you will need to set versions and download urls for the
eclipse distribution.

## Using this plugin
Below is an example of using the Eclipse p2 plugin. This example uses the update site plugin and adds the plugins
and features from a remote p2 update site.
```groovy
apply plugin: 'com.ngc.seaside.eclipse.updatesite'
apply plugin: 'com.ngc.seaside.eclipse.p2'
eclipseDistribution { ... }
p2.remoteRepository('http://example.com/updatesite') {
   // Include all the external site plugins
   plugins { externalPlugin ->;
      dependencies {
         plugin externalPlugin.dependency
      }
   }
   // Include all the external site features
   features { externalFeature ->;
      eclipseUpdateSite.feature externalFeature
   }
}
```