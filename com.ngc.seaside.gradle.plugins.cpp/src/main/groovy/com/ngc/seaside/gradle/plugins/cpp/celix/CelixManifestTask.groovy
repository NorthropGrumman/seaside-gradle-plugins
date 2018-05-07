package com.ngc.seaside.gradle.plugins.cpp.celix

import com.google.common.base.Preconditions
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.util.jar.Attributes
import java.util.jar.Manifest

/**
 * A task that generates a Manifest for Celix bundle.  The entries in the manifest can be configured via {@code entry}
 * an {@code entries}.  Likewise, the output file of the manifest is set via {@code file}.
 */
class CelixManifestTask extends DefaultTask {

    /**
     * The entries to insert into the manifest.
     */
    Map<String, Object> entries = new HashMap<>()

    /**
     * The name of the manifest file to output.
     */
    String file

    CelixManifestTask() {
        // Manifest version must always be set or the generated file will be empty.
        entries.put(Attributes.Name.MANIFEST_VERSION.toString(), '1.0')
    }

    /**
     * Creates a manifest that is saved to the configured location and has the specific entries.
     */
    @TaskAction
    void createManifest() {
        Preconditions.checkState(file != null && !file.trim().isEmpty(),
                                 "file may not be null or an empty string!")

        Manifest manifest = new Manifest()
        Attributes main = manifest.getMainAttributes()
        for (Map.Entry<String, Object> entry : entries) {
            main.put(new Attributes.Name(entry.getKey()), entry.getValue().toString())
        }

        File file = getProject().file(file)
        // Create the parent directory if needed.
        File parent = file.getParentFile()
        if (parent != null) {
            parent.mkdirs()
        }

        file.withOutputStream { s ->
            manifest.write(s)
        }
    }

    /**
     * Adds an entry to the manifest.
     * @param key the entry's key
     * @param value the value
     */
    void entry(String key, Object value) {
        entries.put(key, value)
    }

    /**
     * Adds a group of entries to the manifest.
     * @param entries
     */
    void entries(Map<String, Object> entries) {
        this.entries.putAll(entries)
    }
}
