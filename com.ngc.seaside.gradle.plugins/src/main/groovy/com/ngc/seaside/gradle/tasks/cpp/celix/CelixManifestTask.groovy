package com.ngc.seaside.gradle.tasks.cpp.celix

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.util.jar.Attributes
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

class CelixManifestTask extends DefaultTask {

    Map<String, Object> entries = new HashMap<>()

    String file

    CelixManifestTask() {
        entries.put(Attributes.Name.MANIFEST_VERSION.toString(), '1.0')
    }

    @TaskAction
    void createManifest() {
        Manifest manifest = new Manifest()
        Attributes main = manifest.getMainAttributes()
        for(Map.Entry<String, Object> entry : entries) {
            main.put(new Attributes.Name(entry.getKey()), entry.getValue().toString())
        }

        File file = getProject().file(file)
        // Create the parent directory if needed.
        File parent = file.getParentFile()
        if(parent != null) {
            parent.mkdirs()
        }

        file.withOutputStream { s ->
            manifest.write(s)
        }
    }

    void entry(String key, Object value) {
        entries.put(key, value)
    }

    void entries(Map<String, Object> entries) {
        this.entries.putAll(entries)
    }
}
