package com.ngc.seaside.gradle.util

import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Copy

class FileUtil {

    static void copyFileTreeToDest(FileTree source, String destination) {
        Copy copy = new Copy()
        copy.source = source
        copy.destinationDir = new File(destination)
        copy.execute()
    }

    static String toPath(String... items) {
        return items.flatten().join(File.separator)
    }
}
