package com.ngc.seaside.gradle.extensions.cpp.celix

class CelixDistributionExtension {

    private final static String[] DEFAULT_BUNDLE_NAMES = [
        "dm_shell",
        "shell",
        "shell_tui"
    ]

    String distributionName
    String distributionDir
    String runScript
    List<String> defaultBundlesToInclude = new ArrayList<>()

    CelixDistributionExtension() {
        defaultBundlesToInclude.addAll(DEFAULT_BUNDLE_NAMES)
    }

    void includeDefaultBundle(String bundleName) {
        defaultBundlesToInclude.add(bundleName)
    }
}
