/**
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
