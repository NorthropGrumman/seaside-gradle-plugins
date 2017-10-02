package com.ngc.seaside.gradle.tasks.cpp.dependencies;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.internal.resolve.ProjectModelResolver;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.TaskAction;
import org.gradle.language.cpp.CppSourceSet;
import org.gradle.model.internal.registry.ModelRegistry;
import org.gradle.nativeplatform.NativeLibraryBinary;
import org.gradle.nativeplatform.NativeLibrarySpec;
import org.gradle.nativeplatform.PrebuiltLibrary;
import org.gradle.nativeplatform.Repositories;
import org.gradle.nativeplatform.internal.prebuilt.AbstractPrebuiltLibraryBinary;
import org.gradle.nativeplatform.internal.prebuilt.DefaultPrebuiltLibraries;
import org.gradle.nativeplatform.internal.prebuilt.DefaultPrebuiltSharedLibraryBinary;
import org.gradle.nativeplatform.internal.prebuilt.DefaultPrebuiltStaticLibraryBinary;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.nativeplatform.test.googletest.GoogleTestTestSuiteBinarySpec;
import org.gradle.nativeplatform.toolchain.Gcc;
import org.gradle.nativeplatform.toolchain.GccPlatformToolChain;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry;
import org.gradle.platform.base.BinaryContainer;
import org.gradle.platform.base.ComponentSpecContainer;
import org.gradle.platform.base.ToolChain;
import org.gradle.platform.base.ToolChainRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  This task unpacks the dependencies and configures the cpp task.
 */
public class UnpackCppDistributionsTask extends DefaultTask {

   private File dependenciesDirectory;
   private String componentName;
   private String componentSourceSetName;
   private boolean testDependencies = false;

   /**
    * Shared, static or api library types are required for the components configuration
    */
   private enum LibraryType {
      SHARED("shared"),
      STATIC("static"),
      API("api");

      private String name;

      LibraryType(String name) {
         this.name = name;
      }

      public String getName() {
         return name;
      }
   }

   /**
    * Unpack all the necessary dependencies and update the cpp plugin's configuration.
    */
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


   /**
    * Configure the dependency within the cpp plugin. This includes creating the libs under model > repositories
    * and adding the dependency to the components
    *
    * @param directory       the base directory in which the dependency exists in its unpacked state.
    * @param dependencyName  the name of the dependency. This is usually the artifact ID.
    */
   private void configureDependency(File directory, String dependencyName) {
      BuildingExtension buildingExtension = getProject().getExtensions().getByType(BuildingExtension.class);

      ModelRegistry projectModel = getServices()
               .get(ProjectModelResolver.class)
               .resolveProjectModel(getProject().getPath());

      Repositories repositories = projectModel.find("repositories", Repositories.class);
      DefaultPrebuiltLibraries libs = repositories.withType(DefaultPrebuiltLibraries.class).getByName("libs");

      List<File> dependencyHeaders = createHeaderFiles(directory, dependencyName, buildingExtension);

      addStaticDependencies(directory, dependencyName, buildingExtension, libs, dependencyHeaders);
      addSharedDependencies(directory, dependencyName, buildingExtension, libs, dependencyHeaders);
      addApiDependencies(directory, dependencyName, buildingExtension, libs, dependencyHeaders);
   }

   /**
    * Add the dependency as an API dependency if configured that way.
    *
    * @param directory          the base directory in which the dependency exists in its unpacked state.
    * @param dependencyName     the name of the dependency (this is derived from the directory name (minus the version)
    * @param buildingExtension  the plugins extension (i.e. the 'building' configuration in groovy)
    * @param libs               the PrebuiltLibraries libs (the object that the dependency gets added to)
    * @param dependencyHeaders  the headers associated with this dependency
    */
   private void addApiDependencies(File directory,
                                   String dependencyName,
                                   BuildingExtension buildingExtension,
                                   DefaultPrebuiltLibraries libs,
                                   List<File> dependencyHeaders) {
      List<String> configs = buildingExtension.getStorage().getApiDependencies();
      if(configs.contains(dependencyName)) {
         PrebuiltLibrary lib = libs.create(dependencyName);
         lib.getHeaders().setSrcDirs(dependencyHeaders);
         if(!testDependencies) {
            addDependencyToComponent(dependencyName, LibraryType.API);
         }
         addDependencyToGoogleTestBinary(dependencyName, LibraryType.API);
      }
   }

