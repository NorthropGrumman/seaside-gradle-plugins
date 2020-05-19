/*
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
