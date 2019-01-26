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
 * Property details for a repository.
 */
public interface RepositoryConfiguration {

   /**
    * Returns the descriptive name of the repository.
    * 
    * @return the descriptive name of the repository.
    */
   String getName();

   /**
    * Sets the descriptive name of the repository.
    * 
    * @param repositoryName the descriptive name of the repository
    * @return this
    */
   RepositoryConfiguration setName(String repositoryName);

   /**
    * Returns the property key for determining the url of the repository.
    * 
    * @return the property key for determining the url of the repository
    */
   String getUrlProperty();

   /**
    * Sets the property key for determining the url of the repository.
    * 
    * @param urlProperty the property key for determining the url of the repository
    * @return this
    */
   RepositoryConfiguration setUrlProperty(String urlProperty);

   /**
    * Returns the property key for determining the username of the repository authentication.
    * 
    * @return the property key for determining the username of the repository authentication
    */
   String getUsernameProperty();

   /**
    * Sets the property key for determining the username of the repository authentication.
    * 
    * @param usernameProperty the property key for determining the username of the repository authentication
    * @return this
    */
   RepositoryConfiguration setUsernameProperty(String usernameProperty);

   /**
    * Returns the property key for determining the password of the repository authentication.
    * 
    * @return the property key for determining the password of the repository authentication
    */
   String getPasswordProperty();

   /**
    * Sets the property key for determining the password of the repository authentication.
    * 
    * @param passwordProperty the property key for determining the password of the repository authentication
    * @return this
    */
   RepositoryConfiguration setPasswordProperty(String passwordProperty);

   /**
    * Returns whether or not the repository's property is required to be set.
    * 
    * @return whether or not the repository's property is required to be set
    */
   boolean isRequired();

   /**
    * Sets whether or not the repository's property is required to be set.
    * 
    * @param required whether or not the repository's property is required to be set
    * @return this
    */
   RepositoryConfiguration setRequired(boolean required);

   /**
    * Requires that the repository's property be set.
    * 
    * @return this
    */
   default RepositoryConfiguration required() {
      return setRequired(true);
   }

   /**
    * Sets the requirement that the repository's property be set to optional.
    * 
    * @return this
    */
   default RepositoryConfiguration optional() {
      return setRequired(false);
   }

   /**
    * Returns whether or not the repository's username and password properties are required to be set.
    * 
    * @return whether or not the repository's username and password properties are required to be set
    */
   boolean isAuthenticationRequired();

   /**
    * Sets whether or not the repository's username and password properties are required to be set.
    * 
    * @param required whether or not the repository's username and password properties are required to be set
    * @return this
    */
   RepositoryConfiguration setAuthenticationRequired(boolean required);

   /**
    * Requires that the repository's username and password properties be set.
    * 
    * @return this
    */
   default RepositoryConfiguration authenticationRequired() {
      return this.setAuthenticationRequired(true);
   }

   /**
    * Sets the requirement that the repository's username and password properties be set to optional.
    * 
    * @return this
    */
   default RepositoryConfiguration authenticationOptional() {
      return this.setAuthenticationRequired(false);
   }

   /**
    * Copies the given repository configuration to this configuration.
    * 
    * @param configuration other configuration
    * @return this
    */
   default RepositoryConfiguration from(RepositoryConfiguration configuration) {
      this.setName(configuration == null ? null : configuration.getName());
      this.setUrlProperty(configuration == null ? null : configuration.getUrlProperty());
      this.setUsernameProperty(configuration == null ? null : configuration.getUsernameProperty());
      this.setPasswordProperty(configuration == null ? null : configuration.getPasswordProperty());
      this.setAuthenticationRequired(configuration == null ? null : configuration.isAuthenticationRequired());
      this.setRequired(configuration == null ? null : configuration.isRequired());
      return this;
   }

}
