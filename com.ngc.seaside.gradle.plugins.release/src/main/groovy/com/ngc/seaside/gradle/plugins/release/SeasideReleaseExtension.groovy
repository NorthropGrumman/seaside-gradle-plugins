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
package com.ngc.seaside.gradle.plugins.release

class SeasideReleaseExtension {
    private static final boolean DEFAULT_PUSH = true
    private static final boolean DEFAULT_UPLOAD_ARTIFACTS = true
    private static final boolean DEFAULT_COMMIT_CHANGES = true
    private static final String DEFAULT_TAG_PREFIX = 'v'
    private static final String DEFAULT_TAG = ''
    private static final String DEFAULT_VERSION_SUFFIX = '-SNAPSHOT'

    boolean push = DEFAULT_PUSH
    boolean uploadArtifacts = DEFAULT_UPLOAD_ARTIFACTS
    boolean commitChanges = DEFAULT_COMMIT_CHANGES
    String tagPrefix = DEFAULT_TAG_PREFIX
    String tag = DEFAULT_TAG
    String versionSuffix = DEFAULT_VERSION_SUFFIX

    def finalizedBy(String task) {
        this.finalizedBy(task)
    }
}
