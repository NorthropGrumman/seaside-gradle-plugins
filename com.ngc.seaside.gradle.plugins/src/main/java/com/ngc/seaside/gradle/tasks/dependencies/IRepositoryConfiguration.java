package com.ngc.seaside.gradle.tasks.dependencies;

import java.nio.file.Path;

public interface IRepositoryConfiguration {

   String getRemoteRepositoryUrl();

   String getRemoteRepositoryUsername();

   String getRemoteRepositoryPassword();

   Path getLocalMavenRepository();
}
