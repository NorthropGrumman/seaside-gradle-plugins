/**
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2019, Northrop Grumman Systems Corporation
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of
 * Northrop Grumman Systems Corporation. The intellectual and technical concepts
 * contained herein are proprietary to Northrop Grumman Systems Corporation and
 * may be covered by U.S. and Foreign Patents or patents in process, and are
 * protected by trade secret or copyright law. Dissemination of this information
 * or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from Northrop Grumman.
 */
package com.ngc.seaside.gradle.plugins.repository;

/**
 * Default implementation of {@link RepositoryConfiguration}.
 */
public class DefaultRepositoryConfiguration implements RepositoryConfiguration {

   private String repositoryName;
   private String urlProperty;
   private String usernameProperty;
   private String passwordProperty;
   private boolean isRequired;
   private boolean isAuthenticationRequired;

   /**
    * Default constructor.
    */
   public DefaultRepositoryConfiguration() {}

   /**
    * Copy constructor.
    */
   public DefaultRepositoryConfiguration(RepositoryConfiguration config) {
      from(config);
   }

   @Override
   public String getName() {
      return repositoryName;
   }

   @Override
   public DefaultRepositoryConfiguration setName(String repositoryName) {
      this.repositoryName = repositoryName;
      return this;
   }

   @Override
   public String getUrlProperty() {
      return urlProperty;
   }

   @Override
   public DefaultRepositoryConfiguration setUrlProperty(String urlProperty) {
      this.urlProperty = urlProperty;
      return this;
   }

   @Override
   public String getUsernameProperty() {
      return usernameProperty;
   }

   @Override
   public DefaultRepositoryConfiguration setUsernameProperty(String usernameProperty) {
      this.usernameProperty = usernameProperty;
      return this;
   }

   @Override
   public String getPasswordProperty() {
      return passwordProperty;
   }

   @Override
   public DefaultRepositoryConfiguration setPasswordProperty(String passwordProperty) {
      this.passwordProperty = passwordProperty;
      return this;
   }

   @Override
   public boolean isRequired() {
      return isRequired;
   }

   @Override
   public DefaultRepositoryConfiguration setRequired(boolean required) {
      this.isRequired = required;
      return this;
   }

   @Override
   public DefaultRepositoryConfiguration optional() {
      setRequired(false);
      return this;
   }

   @Override
   public boolean isAuthenticationRequired() {
      return isAuthenticationRequired;
   }

   @Override
   public DefaultRepositoryConfiguration setAuthenticationRequired(boolean required) {
      this.isAuthenticationRequired = required;
      return this;
   }

}
