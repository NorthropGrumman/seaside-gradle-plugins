package com.ngc.seaside.gradle.util.eclipse

import com.google.common.base.Preconditions
import org.gradle.internal.os.OperatingSystem

class EclipsePropertyUtil {
    static String getEclipseVersion(Object extension) {
        Preconditions.checkNotNull(extension.linuxEclipseVersion, "linuxEclipseVersion not defined on extension!")
        Preconditions.checkNotNull(extension.windowsEclipseVersion, "windowsEclipseVersion not defined on extension!")
        Preconditions.checkState(
              OperatingSystem.current().isLinux() || OperatingSystem.current().isWindows(),
              "supported operating systems are Linux and Windows!"
        )

        return OperatingSystem.current().isLinux() ? extension.linuxEclipseVersion : extension.windowsEclipseVersion
    }
}