   /**
    * Add the dependency as a static library if it is configured as such.
    *
    * @param directory          the base directory in which the dependency exists in its unpacked state.
    * @param dependencyName     the name of the dependency (this is derived from the directory name (minus the version)
    * @param buildingExtension  the plugins extension (i.e. the 'building' configuration in groovy)
    * @param libs               the PrebuiltLibraries libs (the object that the dependency gets added to)
    * @param dependencyHeaders  the headers associated with this dependency
    */
   private void addStaticDependencies(File directory,
                                      String dependencyName,
                                      BuildingExtension buildingExtension,
                                      DefaultPrebuiltLibraries libs,
                                      List<File> dependencyHeaders) {
      Collection<StaticBuildConfiguration> configs = buildingExtension.getStorage().getStaticBuildConfigurations(dependencyName);
      for (StaticBuildConfiguration config : configs) {
         if(config.getLibs() != null && !config.getLibs().isEmpty()) {
            for (String library : config.getLibs()) {
               PrebuiltLibrary lib = libs.create(library);

               lib.getHeaders().setSrcDirs(dependencyHeaders);

               libs.resolveLibrary(lib.getName());

               for (DefaultPrebuiltStaticLibraryBinary bin :
                        lib.getBinaries().withType(DefaultPrebuiltStaticLibraryBinary.class)) {
                  File obj = getLibFile(bin, directory, library, LibraryType.STATIC);
                  if (obj.exists()) {
                     bin.setStaticLibraryFile(obj);

                     if(config.getWithArgs() != null) {
                        addLinkerArgs(obj.getAbsolutePath(), config.getWithArgs(), buildingExtension);
                     }

                     if(!testDependencies) {
                        addDependencyToComponent(library, LibraryType.STATIC);
                     }
                     addDependencyToGoogleTestBinary(library, LibraryType.STATIC);
                  }
               }
            }
         } else {
            /**
             * Standard configuration uses the dependency name as the lib name.
             */
            PrebuiltLibrary lib = libs.create(dependencyName);
            lib.getHeaders().setSrcDirs(dependencyHeaders);
            libs.resolveLibrary(lib.getName());
            for (DefaultPrebuiltStaticLibraryBinary bin :
                     lib.getBinaries().withType(DefaultPrebuiltStaticLibraryBinary.class)) {
               File obj = getLibFile(bin, directory, dependencyName, LibraryType.STATIC);
               if (obj.exists()) {
                  bin.setStaticLibraryFile(obj);

                  if(config.getWithArgs() != null) {
                     addLinkerArgs(obj.getAbsolutePath(), config.getWithArgs(), buildingExtension);
                  }
                  if(!testDependencies) {
                     addDependencyToComponent(dependencyName, LibraryType.STATIC);
                  }
                  addDependencyToGoogleTestBinary(dependencyName, LibraryType.STATIC);
               }
            }
         }
      }
   }

   /**
    * Add the dependency as a shared library if it is configured as such.
    *
    * @param directory          the base directory in which the dependency exists in its unpacked state.
    * @param dependencyName     the name of the dependency (this is derived from the directory name (minus the version)
    * @param buildingExtension  the plugins extension (i.e. the 'building' configuration in groovy)
    * @param libs               the PrebuiltLibraries libs (the object that the dependency gets added to)
    * @param dependencyHeaders  the headers associated with this dependency
    */
   private void addSharedDependencies(File directory,
                                      String dependencyName,
                                      BuildingExtension buildingExtension,
                                      DefaultPrebuiltLibraries libs,
                                      List<File> dependencyHeaders) {

      Collection<SharedBuildConfiguration> configs = buildingExtension.getStorage().getSharedBuildConfigurations(dependencyName);
      for (SharedBuildConfiguration config : configs) {
         if(config.getLibs() != null && !config.getLibs().isEmpty()) {
            for (String library : config.getLibs()) {
               PrebuiltLibrary lib = libs.create(library);

               lib.getHeaders().setSrcDirs(dependencyHeaders);

               libs.resolveLibrary(lib.getName());

               for (DefaultPrebuiltSharedLibraryBinary bin :
                        lib.getBinaries().withType(DefaultPrebuiltSharedLibraryBinary.class)) {
                  File obj = getLibFile(bin, directory, library, LibraryType.SHARED);
                  if (obj.exists()) {
                     bin.setSharedLibraryFile(obj);

                     if(!testDependencies) {
                        addDependencyToComponent(library, LibraryType.SHARED);
                     }
                     addDependencyToGoogleTestBinary(library, LibraryType.SHARED);
                  }
               }
            }
         } else {
            /**
             * Standard configuration uses the dependency name as the lib name.
             */
            PrebuiltLibrary lib = libs.create(dependencyName);
            lib.getHeaders().setSrcDirs(dependencyHeaders);
            libs.resolveLibrary(lib.getName());
            for (DefaultPrebuiltSharedLibraryBinary bin :
                     lib.getBinaries().withType(DefaultPrebuiltSharedLibraryBinary.class)) {
               File obj = getLibFile(bin, directory, dependencyName, LibraryType.SHARED);
               if (obj.exists()) {
                  bin.setSharedLibraryFile(obj);

                  if(!testDependencies) {
                     addDependencyToComponent(dependencyName, LibraryType.SHARED);
                  }
                  addDependencyToGoogleTestBinary(dependencyName, LibraryType.SHARED);
               }
            }
         }
      }
   }

