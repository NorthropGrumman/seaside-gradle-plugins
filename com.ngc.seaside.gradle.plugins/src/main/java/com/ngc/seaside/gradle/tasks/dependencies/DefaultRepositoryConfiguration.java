package com.ngc.seaside.gradle.tasks.dependencies;

import java.nio.file.Path;

public class DefaultRepositoryConfiguration implements IRepositoryConfiguration {

   private String remoteRepositoryUrl;
   private String remoteRepositoryUsername;
   private String remoteRepositoryPassword;
   private Path localMavenRepository;

   @Override
   public String getRemoteRepositoryUrl() {
      return remoteRepositoryUrl;
   }

   public DefaultRepositoryConfiguration setRemoteRepositoryUrl(String remoteRepositoryUrl) {
      this.remoteRepositoryUrl = remoteRepositoryUrl;
      return this;
   }

   @Override
   public String getRemoteRepositoryUsername() {
      return remoteRepositoryUsername;
   }

   public DefaultRepositoryConfiguration setRemoteRepositoryUsername(String remoteRepositoryUsername) {
      this.remoteRepositoryUsername = remoteRepositoryUsername;
      return this;
   }

   @Override
   public String getRemoteRepositoryPassword() {
      return remoteRepositoryPassword;
   }

   public DefaultRepositoryConfiguration setRemoteRepositoryPassword(String remoteRepositoryPassword) {
      this.remoteRepositoryPassword = remoteRepositoryPassword;
      return this;
   }

   @Override
   public Path getLocalMavenRepository() {
      return localMavenRepository;
   }

   public DefaultRepositoryConfiguration setLocalMavenRepository(Path localMavenRepository) {
      this.localMavenRepository = localMavenRepository;
      return this;
   }
}
