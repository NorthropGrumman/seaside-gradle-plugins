package com.ngc.seaside.gradle.tasks.cpp.dependencies;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.internal.resolve.ProjectModelResolver;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.TaskAction;
import org.gradle.language.cpp.CppSourceSet;
import org.gradle.model.internal.registry.ModelRegistry;
import org.gradle.nativeplatform.NativeLibrarySpec;
import org.gradle.nativeplatform.PrebuiltLibrary;
import org.gradle.nativeplatform.Repositories;
import org.gradle.nativeplatform.internal.prebuilt.AbstractPrebuiltLibraryBinary;
import org.gradle.nativeplatform.internal.prebuilt.DefaultPrebuiltLibraries;
import org.gradle.nativeplatform.internal.prebuilt.DefaultPrebuiltSharedLibraryBinary;
import org.gradle.nativeplatform.internal.prebuilt.DefaultPrebuiltStaticLibraryBinary;
import org.gradle.nativeplatform.test.googletest.GoogleTestTestSuiteBinarySpec;
import org.gradle.platform.base.BinaryContainer;
import org.gradle.platform.base.ComponentSpecContainer;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class UnpackCppDistributionsTask extends DefaultTask {

   private File dependenciesDirectory;
   private String componentName;
   private String componentSourceSetName;
   private boolean testDependencies = false;

   @TaskAction
   public void unpackDistributions() {
      if (dependenciesDirectory == null) {
         throw new IllegalArgumentException("dependenciesDirectory property not set!");
      }
      Project project = getProject();

      if (dependenciesDirectory.isDirectory()) {
         if (!testDependencies && (componentName == null || componentName.trim().equals(""))) {
            throw new IllegalArgumentException("componentName must be set!");
         }
         if (!testDependencies && (componentSourceSetName == null || componentSourceSetName.trim().equals(""))) {
            throw new IllegalArgumentException("componentSourceSetName must be set!");
         }

         File[] zips = dependenciesDirectory.listFiles((dir, name) -> name.endsWith(".zip"));
         for (File zip : zips) {
            // Remove the .zip
            String destFileName = zip.getName().substring(0, zip.getName().length() - 4);
            File dest = new File(zip.getParentFile(), destFileName);

            Copy unzipTask = project.getTasks().create("unzip" + destFileName, Copy.class);
            unzipTask.into(dest);
            unzipTask.from(project.zipTree(zip));
            unzipTask.execute();

            configureDependency(dest, getDependencyNameFromFileName(destFileName));
         }
      }
   }

   public File getDependenciesDirectory() {
      return dependenciesDirectory;
   }

   public void setDependenciesDirectory(File dependenciesDirectory) {
      this.dependenciesDirectory = dependenciesDirectory;
   }

   public String getComponentName() {
      return componentName;
   }

   public void setComponentName(String componentName) {
      this.componentName = componentName;
   }

   public String getComponentSourceSetName() {
      return componentSourceSetName;
   }

   public void setComponentSourceSetName(String componentSourceSetName) {
      this.componentSourceSetName = componentSourceSetName;
   }

   public boolean isTestDependencies() {
      return testDependencies;
   }

   public void setTestDependencies(boolean testDependencies) {
      this.testDependencies = testDependencies;
   }


   private void configureDependency(File directory, String dependencyName) {
      addDependencyAsRepository(directory, dependencyName);
      if (!testDependencies) {
         addDependencyToComponent(dependencyName);
      }
      addDependencyToGoogleTestBinary(dependencyName);
   }

   private void addHeaders(File directory, String dependencyName, BuildingExtension buildingExtension, PrebuiltLibrary lib) {
      Collection<HeaderBuildConfiguration>
            headerConfigurations = buildingExtension.getStorage().getHeaderBuildConfigurations(dependencyName);
      if (headerConfigurations != null) {
         for (HeaderBuildConfiguration buildConfiguration : headerConfigurations) {
            for (String path : buildConfiguration.getDirs()) {
               File includeDirectory = new File(directory, path);
               System.out.println("Adding include " + path);
               lib.getHeaders().setSrcDirs(Collections.singletonList(includeDirectory));
            }
         }
      } else {
         File includeDirectory = new File(directory, "include");
         lib.getHeaders().setSrcDirs(Collections.singletonList(includeDirectory));
      }
   }

   private void addCustomStaticDependencies(File directory, String dependencyName,
                                            BuildingExtension buildingExtension, DefaultPrebuiltLibraries libs) {
      Collection<StaticBuildConfiguration> configs = buildingExtension.getStorage().getStaticBuildConfigurations(dependencyName);
      for (StaticBuildConfiguration config : configs) {
         for (String library : config.getLibs()) {
            PrebuiltLibrary lib = libs.create(library);

            addHeaders(directory, dependencyName, buildingExtension, lib);

            libs.resolveLibrary(lib.getName());

            for (DefaultPrebuiltStaticLibraryBinary bin :
                  lib.getBinaries().withType(DefaultPrebuiltStaticLibraryBinary.class)) {
               File obj = getFile(bin, directory, library);
               if(obj.exists()) {
                  bin.setStaticLibraryFile(obj);
               }
            }
         }
      }
   }

   private void addCustomSharedDependencies(File directory, String dependencyName,
                                            BuildingExtension buildingExtension) {

   }

   private File getFile(AbstractPrebuiltLibraryBinary bin, File directory,  String libName) {
      String arch = bin.getTargetPlatform().getArchitecture().getName().replace('-', '_');
      String os = bin.getTargetPlatform().getOperatingSystem().getName();
      String ext = bin.getTargetPlatform().getOperatingSystem().isWindows() ? "lib" : "a";
      String prefix = bin.getTargetPlatform().getOperatingSystem().isWindows() ? "" : "lib";
      String file = String.format("lib/%s_%s/%s%s.%s",
                                  os,
                                  arch,
                                  prefix,
                                  libName,
                                  ext);
      return new File(directory, file);
   }


   private void addStandardStaticDependency(
         File directory, String dependencyName, BuildingExtension buildingExtension, DefaultPrebuiltLibraries libs) {
      PrebuiltLibrary lib = libs.create(dependencyName);

      Collection<HeaderBuildConfiguration>
            headerConfigurations = buildingExtension.getStorage().getHeaderBuildConfigurations(dependencyName);
      if (headerConfigurations != null) {
         for (HeaderBuildConfiguration buildConfiguration : headerConfigurations) {
            for (String path : buildConfiguration.getDirs()) {
               File includeDirectory = new File(directory, path);
               lib.getHeaders().setSrcDirs(Collections.singletonList(includeDirectory));
            }
         }
      } else {
         File includeDirectory = new File(directory, "include");
         lib.getHeaders().setSrcDirs(Collections.singletonList(includeDirectory));
      }

      libs.resolveLibrary(lib.getName());

      for (DefaultPrebuiltStaticLibraryBinary bin : lib.getBinaries().withType(DefaultPrebuiltStaticLibraryBinary.class)) {
         File obj = getFile(bin, directory, dependencyName);
         if(obj.exists()) {
            bin.setStaticLibraryFile(obj);
         }
      }
   }

   private void addDependencyAsRepository(File directory, String dependencyName) {
      BuildingExtension buildingExtension = getProject().getExtensions().getByType(BuildingExtension.class);

      ModelRegistry projectModel = getServices()
            .get(ProjectModelResolver.class)
            .resolveProjectModel(getProject().getPath());

      Repositories repositories = projectModel.find("repositories", Repositories.class);
      DefaultPrebuiltLibraries libs = repositories.withType(DefaultPrebuiltLibraries.class).getByName("libs");

      System.out.println("addDependencyAsRepository " + dependencyName + "(" + directory.getAbsolutePath() +" )");

      if (buildingExtension.getStorage().hasCustomStaticBuildConfiguration(dependencyName)) {
         System.out.println("Adding custom static dependency " + dependencyName);
         addCustomStaticDependencies(directory, dependencyName, buildingExtension, libs);
      } else {
         System.out.println("Adding standard static dependency " + dependencyName);
         addStandardStaticDependency(directory, dependencyName, buildingExtension, libs);
      }

//      PrebuiltLibrary lib = libs.create(dependencyName);
//
//      Collection<HeaderBuildConfiguration>
//            headerConfigurations = buildingExtension.getStorage().getHeaderBuildConfigurations(dependencyName);
//      if(headerConfigurations != null) {
//         for(HeaderBuildConfiguration buildConfiguration : headerConfigurations) {
//            for(String path : buildConfiguration.getDirs()) {
//               File includeDirectory = new File(directory, path);
//               lib.getHeaders().setSrcDirs(Collections.singletonList(includeDirectory));
//            }
//         }
//      } else {
//         File includeDirectory = new File(directory, "include");
//         lib.getHeaders().setSrcDirs(Collections.singletonList(includeDirectory));
//      }
//
//      libs.resolveLibrary(lib.getName());
//
//      for (DefaultPrebuiltStaticLibraryBinary bin : lib.getBinaries().withType(DefaultPrebuiltStaticLibraryBinary.class)) {
//         String arch = bin.getTargetPlatform().getArchitecture().getName().replace('-', '_');
//         String os = bin.getTargetPlatform().getOperatingSystem().getName();
//         String ext = bin.getTargetPlatform().getOperatingSystem().isWindows() ? "lib" : "a";
//         String prefix = bin.getTargetPlatform().getOperatingSystem().isWindows() ? "" : "lib";
//         String file = String.format("lib/%s_%s/%s%s.%s",
//                                     os,
//                                     arch,
//                                     prefix,
//                                     dependencyName,
//                                     ext);
//         File obj = new File(directory, file);
//         bin.setStaticLibraryFile(obj);
//      }
//      for (DefaultPrebuiltSharedLibraryBinary bin : lib.getBinaries()
//               .withType(DefaultPrebuiltSharedLibraryBinary.class)) {
//         String arch = bin.getTargetPlatform().getArchitecture().getName().replace('-', '_');
//         String os = bin.getTargetPlatform().getOperatingSystem().getName();
//         String ext = bin.getTargetPlatform().getOperatingSystem().isWindows() ? "dll" : "so";
//         String prefix = bin.getTargetPlatform().getOperatingSystem().isWindows() ? "" : "lib";
//         String file = String.format("lib/%s_%s/%s%s.%s",
//                                     os,
//                                     arch,
//                                     prefix,
//                                     dependencyName,
//                                     ext);
//         bin.setSharedLibraryFile(new File(directory, file));
//      }
   }

   private void addDependencyToComponent(String dependencyName) {
      ModelRegistry projectModel = getServices()
            .get(ProjectModelResolver.class)
            .resolveProjectModel(getProject().getPath());
      NativeLibrarySpec component = projectModel.find("components", ComponentSpecContainer.class)
            .withType(NativeLibrarySpec.class)
            .get(getComponentName());
      CppSourceSet cppSourceSet = component.getSources()
            .withType(CppSourceSet.class)
            .get(getComponentSourceSetName());

      Map<String, String> map = new HashMap<>();
      map.put("library", dependencyName);

      map.put("linkage", getLinkingType(dependencyName)); // api = header only, other options are shared or static.
      cppSourceSet.lib(map);
   }

   private String getLinkingType(String dependencyName) {
//      LinkingConfiguration linkingConfig = getProject().getExtensions().getByType(LinkingConfiguration.class);
//      String type = linkingConfig.getLinkingType(dependencyName);
//      return type == null ? "static" : type;
      return "static";
   }

   private void addDependencyToGoogleTestBinary(String dependencyName) {
      ModelRegistry projectModel = getServices()
            .get(ProjectModelResolver.class)
            .resolveProjectModel(getProject().getPath());
      for (GoogleTestTestSuiteBinarySpec binary :
            projectModel.find("binaries", BinaryContainer.class)
                  .withType(GoogleTestTestSuiteBinarySpec.class)) {

         Map<String, String> map = new HashMap<>();
         map.put("library", dependencyName);
         map.put("linkage", getLinkingType(dependencyName));
         binary.lib(map);
      }
   }

   private static String getDependencyNameFromFileName(String fileName) {
      // Return the name of the artifact/dependency minus the version.
      String name = fileName;
      int position = fileName.indexOf('-');
      if (position > 0) {
         name = fileName.substring(0, position);
      }
      return name;
   }
}