   /**
    * Add the dependency as a shared library if it is configured as such.
    *
    * @param directory          the base directory in which the dependency exists in its unpacked state.
    * @param dependencyName     the name of the dependency (this is derived from the directory name (minus the version)
    * @param buildingExtension  the plugins extension (i.e. the 'building' configuration in groovy)
    *
    * @return The list of header files associated with the dependency.
    */
   private List<File> createHeaderFiles(File directory, String dependencyName, BuildingExtension buildingExtension) {
      Collection<HeaderBuildConfiguration>
            headerConfigurations = buildingExtension.getStorage().getHeaderBuildConfigurations(dependencyName);
      if (headerConfigurations != null && !headerConfigurations.isEmpty()) {
         List<File> headers = new ArrayList<>();
         for (HeaderBuildConfiguration buildConfiguration : headerConfigurations) {
            for (String path : buildConfiguration.getDirs()) {
               headers.add(new File(directory, path));
            }
         }
         return headers;
      } else {
         return Collections.singletonList(new File(directory, "include"));
      }
   }

   /**
    * Create the lib file given the prebuilt library configuration.
    *
    * @param bin        the configuration for different architecture
    * @param directory  the base directory in which the dependency exists in its unpacked state.
    * @param libName    the name of the library (dependency)
    * @param type       the type of library (shared or static only make sense here)
    *
    * @return the File associated with the library. This will not turn null, but the file may not exists
    */
   private File getLibFile(AbstractPrebuiltLibraryBinary bin, File directory, String libName, LibraryType type) {
      String arch = bin.getTargetPlatform().getArchitecture().getName().replace('-', '_');
      String os = bin.getTargetPlatform().getOperatingSystem().getName();
      String ext = bin.getTargetPlatform().getOperatingSystem().isWindows() ? "lib" : "a";
      if(type == LibraryType.SHARED) {
         ext = bin.getTargetPlatform().getOperatingSystem().isWindows() ? "dll" : "so";
      }
      String prefix = bin.getTargetPlatform().getOperatingSystem().isWindows() ? "" : "lib";
      String file = String.format("lib/%s_%s/%s%s.%s",
                                  os,
                                  arch,
                                  prefix,
                                  libName,
                                  ext);
      return new File(directory, file);
   }

   /**
    * Add the linker args to the available native toolchains (gcc, visualCpp, etc....)
    *
    * @param fileName the static library in which to apply the linker args.
    * @param args     the arguments in which to apply.
    */
   private void addLinkerArgs(String fileName, StaticBuildConfiguration.WithArgs args, BuildingExtension buildingExtension) {
      System.out.println(String.format("Adding Linker Args ('%s') for '%s'", args, fileName));


      List<String> arguments = new ArrayList<>();
      arguments.addAll(args.before);
      arguments.add(fileName);
      arguments.addAll(args.after);



//      ModelRegistry projectModel = getServices()
//               .get(ProjectModelResolver.class)
//               .resolveProjectModel(getProject().getPath());
//
//
//
//      NativeToolChainRegistry registry = projectModel.find("toolChains", NativeToolChainRegistry.class);
//      for(NativeToolChain toolChain : registry) {
//         System.out.println("Toolchain: " + toolChain.getName());
//      }
//
//      Gcc gcc = (Gcc)registry.getByName("gcc");
//
//      gcc.eachPlatform(new Action<GccPlatformToolChain>() {
//         @Override
//         public void execute(GccPlatformToolChain gccPlatformToolChain) {
//            gccPlatformToolChain.getLinker().setExecutable("-Wl,--whole-archive");
//         }
//      });


   }

   /**
    * Add the dependency to the components cpp plugin configuration.
    *
    * @param dependencyName the name of the dependency or library
    * @param type           the type being added.
    */
   private void addDependencyToComponent(String dependencyName, LibraryType type) {
      ModelRegistry projectModel = getServices()
               .get(ProjectModelResolver.class)
               .resolveProjectModel(getProject().getPath());
      NativeLibrarySpec component = projectModel
               .find("components", ComponentSpecContainer.class)
               .withType(NativeLibrarySpec.class)
               .get(getComponentName());
      CppSourceSet cppSourceSet = component
               .getSources()
               .withType(CppSourceSet.class)
               .get(getComponentSourceSetName());

      Map<String, String> map = new HashMap<>();
      map.put("library", dependencyName);
      map.put("linkage", type.getName()); // api = header only, other options are shared or static.
      cppSourceSet.lib(map);
   }

   /**
    * Add the dependency to the google test binary.
    *
    * @param dependencyName the name of the dependency or library.
    * @param type           the type being added.
    */
   private void addDependencyToGoogleTestBinary(String dependencyName, LibraryType type) {
      ModelRegistry projectModel = getServices()
               .get(ProjectModelResolver.class)
               .resolveProjectModel(getProject().getPath());
      for (GoogleTestTestSuiteBinarySpec binary :
               projectModel.find("binaries", BinaryContainer.class).withType(GoogleTestTestSuiteBinarySpec.class)) {

         Map<String, String> map = new HashMap<>();
         map.put("library", dependencyName);
         map.put("linkage", type.getName());
         binary.lib(map);
      }
   }

   /**
    * Simple method to get the dependency name from the given file name. This will strip the version from the file.
    *
    * @param fileName the name of the file.
    * @return the dependency name.
    */
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
