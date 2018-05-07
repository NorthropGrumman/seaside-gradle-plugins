package com.ngc.seaside.gradle.util;

import org.gradle.api.Project;
import org.gradle.api.file.FileTree;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

public class FileUtil {

   /**
    * Copies the source filetree to its destination path
    * 
    * @param project project to run this task from
    * @param source source filetree
    * @param destination destination path
    */
   public static void copyFileTreeToDest(Project project, FileTree source, String destination) {
      project.copy(process -> {
         process.from(source);
         process.include(destination);
      });
   }

   /**
    * Creates a ziptree of a giving filename (i.e. foo.zip)
    * 
    * @param project project to run this task from
    * @param zipFilename archive file name (i.e. foo.zip)
    * @return {@link FileTree} of extracted zip
    */
   public static FileTree extractZipfile(Project project, String zipFilename) {
      return project.zipTree(zipFilename);
   }

   /**
    * Coverts array of strings to a file path
    * 
    * @param items strings to be joined to a path
    * @return full qualified path string
    */
   public static String toPath(String... items) {
      return Arrays.stream(items).collect(Collectors.joining(File.separator));
   }
}
