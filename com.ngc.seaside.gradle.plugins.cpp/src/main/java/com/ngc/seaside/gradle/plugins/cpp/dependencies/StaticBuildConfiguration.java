/**
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2018, Northrop Grumman Systems Corporation
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of
 * Northrop Grumman Systems Corporation. The intellectual and technical concepts
 * contained herein are proprietary to Northrop Grumman Systems Corporation and
 * may be covered by U.S. and Foreign Patents or patents in process, and are
 * protected by trade secret or copyright law. Dissemination of this information
 * or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from Northrop Grumman.
 */
package com.ngc.seaside.gradle.plugins.cpp.dependencies;

import groovy.lang.Closure;

import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Configure a static dependency.
 */
public class StaticBuildConfiguration extends AbstractBuildConfiguration {

   private WithArgs withArgs;

   /**
    * Constructor
    *
    * @param project the gradle project.
    */
   public StaticBuildConfiguration(Project project) {
      super(project);
   }

   /**
    * Set the linker arguments associated with the libraries.
    *
    * @param closure the Groovy closure that maps to the #WithArgs class.
    * @return the arguments
    */
   public WithArgs withArgs(Closure<?> closure) {
      withArgs = new WithArgs();
      getProject().configure(withArgs, closure);
      return withArgs;
   }

   /**
    * Get the arguments. These will be applied to every item in the libs field.
    *
    * @return the arguments.
    */
   public WithArgs getWithArgs() {
      return withArgs;
   }

   /**
    * Set the arguments.
    *
    * @param withArgs the arguments
    */
   public void setWithArgs(WithArgs withArgs) {
      this.withArgs = withArgs;
   }

   @Override
   public String toString() {
      return super.toString() +
             ", withArgs=" + withArgs;
   }

   /**
    * The link arguments for a static configuration.
    */
   public static class WithArgs {
      List<String> before = new ArrayList<>();
      List<String> after = new ArrayList<>();

      WithArgs() {}

      /**
       * Get the items that should be before the static library in the link options.
       * @return the before options.
       */
      public List<String> getBefore() {
         return before;
      }

      /**
       * Set the before options
       *
       * @param before the before options
       */
      public void setBefore(List<String> before) {
         this.before = before;
      }

      /**
       * Set the before options
       *
       * @param before the before options
       */
      public void before(String... before) {
         Collections.addAll(this.before, before);
      }

      /**
       * Get the items that should be after the static library in the link options.
       * @return the after options.
       */
      public List<String> getAfter() {
         return after;
      }

      /**
       * Set the after options
       *
       * @param after the after options
       */
      public void setAfter(List<String> after) {
         this.after = after;
      }

      /**
       * Set the after options
       *
       * @param after the after options
       */
      public void after(String... after) {
         Collections.addAll(this.after, after);
      }

      @Override
      public String toString() {
         return "before='" + before + '\'' +
                ", after='" + after + '\'';
      }
   }
}
