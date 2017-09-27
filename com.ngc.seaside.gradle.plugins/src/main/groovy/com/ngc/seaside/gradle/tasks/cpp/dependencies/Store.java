package com.ngc.seaside.gradle.tasks.cpp.dependencies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jprovence on 9/27/2017.
 */
public class Store {

   static Map<String, List<BuildingExtension.Statically>> staticDependenciesMap = new LinkedHashMap<>();

   public static void add(BuildingExtension.Statically statically) {
      if(staticDependenciesMap.containsKey(statically.getDependency())) {
         staticDependenciesMap.get(statically.getDependency()).add(statically);
      } else {
         List<BuildingExtension.Statically> list = new ArrayList<>();
         list.add(statically);
         staticDependenciesMap.put(statically.getDependency(), list);
      }
   }

   public static Collection<String> getStaticDependencies() {
     return staticDependenciesMap.keySet();
   }

}
