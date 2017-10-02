package com.ngc.seaside.gradle.tasks.cpp.dependencies;

import groovy.lang.Closure;

import org.gradle.api.Project;

import java.util.List;

/**
 *
 */
public class StaticBuildConfiguration extends AbstractBuildConfiguration {

   private WithArgs withArgs;

   public StaticBuildConfiguration(Project project) {
      super(project);
   }

   public WithArgs withArgs(Closure closure) {
      withArgs = new WithArgs();
      getProject().configure(withArgs, closure);
      return withArgs;
   }

   public WithArgs getWithArgs() {
      return withArgs;
   }

   public void setWithArgs(WithArgs withArgs) {
      this.withArgs = withArgs;
   }

   @Override
   public String toString() {
      return super.toString() +
             ", withArgs=" + withArgs;
   }

   public static class WithArgs {
      List<String> before;
      List<String> after;

      WithArgs() {}

      public List<String> getBefore() {
         return before;
      }

      public void setBefore(List<String> before) {
         this.before = before;
      }

      public List<String> getAfter() {
         return after;
      }

      public void setAfter(List<String> after) {
         this.after = after;
      }

      @Override
      public String toString() {
         return "before='" + before + '\'' +
                ", after='" + after + '\'';
      }
   }
}
