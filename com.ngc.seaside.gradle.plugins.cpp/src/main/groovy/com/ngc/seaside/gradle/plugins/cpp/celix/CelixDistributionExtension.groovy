package com.ngc.seaside.gradle.plugins.cpp.celix

/**
 * Used to configure a project that generates a Celix distribution ZIP.
 */
class CelixDistributionExtension {

    /**
     * The default Celix bundles that are included in a distribution.
     */
    private final static String[] DEFAULT_BUNDLE_NAMES = [
        "dm_shell",
        "shell",
        "shell_tui"
    ]

    /**
     * The name of the distribution.
     */
    String distributionName

    /**
     * The name of the distribution directory.
     */
    String distributionDir

    /**
     * The location of the run script to generate.
     */
    String runScript

    /**
     * The names of the default bundles to include.
     */
    List<String> defaultBundlesToInclude = new ArrayList<>()

    CelixDistributionExtension() {
        defaultBundlesToInclude.addAll(DEFAULT_BUNDLE_NAMES)
    }

    /**
     * Includes the given default bundle in the distribution.
     * @param bundleName
     */
    void includeDefaultBundle(String bundleName) {
        defaultBundlesToInclude.add(bundleName)
    }
}
